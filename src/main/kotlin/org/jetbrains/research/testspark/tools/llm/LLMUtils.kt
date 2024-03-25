package org.jetbrains.research.testspark.tools.llm

import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.bundles.TestSparkBundle
import org.jetbrains.research.testspark.core.data.TestGenerationData
import org.jetbrains.research.testspark.core.generation.llm.executeTestCaseModificationRequest
import org.jetbrains.research.testspark.core.generation.llm.network.RequestManager
import org.jetbrains.research.testspark.core.progress.CustomProgressIndicator
import org.jetbrains.research.testspark.core.test.data.TestSuiteGeneratedByLLM
import org.jetbrains.research.testspark.tools.llm.error.LLMErrorManager
import org.jetbrains.research.testspark.tools.llm.generation.JUnitTestsAssembler

/**
 * Checks if the token is set.
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

/**
 * Updates the token and sends a test modification request according to user's feedback.
 * After receiving the response, it tries to parse the generated test cases.
 *
 * @param testCase: The test that is requested to be modified
 * @param task: A string representing the requested task for test modification
 * @param indicator: A progress indicator object that represents the indication of the test generation progress.
 * @param project: A Project object that represents the current project in which the tests are to be generated.
 *
 * @return instance of TestSuiteGeneratedByLLM if the generated test cases are parsable, otherwise null.
 */
fun testModificationRequest(
    testCase: String,
    task: String,
    indicator: CustomProgressIndicator,
    requestManager: RequestManager,
    project: Project,
    testGenerationOutput: TestGenerationData,
): TestSuiteGeneratedByLLM? {
    // Update Token information
    if (!updateToken(requestManager, project)) {
        return null
    }

    val testSuite = executeTestCaseModificationRequest(
        testCase,
        task,
        indicator,
        requestManager,
        testsAssembler = JUnitTestsAssembler(project, indicator, testGenerationOutput),
    )
    return testSuite
}

/**
 * Updates token based on the last entries of settings and check if the token is valid
 *
 * @return True if the token is set, false otherwise.
 */
private fun updateToken(requestManager: RequestManager, project: Project): Boolean {
    requestManager.token = SettingsArguments.getToken()
    return isCorrectToken(project)
}
