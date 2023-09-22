package org.jetbrains.research.testspark.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.TestSparkBundle
import org.jetbrains.research.testspark.tools.llm.SettingsArguments
import org.jetbrains.research.testspark.tools.llm.error.LLMErrorManager
import org.jetbrains.research.testspark.tools.llm.generation.StandardRequestManagerFactory
import org.jetbrains.research.testspark.tools.llm.test.TestSuiteGeneratedByLLM

@Service(Service.Level.PROJECT)
class LLMChatService(
    private val project: Project,
) {

    private var requestManager = StandardRequestManagerFactory().getRequestManager()

    fun newSession() {
        requestManager = StandardRequestManagerFactory().getRequestManager()
    }

    fun testGenerationRequest(
        messageToPrompt: String,
        indicator: ProgressIndicator,
        packageName: String,
        project: Project,
        llmErrorManager: LLMErrorManager
    ): Pair<String, TestSuiteGeneratedByLLM?> {
        return requestManager.request(messageToPrompt, indicator, packageName, project, llmErrorManager)
    }

    fun testModificationRequest(
        testcase: String,
        task: String,
        indicator: ProgressIndicator,
        project: Project
    ): TestSuiteGeneratedByLLM? {
        // Update Token information
        if (!updateToken(project)) {
            return null
        }
        val prompt = "For this test:\n ```\n $testcase\n ```\nPerform the following task: $task"

        var packageName = ""
        testcase.split("\n")[0].let {
            if (it.startsWith("package")) {
                packageName = it
                    .removePrefix("package ")
                    .removeSuffix(";")
                    .trim()
            }
        }

        val requestResult = requestManager.request(
            prompt,
            indicator,
            packageName,
            project,
            LLMErrorManager(),
            isUserFeedback = true
        )

        return requestResult.second
    }

    private fun updateToken(project: Project): Boolean {
        requestManager.token = SettingsArguments.llmUserToken()
        return isCorrectToken(project)
    }

    /**
     * Checks if the token is set.
     *
     * @param project The project for error processing.
     *
     * @return True if the token is set, false otherwise.
     */
    fun isCorrectToken(project: Project): Boolean {
        if (!SettingsArguments.isTokenSet()) {
            LLMErrorManager().errorProcess(TestSparkBundle.message("missingToken"), project)
            return false
        }
        return true
    }
}
