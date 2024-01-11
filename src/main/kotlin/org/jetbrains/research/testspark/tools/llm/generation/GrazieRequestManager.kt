package org.jetbrains.research.testspark.tools.llm.generation

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.services.SettingsApplicationService
import org.jetbrains.research.testspark.tools.llm.error.LLMErrorManager

class GrazieRequestManager : RequestManager() {

    override fun send(
        prompt: String,
        indicator: ProgressIndicator,
        project: Project,
        llmErrorManager: LLMErrorManager,
    ): TestsAssembler {
        var testsAssembler = TestsAssembler(project, indicator)

        try {
            val className = "org.jetbrains.research.grazie.Request"
            val request: Request = Class.forName(className).getDeclaredConstructor().newInstance() as Request
            val profile = SettingsApplicationService.getInstance().state!!.model
            val requestResult = request.request(token, getMessages(), profile, TestsAssembler(project, indicator))
            val requestError = requestResult.first

            if (requestError.isNotEmpty()) {
                with(requestError) {
                    when {
                        contains("invalid: 401") -> llmErrorManager.errorProcess(
                            "Invalid Token for Grazie provided!",
                            project,
                        )

                        else -> llmErrorManager.errorProcess(requestError, project)
                    }
                }
            } else {
                testsAssembler = requestResult.second
            }
        } catch (e: ClassNotFoundException) {
            llmErrorManager.errorProcess("Grazie test generation feature is not available in this build.", project)
        }

        return testsAssembler
    }

    private fun getMessages(): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()
        chatHistory.forEach {
            result.add(Pair(it.role, it.content))
        }
        return result
    }
}
