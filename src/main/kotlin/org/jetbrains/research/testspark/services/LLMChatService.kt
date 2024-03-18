package org.jetbrains.research.testspark.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.bundles.TestSparkBundle
import org.jetbrains.research.testspark.core.generation.network.LLMResponse
import org.jetbrains.research.testspark.core.progress.CustomProgressIndicator
import org.jetbrains.research.testspark.tools.llm.SettingsArguments
import org.jetbrains.research.testspark.tools.llm.error.LLMErrorManager
import org.jetbrains.research.testspark.tools.llm.generation.StandardRequestManagerFactory
import org.jetbrains.research.testspark.core.test.data.TestSuiteGeneratedByLLM

@Service(Service.Level.PROJECT)
class LLMChatService(private val project: Project) {

    private var requestManager = StandardRequestManagerFactory().getRequestManager(project)

    /**
     * Re-initiates the requestManager. All the chat history will be removed.
     */
    fun newSession() {
        requestManager = StandardRequestManagerFactory().getRequestManager(project)
    }

    /**
     * Sends a test generation request (when chat history of the requestManager is empty).
     * Sends a feedback to LLm according to previously generated tests (when chat history of the requestManager is not empty).
     * After receiving the response, it tries to parse the tests
     *
     * @param messageToPrompt: A string that represents the prompt to LLM
     * @param indicator: A progress indicator object that represents the indication of the test generation progress.
     * @param packageName: A string that represents the package name where the tests will be generated.
     * @param project: A Project object that represents the current project in which the tests are to be generated.
     *
     * @return A Pair object containing a String and a TestSuiteGeneratedByLLM object.
     * The string component of the Pair represents the parsing result
     * the TestSuiteGeneratedByLLM component represents the test suite parsed from the LLm response.
     * If the test suite generation fails, the TestSuiteGeneratedByLLM object will be null. and the reason is available in the string.
     */
    fun testGenerationRequest(
        messageToPrompt: String,
        indicator: CustomProgressIndicator,
        packageName: String,
    ): LLMResponse {
        return requestManager.request(messageToPrompt, indicator, packageName)
    }

    /**
     * Sends a test modification request according to user's feedback.
     * After receiving the response, it tries to parse the tests
     *
     * @param testcase: The test that is requested to be modified
     * @param task: A string representing the requested task for test modification
     * @param indicator: A progress indicator object that represents the indication of the test generation progress.
     * @param project: A Project object that represents the current project in which the tests are to be generated.
     *
     * @return A Pair object containing a String and a TestSuiteGeneratedByLLM object.
     * The string component of the Pair represents the parsing result
     * the TestSuiteGeneratedByLLM component represents the test suite parsed from the LLm response.
     * If the test suite generation fails, the TestSuiteGeneratedByLLM object will be null. and the reason is available in the string.
     */
    fun testModificationRequest(
        testcase: String,
        task: String,
        indicator: CustomProgressIndicator,
    ): TestSuiteGeneratedByLLM? {
        // Update Token information
        if (!updateToken()) {
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

        val response = requestManager.request(
            prompt,
            indicator,
            packageName,
            isUserFeedback = true,
        )

        return response.testSuite
    }

    /**
     * Updates token  based on the last entries of settings and check if the token is valid
     *
     * @return True if the token is set, false otherwise.
     */
    private fun updateToken(): Boolean {
        requestManager.token = SettingsArguments.getToken()
        return isCorrectToken()
    }

    /**
     * Checks if the token is set.
     *
     * @return True if the token is set, false otherwise.
     */
    fun isCorrectToken(): Boolean {
        if (!SettingsArguments.isTokenSet()) {
            LLMErrorManager().errorProcess(TestSparkBundle.message("missingToken"), project)
            return false
        }
        return true
    }
}
