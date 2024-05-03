package org.jetbrains.research.testspark.core.generation.llm

import org.jetbrains.research.testspark.core.generation.llm.network.RequestManager
import org.jetbrains.research.testspark.core.monitor.DefaultErrorMonitor
import org.jetbrains.research.testspark.core.monitor.ErrorMonitor
import org.jetbrains.research.testspark.core.progress.CustomProgressIndicator
import org.jetbrains.research.testspark.core.test.TestsAssembler
import org.jetbrains.research.testspark.core.test.data.TestSuiteGeneratedByLLM
import java.util.*

// TODO: find a better place for the below functions

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
 * After receiving the response, it tries to parse the generated test cases.
 *
 * @param testCase: The test that is requested to be modified
 * @param task: A string representing the requested task for test modification
 * @param indicator: A progress indicator object that represents the indication of the test generation progress.
 *
 * @return instance of TestSuiteGeneratedByLLM if the generated test cases are parsable, otherwise null.
 */
fun executeTestCaseModificationRequest(
    testCase: String,
    task: String,
    indicator: CustomProgressIndicator,
    requestManager: RequestManager,
    testsAssembler: TestsAssembler,
    errorMonitor: ErrorMonitor = DefaultErrorMonitor()
): TestSuiteGeneratedByLLM? {
    // Update Token information
    val prompt = "For this test:\n ```\n $testCase\n ```\nPerform the following task: $task"

    var packageName = ""
    testCase.split("\n")[0].let {
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
        testsAssembler,
        isUserFeedback = true,
        errorMonitor
    )

    return response.testSuite
}
