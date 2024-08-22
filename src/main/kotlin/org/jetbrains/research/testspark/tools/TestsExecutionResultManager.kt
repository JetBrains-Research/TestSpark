package org.jetbrains.research.testspark.tools

class TestsExecutionResultManager {
    // test case name --> test error
    private val currentTestErrors: MutableMap<Int, String> = mutableMapOf()

    // test case id --> result list of { trimmed code --> test error ("" if passed) }
    private val executionResult: MutableMap<Int, MutableMap<String, String>> = mutableMapOf()

    /**
     * Adds a failed test to the current execution result.
     *
     * @param testId The id of the failed test.
     * @param testCaseCode The code of the failed test case.
     * @param testError The error message or description of the failed test.
     */
    fun addFailedTest(testId: Int, testCaseCode: String, testError: String) {
        val htmlError = "<html>${testError.replace("===", "").replace("\t", "<br/>").trimEnd()}</html>"
        currentTestErrors[testId] = htmlError
        executionResult[testId]!![getTrimmedCode(testCaseCode)] = htmlError
    }

    /**
     * Adds a passed test to the execution results.
     *
     * @param testId The id of the test.
     * @param testCaseCode The code of the test case.
     */
    fun addPassedTest(testId: Int, testCaseCode: String) {
        if (currentTestErrors.contains(testId)) currentTestErrors.remove(testId)
        executionResult[testId]!![getTrimmedCode(testCaseCode)] = ""
    }

    /**
     * Adds the current passed test to the list of errors.
     *
     * @param testId the id of the test to be added as a current passed test
     */
    fun addCurrentPassedTest(testId: Int) {
        if (currentTestErrors.contains(testId)) currentTestErrors.remove(testId)
    }

    /**
     * Add the current failed test to the list of test errors.
     *
     * @param testId The id of the failed test.
     * @param testError The error message of the failed test.
     */
    fun addCurrentFailedTest(testId: Int, testError: String) {
        val htmlError = "<html>${testError.replace("===", "").replace("\t", "<br/>").trimEnd()}</html>"
        currentTestErrors[testId] = htmlError
    }

    /**
     * Gets error message.
     *
     * @param testCaseId The id of the test to be removed.
     */
    fun getCurrentError(testCaseId: Int): String {
        if (currentTestErrors.contains(testCaseId)) return currentTestErrors[testCaseId]!!
        return ""
    }

    /**
     * Retrieves the error message for a given test.
     *
     * @param testCaseId The id of the test.
     * @param testCaseCode The code of the specific test case.
     * @return The error message for the given test.
     */
    fun getError(testCaseId: Int, testCaseCode: String) = executionResult[testCaseId]!![getTrimmedCode(testCaseCode)]

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
     * @param testCaseIds A list of test case ids for which the execution result needs to be initialized.
     */
    fun initExecutionResult(testCaseIds: List<Int>) {
        for (testCaseId in testCaseIds) {
            executionResult[testCaseId] = mutableMapOf()
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
