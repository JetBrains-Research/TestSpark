package org.jetbrains.research.testspark.core.data

open class ChatMessage protected constructor(
    val role: ChatRole,
    val content: String,
) {
    enum class ChatRole(val representation: String) {
        User("user"),
        Assistant("assistant"),
    }
}

class ChatUserMessage(content: String) : ChatMessage(ChatRole.User, content)
class ChatAssistantMessage(content: String) : ChatMessage(ChatRole.Assistant, content)
