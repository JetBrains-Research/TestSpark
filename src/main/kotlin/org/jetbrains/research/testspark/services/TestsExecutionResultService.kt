package org.jetbrains.research.testspark.services

import com.intellij.openapi.project.Project

class TestsExecutionResultService(private val project: Project) {
    private val passingTests: MutableSet<String> = mutableSetOf()

    /**
     * Adds a passing test to the collection.
     *
     * @param testName the name of the passing test to add
     */
    fun addPassingTest(testName: String) { passingTests.add(testName) }

    /**
     * Checks if a test case with the given name is passing.
     *
     * @param testName The name of the test case to check.
     * @return Returns true if the test case is passing, false otherwise.
     */
    fun isTestCasePassing(testName: String): Boolean = passingTests.contains(testName)

    /**
     * Removes the specified test from the passing tests list.
     *
     * @param testName The name of the test to be removed.
     */
    fun removeFromPassingTest(testName: String) {
        if (passingTests.contains(testName)) passingTests.remove(testName)
    }
}
