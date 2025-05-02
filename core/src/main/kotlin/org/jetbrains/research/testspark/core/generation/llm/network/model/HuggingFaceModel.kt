package org.jetbrains.research.testspark.core.generation.llm.network.model

import com.google.gson.annotations.SerializedName
import org.jetbrains.research.testspark.core.data.ChatMessage

data class HuggingFaceRequestBody(
    val inputs: String,
    val parameters: HuggingFaceParameters,
    val stream: Boolean,
) : LLMRequestBody()

data class HuggingFaceParameters(
    val temperature: Float?,
    @SerializedName("top_p")
    val topProbability: Float?,
    @SerializedName("return_full_text")
    val appendPromptToResponse: Boolean = false,
)

data class HuggingFaceResponse(
    val token: HuggingFaceToken,
)

data class HuggingFaceToken(
    val text: String,
)

fun constructHuggingFaceRequestBody(
    params: LlmParams, messages: List<ChatMessage>, stream: Boolean,
) = HuggingFaceRequestBody(
    inputs = messages.joinToString(separator = "\n") { it.content },
    parameters = HuggingFaceParameters(
        temperature = params.temperature,
        topProbability = params.topProbability,
    ),
    stream = stream,
)

fun HuggingFaceResponse.extractContent() = this.token.text