package org.jetbrains.research.testspark.core.generation.llm.network

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.research.testspark.core.data.ChatMessage
import org.jetbrains.research.testspark.core.data.LlmModuleType
import org.jetbrains.research.testspark.core.data.TestSparkModule
import org.jetbrains.research.testspark.core.error.HttpError
import org.jetbrains.research.testspark.core.error.LlmError
import org.jetbrains.research.testspark.core.error.TestSparkError
import org.jetbrains.research.testspark.core.generation.llm.network.model.GeminiResponse
import org.jetbrains.research.testspark.core.generation.llm.network.model.HuggingFaceResponse
import org.jetbrains.research.testspark.core.generation.llm.network.model.LlmParams
import org.jetbrains.research.testspark.core.generation.llm.network.model.LlmResponse
import org.jetbrains.research.testspark.core.generation.llm.network.model.OpenAIResponse
import org.jetbrains.research.testspark.core.generation.llm.network.model.constructGeminiRequestBody
import org.jetbrains.research.testspark.core.generation.llm.network.model.constructHuggingFaceRequestBody
import org.jetbrains.research.testspark.core.generation.llm.network.model.constructOpenAiRequestBody
import java.net.HttpURLConnection

enum class LlmProvider(
    val url: (LlmParams) -> String,
    val supportsBearerAuth: Boolean,
    val constructJsonBody: Json.(LlmParams, List<ChatMessage>) -> String,
    val decodeResponse: Json.(rawTextResponse: String) -> LlmResponse,
    val mapHttpStatusCodeToError: (Int) -> TestSparkError,
) {
    OpenAI(
        url = { "https://api.openai.com/v1/chat/completions" },
        supportsBearerAuth = true,
        constructJsonBody = { params, chatHistory ->
            encodeToString(constructOpenAiRequestBody(params, chatHistory))
        },
        decodeResponse = { decodeFromString<OpenAIResponse>(it) },
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
            "$baseUrl${it.model}:streamGenerateContent?alt=sse&key=${it.token}"
        },
        supportsBearerAuth = false,
        constructJsonBody = { params, chatHistory ->
            encodeToString(constructGeminiRequestBody(params, chatHistory))
        },
        decodeResponse = { decodeFromString<GeminiResponse>(it) },
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
        constructJsonBody = { params, chatHistory ->
            encodeToString(constructHuggingFaceRequestBody(params, chatHistory))
        },
        decodeResponse = { decodeFromString<HuggingFaceResponse>(it) },
        mapHttpStatusCodeToError = { httpCode ->
            HttpError(httpCode = httpCode, module = TestSparkModule.Llm(LlmModuleType.HuggingFace))
        },
    );
}