package org.jetbrains.research.testgenie.tools.llm.generation

data class OpenAIRequestBody(
    val model: String,
    val messages: List<OpenAIMessage>,
    val stream: Boolean = true,
)

data class OpenAIMessage(
    val role: String,
    val content: String,
)
