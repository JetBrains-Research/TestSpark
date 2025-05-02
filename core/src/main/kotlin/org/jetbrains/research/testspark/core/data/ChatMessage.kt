package org.jetbrains.research.testspark.core.data

data class ChatMessage(
    val role: ChatRole,
    val contentBuilder: StringBuilder,
) {
    enum class ChatRole {
        User,
        Assistant,
    }

    val content: String
        get() = contentBuilder.toString()

    companion object {
        fun createUserMessage(message: String) = ChatMessage(ChatRole.User, StringBuilder(message))

        fun createAssistantMessage(message: String) = ChatMessage(ChatRole.Assistant, StringBuilder(message))
    }
}
