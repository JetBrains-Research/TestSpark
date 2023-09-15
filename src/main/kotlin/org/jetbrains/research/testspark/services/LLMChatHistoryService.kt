package org.jetbrains.research.testspark.services

import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.tools.llm.generation.ChatMessage
 class LLMChatHistoryService(
    private val project: Project,
) {
     open val baseChatHistory = emptyList<ChatMessage>().toMutableList()
}