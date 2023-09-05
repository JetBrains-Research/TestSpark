package org.jetbrains.research.testspark.tools.llm.generation

data class OpenAIRequestBody(
    val model: String,
    val messages: List<ChatMessage>,
    val stream: Boolean = true,
)

data class ChatMessage(
    val role: String,
    val content: String,
)
