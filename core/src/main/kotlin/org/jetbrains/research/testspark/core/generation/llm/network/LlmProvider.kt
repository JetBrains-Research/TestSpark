package org.jetbrains.research.testspark.core.generation.llm.network

import com.google.gson.Gson
import org.jetbrains.research.testspark.core.data.ChatMessage
import org.jetbrains.research.testspark.core.data.LlmModuleType
import org.jetbrains.research.testspark.core.data.TestSparkModule
import org.jetbrains.research.testspark.core.error.HttpError
import org.jetbrains.research.testspark.core.error.LlmError
import org.jetbrains.research.testspark.core.error.TestSparkError
import org.jetbrains.research.testspark.core.generation.llm.network.model.GeminiResponse
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
    val supportsBearerAuth: Boolean,
    val supportsStreaming: Boolean,
    val constructJsonBody: Gson.(LlmParams, List<ChatMessage>) -> String,
    val extractResponse: Gson.(rawTextResponse: String) -> String?,
    val mapHttpStatusCodeToError: (Int) -> TestSparkError,
) {
    OpenAI(
        url = { "https://api.openai.com/v1/chat/completions" },
        supportsBearerAuth = true,
        supportsStreaming = true,
        constructJsonBody = { params, messages ->
            toJson(constructOpenAiRequestBody(params, messages, stream = true))
        },
        extractResponse = { rawText ->
            if (rawText.startsWith(STREAMING_PREFIX) && rawText != "$STREAMING_PREFIX [DONE]") {
                val chunk = fromJson(rawText.removePrefix(STREAMING_PREFIX), OpenAIResponse::class.java)
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
        supportsBearerAuth = false,
        supportsStreaming = false,
        constructJsonBody = { params, messages -> toJson(constructGeminiRequestBody(params, messages)) },
        extractResponse = { rawText -> fromJson(rawText, GeminiResponse::class.java).extractContent() },
        mapHttpStatusCodeToError = { httpCode ->
            when (httpCode) {
                HttpURLConnection.HTTP_INTERNAL_ERROR -> LlmError.PromptTooLong
                else -> HttpError(httpCode = httpCode, module = TestSparkModule.Llm(LlmModuleType.Gemini))
            }
        }
    ),

    Llama(
        url = { "https://api-inference.huggingface.co/models/meta-llama/${it.model}" },
        supportsBearerAuth = true,
        supportsStreaming = true,
        constructJsonBody = { params, messages ->
            toJson(constructHuggingFaceRequestBody(params, messages, stream = true))
        },
        extractResponse = { rawText ->
            if (rawText.startsWith(STREAMING_PREFIX)) {
                val chunk = fromJson(rawText.removePrefix(STREAMING_PREFIX), HuggingFaceResponse::class.java)
                chunk.extractContent()
            } else null
        },
        mapHttpStatusCodeToError = { httpCode ->
            HttpError(httpCode = httpCode, module = TestSparkModule.Llm(LlmModuleType.HuggingFace))
        },
    );

    companion object {
        const val STREAMING_PREFIX = "data:"
    }
}