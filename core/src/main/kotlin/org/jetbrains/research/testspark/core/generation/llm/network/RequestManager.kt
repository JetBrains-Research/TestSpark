package org.jetbrains.research.testspark.core.generation.llm.network

import kotlinx.coroutines.flow.Flow
import org.jetbrains.research.testspark.core.error.Result
import org.jetbrains.research.testspark.core.data.ChatMessage
import org.jetbrains.research.testspark.core.generation.llm.network.model.LlmParams


interface RequestManager {

    suspend fun sendRequest(
        params: LlmParams,
        chatHistory: List<ChatMessage>,
        isUserFeedback: Boolean = false,
    ): Flow<Result<String>>
}