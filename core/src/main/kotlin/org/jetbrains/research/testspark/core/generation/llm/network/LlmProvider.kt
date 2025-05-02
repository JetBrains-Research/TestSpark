package org.jetbrains.research.testspark.core.generation.llm.network

import com.google.gson.Gson
import com.google.gson.JsonParser
import io.ktor.http.HttpHeaders
import org.jetbrains.research.testspark.core.data.ChatMessage
import org.jetbrains.research.testspark.core.data.LlmModuleType
import org.jetbrains.research.testspark.core.data.TestSparkModule
import org.jetbrains.research.testspark.core.error.HttpError
import org.jetbrains.research.testspark.core.error.LlmError
import org.jetbrains.research.testspark.core.error.TestSparkError
import org.jetbrains.research.testspark.core.generation.llm.network.model.GeminiReply
import org.jetbrains.research.testspark.core.generation.llm.network.model.HuggingFaceResponse
import org.jetbrains.research.testspark.core.generation.llm.network.model.LlmParams
import org.jetbrains.research.testspark.core.generation.llm.network.model.OpenAIResponse
import org.jetbrains.research.testspark.core.generation.llm.network.model.constructGeminiRequestBody
import org.jetbrains.research.testspark.core.generation.llm.network.model.constructHuggingFaceRequestBody
import org.jetbrains.research.testspark.core.generation.llm.network.model.constructOpenAiRequestBody
import org.jetbrains.research.testspark.core.generation.llm.network.model.extractContent
import java.net.HttpURLConnection

enum class LlmProvider(
    val url: (LlmParams) -> String,
    val headers: (LlmParams) -> List<Pair<String, String>>?,
    val supportsStreaming: Boolean,
    val constructJsonBody: Gson.(LlmParams, List<ChatMessage>) -> String,
    val extractResponse: Gson.(rawTextResponse: String) -> String?,
    val mapHttpStatusCodeToError: (Int) -> TestSparkError,
) {
    OpenAI(
        url = { "https://api.openai.com/v1/chat/completions" },
        headers = { params -> listOf((HttpHeaders.Authorization to params.token)) },
        supportsStreaming = true,
        constructJsonBody = { params, messages ->
            toJson(constructOpenAiRequestBody(params, messages, stream = true))
        },
        extractResponse = { rawText ->
            val prefix = "data:"
            if (rawText.startsWith(prefix)) {
                val chunk = fromJson(rawText.removePrefix(prefix), OpenAIResponse::class.java)
                chunk.extractContent()
            } else null
        },
        mapHttpStatusCodeToError = { httpCode ->
            when (httpCode) {
                HttpURLConnection.HTTP_BAD_REQUEST -> LlmError.PromptTooLong
                else -> HttpError(httpCode = httpCode, module = TestSparkModule.Llm(LlmModuleType.OpenAi))
            }
        }
    ),

    Gemini(
        url = {
            val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/"
            "$baseUrl${it.model}:generateContent?key=${it.token}"
        },
        headers = { null /* no bearer auth, token is passed in the url */ },
        supportsStreaming = false,
        constructJsonBody = { params, messages -> toJson(constructGeminiRequestBody(params, messages)) },
        extractResponse = { rawText -> fromJson(rawText, GeminiReply::class.java).extractContent() },
        mapHttpStatusCodeToError = { httpCode ->
            when (httpCode) {
                HttpURLConnection.HTTP_INTERNAL_ERROR -> LlmError.PromptTooLong
                else -> HttpError(httpCode = httpCode, module = TestSparkModule.Llm(LlmModuleType.Gemini))
            }
        }
    ),

    Llama(
        url = { "https://api-inference.huggingface.co/models/meta-llama/${it.model}" },
        headers = { params -> listOf((HttpHeaders.Authorization to params.token)) },
        supportsStreaming = true,
        constructJsonBody = { params, messages ->
            toJson(constructHuggingFaceRequestBody(params, messages, stream = true))
        },
        extractResponse = { rawText ->
            val prefix = "data:"
            println("Llama extractResponse rawText = $rawText")
            if (rawText.startsWith(prefix)) {
                val chunk = fromJson(rawText.removePrefix(prefix).also { println("Llama extractResponse text = $it") }, HuggingFaceResponse::class.java)
                chunk.extractContent().also { println("Llama extractResponse content = $it") }
            } else null
        },
        mapHttpStatusCodeToError = { httpCode ->
            HttpError(httpCode = httpCode, module = TestSparkModule.Llm(LlmModuleType.HuggingFace))
        },
    ),
}