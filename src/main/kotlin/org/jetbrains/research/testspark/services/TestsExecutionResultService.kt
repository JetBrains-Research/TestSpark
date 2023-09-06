package org.jetbrains.research.testspark.services

import com.intellij.openapi.project.Project

class TestsExecutionResultService(private val project: Project) {
    // test case name --> test error
    private val testErrors: MutableMap<String, String> = mutableMapOf()

    /**
     * Adds a failed test to the collection.
     *
     * @param testName the name of the passing test to add
     * @param testError the error
     */
    fun addFailedTest(testName: String, testError: String) { testErrors[testName] = testError }

    /**
     * Checks if a test case with the given name is failing.
     *
     * @param testName The name of the test case to check.
     * @return Returns true if the test case is passing, false otherwise.
     */
    fun isTestCaseFailing(testName: String): Boolean = testErrors.contains(testName)

    /**
     * Removes the specified test from the failing tests list.
     *
     * @param testName The name of the test to be removed.
     */
    fun removeFromFailingTest(testName: String) {
        if (testErrors.contains(testName)) testErrors.remove(testName)
    }

    /**
     * Gets error message.
     *
     * @param testName The name of the test to be removed.
     */
    fun getError(testName: String): String {
        if (testErrors.contains(testName)) return testErrors[testName]!!
        return ""
    }

    /**
     * Number of failing tests
     */
    fun size() = testErrors.size

    /**
     * Clear failing tests
     */
    fun clear() = testErrors.clear()
}
