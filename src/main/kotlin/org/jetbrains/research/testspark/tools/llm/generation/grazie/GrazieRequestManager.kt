package org.jetbrains.research.testspark.tools.llm.generation.grazie

import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.bundles.llm.LLMMessagesBundle
import org.jetbrains.research.testspark.core.data.ChatMessage
import org.jetbrains.research.testspark.core.monitor.ErrorMonitor
import org.jetbrains.research.testspark.core.progress.CustomProgressIndicator
import org.jetbrains.research.testspark.core.test.TestsAssembler
import org.jetbrains.research.testspark.tools.llm.LlmSettingsArguments
import org.jetbrains.research.testspark.tools.llm.error.LLMErrorManager
import org.jetbrains.research.testspark.tools.llm.generation.IJRequestManager

class GrazieRequestManager(project: Project) : IJRequestManager(project) {
    private val llmErrorManager = LLMErrorManager()

    override fun send(
        prompt: String,
        indicator: CustomProgressIndicator,
        testsAssembler: TestsAssembler,
        errorMonitor: ErrorMonitor,
    ): SendResult {
        var sendResult = SendResult.OK

        try {
            val className = "org.jetbrains.research.grazie.Request"
            val request: GrazieRequest = Class.forName(className).getDeclaredConstructor().newInstance() as GrazieRequest

            val requestError = request.request(token, getMessages(), LlmSettingsArguments(project).getModel(), testsAssembler)

            if (requestError.isNotEmpty()) {
                with(requestError) {
                    when {
                        contains("invalid: 401") -> {
                            llmErrorManager.errorProcess(
                                LLMMessagesBundle.get("wrongToken"),
                                project,
                                errorMonitor = errorMonitor,
                            )
                            sendResult = SendResult.OTHER
                        }

                        contains("invalid: 413 Payload Too Large") -> {
                            llmErrorManager.warningProcess(
                                LLMMessagesBundle.get("tooLongPrompt"),
                                project,
                            )
                            sendResult = SendResult.PROMPT_TOO_LONG
                        }

                        else -> {
                            llmErrorManager.errorProcess(requestError, project, errorMonitor)
                            sendResult = SendResult.OTHER
                        }
                    }
                }
            }
        } catch (e: ClassNotFoundException) {
            llmErrorManager.errorProcess(LLMMessagesBundle.get("grazieError"), project, errorMonitor)
        }

        return sendResult
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
