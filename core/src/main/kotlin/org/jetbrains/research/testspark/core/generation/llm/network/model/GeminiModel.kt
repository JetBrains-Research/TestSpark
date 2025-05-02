package org.jetbrains.research.testspark.core.generation.llm.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.research.testspark.core.data.ChatMessage

@Serializable
data class GeminiRequest(
    val contents: List<GeminiRequestContents>,
    val generationConfig: GeminiGenerationConfig?,
    @SerialName("system_instruction")
    val systemInstruction: GeminiSystemInstruction?,
) : LlmRequest()

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate>,
) : LlmResponse() {
    override fun extractContent(): String =
        candidates
            .first()
            .content.parts
            .first()
            .text
}

@Serializable
data class GeminiRequestContents(
    val role: String,
    val parts: List<GeminiTextObject>,
)

@Serializable
data class GeminiTextObject(
    val text: String,
)

@Serializable
data class GeminiSystemInstruction(
    val parts: List<GeminiTextObject>,
)

@Serializable
data class GeminiGenerationConfig(
    val temperature: Float?,
    @SerialName("top_p")
    val topP: Float?,
)

@Serializable
data class GeminiCandidate(
    val content: GeminiReplyContent,
)

@Serializable
data class GeminiReplyContent(
    val parts: List<GeminiReplyPart>,
    val role: String?,
)

@Serializable
data class GeminiReplyPart(
    val text: String,
)

internal fun constructGeminiRequestBody(
    params: LlmParams,
    messages: List<ChatMessage>,
) = GeminiRequest(
    contents =
        messages.map {
            GeminiRequestContents(
                role =
                    when (it.role) {
                        ChatMessage.ChatRole.User -> "user"
                        ChatMessage.ChatRole.Assistant -> "model"
                    },
                parts = listOf(GeminiTextObject(it.content)),
            )
        },
    systemInstruction =
        params.systemPrompt?.let {
            GeminiSystemInstruction(parts = listOf(GeminiTextObject(it)))
        },
    generationConfig =
        GeminiGenerationConfig(
            temperature = params.temperature,
            topP = params.topProbability,
        ),
)
