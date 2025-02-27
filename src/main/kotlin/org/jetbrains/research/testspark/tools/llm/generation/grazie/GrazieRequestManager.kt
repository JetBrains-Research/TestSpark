package org.jetbrains.research.testspark.tools.llm.generation.grazie

import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.core.data.ChatMessage
import org.jetbrains.research.testspark.core.data.LlmModuleType
import org.jetbrains.research.testspark.core.data.TestSparkModule
import org.jetbrains.research.testspark.core.error.HttpError
import org.jetbrains.research.testspark.core.error.LlmError
import org.jetbrains.research.testspark.core.error.TestSparkError
import org.jetbrains.research.testspark.core.error.Result
import org.jetbrains.research.testspark.core.generation.llm.network.RequestManager
import org.jetbrains.research.testspark.core.monitor.ErrorMonitor
import org.jetbrains.research.testspark.core.progress.CustomProgressIndicator
import org.jetbrains.research.testspark.core.test.TestsAssembler
import org.jetbrains.research.testspark.tools.llm.LlmSettingsArguments

class GrazieRequestManager(project: Project) : RequestManager(
    token = LlmSettingsArguments(project).getToken(),
    llmModel = LlmSettingsArguments(project).getModel(),
) {
    override fun send(
        prompt: String,
        indicator: CustomProgressIndicator,
        testsAssembler: TestsAssembler,
        errorMonitor: ErrorMonitor,
    ): Result<Unit, TestSparkError> {
        return try {
            val className = "org.jetbrains.research.grazie.Request"
            val request: GrazieRequest = Class.forName(className).getDeclaredConstructor().newInstance() as GrazieRequest

            val requestError = request.request(token, getMessages(), llmModel, testsAssembler)

            if (requestError.isNotEmpty()) {
                with(requestError) {
                    when {
                        contains("invalid: 401") -> Result.Failure(
                            error = HttpError(httpCode = 401)
                        )

                        contains("invalid: 413 Payload Too Large") -> Result.Failure(
                            error = LlmError.PromptTooLong
                        )

                        else -> Result.Failure(
                            error = HttpError(
                                message = this, module = TestSparkModule.LLM(LlmModuleType.Grazie)
                            )
                        )
                    }
                }
            } else {
                Result.Success(data = Unit)
            }
        } catch (_: ClassNotFoundException) {
            Result.Failure(error = LlmError.GrazieNotAvailable)
        }
    }

    private fun getMessages(): List<Pair<String, String>> {
        return chatHistory.map {
            val role = when (it.role) {
                ChatMessage.ChatRole.User -> "user"
                ChatMessage.ChatRole.Assistant -> "assistant"
            }
            (role to it.content)
        }
    }
}
