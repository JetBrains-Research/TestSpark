package org.jetbrains.research.testspark.core.generation.llm.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.jetbrains.research.testspark.core.data.ChatMessage
import org.jetbrains.research.testspark.core.error.HttpError
import org.jetbrains.research.testspark.core.error.Result
import org.jetbrains.research.testspark.core.generation.llm.network.model.LlmParams
import java.net.HttpURLConnection

class HttpRequestManager(
    private val llmProvider: LlmProvider,
    private val client: HttpClient = DEFAULT_HTTP_CLIENT,
    private val json: Json = DEFAULT_JSON,
) : RequestManager {
    override suspend fun sendRequest(
        params: LlmParams,
        chatHistory: List<ChatMessage>,
    ): Flow<Result<String>> =
        flow {
            client
                .preparePost(llmProvider.url(params)) {
                    contentType(ContentType.Application.Json)
                    if (llmProvider.supportsBearerAuth) bearerAuth(params.token)
                    setBody(llmProvider.constructJsonBody(json, params, chatHistory))
                }.execute { httpResponse ->
                    val responseIsSuccessful = httpResponse.status.value == HttpURLConnection.HTTP_OK
                    if (responseIsSuccessful) {
                        val channel = httpResponse.body<ByteReadChannel>()
                        while (currentCoroutineContext().isActive && !channel.isClosedForRead) {
                            val chunk = channel.readUTF8Line() ?: continue
                            val response = processChunk(chunk) ?: continue
                            emit(Result.Success(response))
                        }
                    } else {
                        var error = llmProvider.mapHttpStatusCodeToError(httpResponse.status.value)
                        if (error is HttpError) error = error.copy(message = httpResponse.status.description)
                        emit(Result.Failure(error))
                    }
                }
        }

    fun processChunk(chunk: String): String? {
        if (chunk.startsWith(STREAMING_PREFIX).not()) return null
        return llmProvider.decodeResponse(json, chunk.removePrefix(STREAMING_PREFIX)).extractContent()
    }

    private companion object {
        const val STREAMING_PREFIX = "data:"

        val DEFAULT_HTTP_CLIENT =
            HttpClient(CIO) {
                install(Logging) {
                    logger = Logger.DEFAULT
                    level = LogLevel.BODY
                    sanitizeHeader { header -> header == HttpHeaders.Authorization }
                }
                install(HttpTimeout) {
                    requestTimeoutMillis = 90_000
                    connectTimeoutMillis = 15_000
                    socketTimeoutMillis = 60_000
                }
            }

        val DEFAULT_JSON =
            Json {
                ignoreUnknownKeys = true
                encodeDefaults = true

                // this api is not error-prone; it just means it can be changed or removed in the future
                @OptIn(ExperimentalSerializationApi::class)
                explicitNulls = false
            }
    }
}
