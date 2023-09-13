package org.jetbrains.research.testspark.services

import com.intellij.openapi.project.Project

class TestsExecutionResultService(private val project: Project) {
    // test case name --> test error
    private val currentTestErrors: MutableMap<String, String> = mutableMapOf()

    // test case name --> result list of { trimmed code --> test error ("" if passed) }
    private val executionResult: MutableMap<String, MutableMap<String, String>> = mutableMapOf()

    /**
     * Adds a failed test to the current execution result.
     *
     * @param testName The name of the failed test.
     * @param testCaseCode The code of the failed test case.
     * @param testError The error message or description of the failed test.
     */
    fun addFailedTest(testName: String, testCaseCode: String, testError: String) {
        val htmlError = "<html>${testError.replace("===", "").replace("\t", "<br/>").trimEnd()}</html>"
        currentTestErrors[testName] = htmlError
        executionResult[testName]!![getTrimmedCode(testCaseCode)] = htmlError
    }

    /**
     * Adds a passed test to the execution results.
     *
     * @param testName The name of the test.
     * @param testCaseCode The code of the test case.
     */
    fun addPassedTest(testName: String, testCaseCode: String) {
        if (currentTestErrors.contains(testName)) currentTestErrors.remove(testName)
        executionResult[testName]!![getTrimmedCode(testCaseCode)] = ""
    }

    /**
     * Gets error message.
     *
     * @param testCaseNames The name of the test to be removed.
     */
    fun getCurrentError(testCaseNames: String): String {
        if (currentTestErrors.contains(testCaseNames)) return currentTestErrors[testCaseNames]!!
        return ""
    }

    /**
     * Retrieves the error message for a given test.
     *
     * @param testCaseNames The name of the test.
     * @param testCaseCode The code of the specific test case.
     * @return The error message for the given test.
     */
    fun getError(testCaseNames: String, testCaseCode: String) = executionResult[testCaseNames]!![getTrimmedCode(testCaseCode)]

    /**
     * Returns the trimmed code by removing all whitespace characters from the given code.
     *
     * @param code The code to be trimmed.
     * @return The trimmed code.
     */
    private fun getTrimmedCode(code: String): String = code.filter { !it.isWhitespace() }

    /**
     * Initializes the execution result for a list of test case names.
     *
     * @param testCaseNames A list of test case names for which the execution result needs to be initialized.
     */
    fun initExecutionResult(testCaseNames: List<String>) {
        for (testCasName in testCaseNames) {
            executionResult[testCasName] = mutableMapOf()
        }
    }

    /**
     * Number of failing tests
     */
    fun size() = currentTestErrors.size

    /**
     * Clear failing tests
     */
    fun clear() {
        currentTestErrors.clear()
        executionResult.clear()
    }
}
