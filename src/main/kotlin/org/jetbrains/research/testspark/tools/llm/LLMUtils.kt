package org.jetbrains.research.testspark.tools.llm

import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.bundles.TestSparkBundle
import org.jetbrains.research.testspark.core.generation.llm.network.RequestManager
import org.jetbrains.research.testspark.core.progress.CustomProgressIndicator
import org.jetbrains.research.testspark.core.test.data.TestSuiteGeneratedByLLM
import org.jetbrains.research.testspark.data.TestGenerationData
import org.jetbrains.research.testspark.tools.llm.error.LLMErrorManager
import org.jetbrains.research.testspark.tools.llm.generation.JUnitTestsAssembler
import java.util.Locale

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
 * Returns the generated class name for a given test case.
 *
 * @param testCaseName The test case name.
 * @return The generated class name as a string.
 */
fun getClassWithTestCaseName(testCaseName: String): String {
    val className = testCaseName.replaceFirstChar {
        if (it.isLowerCase()) {
            it.titlecase(Locale.getDefault())
        } else {
            it.toString()
        }
    }
    return "Generated$className"
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
    requestManager: RequestManager,
    project: Project,
    testGenerationOutput: TestGenerationData,
): TestSuiteGeneratedByLLM? {
    // Update Token information
    if (!updateToken(requestManager, project)) {
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
        JUnitTestsAssembler(project, indicator, testGenerationOutput),
        isUserFeedback = true,
    )

    return response.testSuite
}

/**
 * Updates token  based on the last entries of settings and check if the token is valid
 *
 * @return True if the token is set, false otherwise.
 */
private fun updateToken(requestManager: RequestManager, project: Project): Boolean {
    requestManager.token = SettingsArguments.getToken()
    return isCorrectToken(project)
}
