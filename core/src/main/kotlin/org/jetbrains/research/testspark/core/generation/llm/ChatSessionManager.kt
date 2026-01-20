package org.jetbrains.research.testspark.core.generation.llm

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.research.testspark.core.data.ChatMessage
import org.jetbrains.research.testspark.core.error.LlmError
import org.jetbrains.research.testspark.core.error.Result
import org.jetbrains.research.testspark.core.generation.llm.network.RequestManager
import org.jetbrains.research.testspark.core.generation.llm.network.model.LlmParams

class ChatSessionManager(
    private val requestManager: RequestManager,
    private val llmParams: LlmParams,
) {
    private val mutex = Mutex()
    private val chatHistory = mutableListOf<ChatMessage>()
    private val log = KotlinLogging.logger {}

    suspend fun request(
        prompt: String,
        isUserFeedback: Boolean,
    ): Flow<Result<String>> {
        log.info { "Sending Request..." }

        recordChatMessage(isUserFeedback, ChatMessage.Companion.createUserMessage(message = prompt))

        val chatHistory =
            if (isUserFeedback) {
                chatHistory + ChatMessage.createUserMessage(prompt)
            } else {
                chatHistory
            }

        return requestManager
            .sendRequest(
                llmParams,
                chatHistory,
            ).map { result ->
                val responseString = result.getDataOrNull()
                if (responseString != null && responseString.isEmpty()) {
                    Result.Failure(error = LlmError.EmptyLlmResponse)
                } else {
                    result
                }
            }.onEach { result ->
                val rawText = result.getDataOrNull()
                if (rawText.isNullOrEmpty().not()) {
                    recordChatMessage(
                        isUserFeedback,
                        ChatMessage.Companion.createAssistantMessage(message = rawText),
                    )
                }
            }
    }

    private suspend fun recordChatMessage(
        isUserFeedback: Boolean,
        message: ChatMessage,
    ) {
        if (isUserFeedback) return
        mutex.withLock {
            when (message.role) {
                ChatMessage.ChatRole.User -> chatHistory.add(message)
                ChatMessage.ChatRole.Assistant -> {
                    val lastMessage = chatHistory.lastOrNull()
                    if (lastMessage != null && lastMessage.role == ChatMessage.ChatRole.Assistant) {
                        lastMessage.contentBuilder.append(message.content)
                    } else {
                        chatHistory.add(message)
                    }
                }
            }
        }
    }
}
