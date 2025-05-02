package org.jetbrains.research.testspark.core.generation.llm.network.model

import kotlinx.serialization.SerialName
import org.jetbrains.research.testspark.core.data.ChatMessage

/**
 * Adheres the naming of fields for OpenAI chat completion API and checks the correctness of a `role`.
 * <br/>
 * Use this class as a carrier of messages that should be sent to OpenAI API.
 */
data class OpenAIChatMessage(
    val role: String,
    val content: String,
) {
    private companion object {
        /**
         * The API strictly defines the set of roles.
         * The `function` role is omitted because it is already deprecated.
         *
         * See: https://platform.openai.com/docs/api-reference/chat/create
         */
        val supportedRoles = listOf("user", "assistant", "system", "tool")
    }

    init {
        if (!supportedRoles.contains(role)) {
            throw IllegalArgumentException(
                "'$role' is not supported ${OpenAIChatMessage::class}. Available roles are: ${(
                    supportedRoles.joinToString(
                        ", ",
                    ) { "'$it'" }
                )}",
            )
        }
    }
}

data class OpenAIRequestBody(
    val model: String,
    val stream: Boolean,
    val messages: List<OpenAIChatMessage>,
    val temperature: Float? = null,
    @SerialName("top_p")
    val topProbability: Float? = null,
) : LLMRequestBody()

data class OpenAIResponse(
    val choices: List<OpenAIChoice>,
)

data class OpenAIChoice(
    val index: Int,
    val delta: Delta,
    @SerialName("finish_reason")
    val finishedReason: String,
)

data class Delta(
    val role: String?,
    val content: String,
)

fun constructOpenAiRequestBody(
    params: LlmParams, messages: List<ChatMessage>, stream: Boolean,
) = OpenAIRequestBody(
    model = params.model,
    messages = messages.map { message ->
        val role = when (message.role) {
            ChatMessage.ChatRole.User -> "user"
            ChatMessage.ChatRole.Assistant -> "assistant"
        }
        OpenAIChatMessage(role, message.content)
    },
    stream = stream,
)

fun OpenAIResponse.extractContent() = this.choices.first().delta.content
