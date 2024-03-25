package org.jetbrains.research.testspark.tools.llm.generation.openai

import org.jetbrains.research.testspark.core.data.ChatMessage

data class OpenAIRequestBody(
    val model: String,
    val messages: List<ChatMessage>,
    val stream: Boolean = true,
)
