package org.jetbrains.research.testspark.core.generation.llm.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
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
import io.ktor.http.headers
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import org.jetbrains.research.testspark.core.data.ChatMessage
import org.jetbrains.research.testspark.core.error.LlmError
import org.jetbrains.research.testspark.core.error.Result
import org.jetbrains.research.testspark.core.error.TestSparkError
import org.jetbrains.research.testspark.core.generation.llm.network.model.LlmParams
import java.net.HttpURLConnection

class HttpRequestManager(
    private val llmProvider: LlmProvider,
    private val client: HttpClient = HttpClient(CIO) {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
//            sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }
    },
    private val gson: Gson = GsonBuilder().create(),
) : RequestManager {
    override suspend fun sendRequest(
        params: LlmParams,
        chatHistory: List<ChatMessage>,
        isUserFeedback: Boolean
    ): Flow<Result<String>> {
        return flow {
            client.preparePost(llmProvider.url(params)) {
                contentType(ContentType.Application.Json)
                bearerAuth(params.token)
                setBody(llmProvider.constructJsonBody(gson, params, chatHistory))
            }.execute { httpResponse ->
                val responseIsSuccessful = httpResponse.status.value == HttpURLConnection.HTTP_OK
                if (responseIsSuccessful) {
                    if (llmProvider.supportsStreaming) {
                        val channel = httpResponse.body<ByteReadChannel>()
                        while (currentCoroutineContext().isActive && !channel.isClosedForRead) {
                            val line = channel.readUTF8Line() ?: continue
                            println("HttpRequestManager line = $line")
                            val response = llmProvider.extractResponse(gson, line) ?: continue
                            emit(Result.Success(response))
                        }
                    } else {
                        val rawText = httpResponse.body<String>()
                        println("HttpRequestManager rawText = $rawText")
                        val response = llmProvider.extractResponse(gson, rawText)
                        println("HttpRequestManager response = $response")
                        if (response != null) {
                            emit(Result.Success(response))
                        } else {
                            emit(Result.Failure(error = LlmError.EmptyLlmResponse))
                        }
                    }
                } else {
                    val error = llmProvider.mapHttpStatusCodeToError(httpResponse.status.value)
                    emit(Result.Failure(error))
                }
            }
        }
    }
}

