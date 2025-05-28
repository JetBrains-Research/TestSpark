package org.jetbrains.research.testspark.core.generation.llm.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.research.testspark.core.data.ChatMessage

@Serializable
data class OpenAIRequest(
    val model: String,
    val stream: Boolean = true,
    val messages: List<OpenAIChatMessage>,
    val temperature: Float?,
    @SerialName("top_p")
    val topProbability: Float? = null,
) : LlmRequest

@Serializable
data class OpenAIResponse(
    val choices: List<OpenAIChoice>,
) : LlmResponse {
    override fun extractContent(): String = choices.first().delta.content
}

@Serializable
data class OpenAIChatMessage(
    val role: String,
    val content: String,
)

@Serializable
data class OpenAIChoice(
    val index: Int,
    val delta: Delta,
    @SerialName("finish_reason")
    val finishedReason: String,
)

@Serializable
data class Delta(
    val role: String?,
    val content: String,
)

internal fun constructOpenAiRequestBody(
    params: LlmParams,
    messages: List<ChatMessage>,
) = OpenAIRequest(
    model = params.model,
    messages =
        messages.map { message ->
            val role =
                when (message.role) {
                    ChatMessage.ChatRole.User -> "user"
                    ChatMessage.ChatRole.Assistant -> "assistant"
                }
            OpenAIChatMessage(role, message.content)
        },
    temperature = params.temperature,
    topProbability = params.topProbability,
)
