package org.jetbrains.research.testspark.tools.llm.generation.grazie

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.jetbrains.research.testspark.core.data.ChatMessage
import org.jetbrains.research.testspark.core.data.LlmModuleType
import org.jetbrains.research.testspark.core.data.TestSparkModule
import org.jetbrains.research.testspark.core.error.HttpError
import org.jetbrains.research.testspark.core.error.LlmError
import org.jetbrains.research.testspark.core.error.Result
import org.jetbrains.research.testspark.core.error.TestSparkError
import org.jetbrains.research.testspark.core.generation.llm.network.RequestManager
import org.jetbrains.research.testspark.core.generation.llm.network.model.LlmParams

class GrazieRequestManager : RequestManager {
    override suspend fun sendRequest(
        params: LlmParams,
        chatHistory: List<ChatMessage>,
        isUserFeedback: Boolean,
    ): Flow<Result<String>> {
        val className = "org.jetbrains.research.grazie.Request"
        val request: GrazieRequest =
            Class.forName(className).getDeclaredConstructor().newInstance() as GrazieRequest

        val messages =
            chatHistory.map {
                val role =
                    when (it.role) {
                        ChatMessage.ChatRole.User -> "user"
                        ChatMessage.ChatRole.Assistant -> "assistant"
                    }
                (role to it.content)
            }

        return request
            .request(params.token, messages, params.model)
            .map { Result.Success(data = it) as Result<String> }
            .catch { emit(Result.Failure(error = it.toError())) }
    }

    companion object {
        private fun Throwable.toError(): TestSparkError {
            val message = message.toString()
            val promptTooLong = message.contains("Provided prompt is too big for this model")
            val preconditionFailed = message.contains("invalid: 412 Precondition Failed")
            return when {
                this is ClassNotFoundException -> LlmError.GrazieNotAvailable
                message.contains("invalid: 401") -> HttpError(httpCode = 401)
                message.contains("invalid: 413 Payload Too Large") -> LlmError.PromptTooLong
                promptTooLong && preconditionFailed -> LlmError.PromptTooLong

                else ->
                    HttpError(
                        message = message,
                        module = TestSparkModule.Llm(LlmModuleType.Grazie),
                    )
            }
        }
    }
}
