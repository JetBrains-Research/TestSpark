package org.jetbrains.research.testspark.core.generation.llm.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.research.testspark.core.data.ChatMessage

@Serializable
data class HuggingFaceRequest(
    val inputs: String,
    val parameters: HuggingFaceParameters,
    val stream: Boolean = true,
): LlmRequest()

@Serializable
data class HuggingFaceResponse(
    val token: HuggingFaceToken,
) : LlmResponse() {
    override fun extractContent(): String = token.text
}

@Serializable
data class HuggingFaceParameters(
    val temperature: Float?,
    @SerialName("top_p")
    val topProbability: Float?,
    @SerialName("return_full_text")
    val appendPromptToResponse: Boolean = false,
    @SerialName("min_length")
    val minLength: Int = 4096,
    @SerialName("max_length")
    val maxLength: Int = 8192,
    @SerialName("max_new_tokens")
    val maxNewTokens: Int = 250,
    @SerialName("max_time")
    val maxTime: Float = 120.0F,
)

@Serializable
data class HuggingFaceToken(
    val text: String,
)

internal fun constructHuggingFaceRequestBody(
    params: LlmParams, messages: List<ChatMessage>
): HuggingFaceRequest {
    val systemPrompt = params.systemPrompt?.let { "$it\n" } ?: ""
    return HuggingFaceRequest(
        inputs = systemPrompt + messages.joinToString(separator = "\n") { it.content },
        parameters = HuggingFaceParameters(
            temperature = params.temperature,
            topProbability = params.topProbability,
        ),
    )
}