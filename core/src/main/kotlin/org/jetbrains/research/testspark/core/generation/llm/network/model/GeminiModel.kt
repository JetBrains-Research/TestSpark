package org.jetbrains.research.testspark.core.generation.llm.network.model

import org.jetbrains.research.testspark.core.data.ChatMessage

data class GeminiRequest(
    val contents: List<GeminiRequestBody>,
)

data class GeminiRequestBody(
    val parts: List<GeminiChatMessage>,
)

data class GeminiChatMessage(
    val text: String,
)

data class GeminiReply(
    val content: GeminiReplyContent,
    val finishReason: String,
    val avgLogprobs: Double,
)

data class GeminiReplyContent(
    val parts: List<GeminiReplyPart>,
    val role: String?,
)

data class GeminiReplyPart(
    val text: String,
)

fun constructGeminiRequestBody(params: LlmParams, messages: List<ChatMessage>) = GeminiRequest(
    contents = listOf(
        GeminiRequestBody(parts = messages.map { GeminiChatMessage(it.content) })
    )
)

fun GeminiReply.extractContent() = this.content.parts.first().text
