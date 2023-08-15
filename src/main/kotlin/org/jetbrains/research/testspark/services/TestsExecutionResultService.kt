package org.jetbrains.research.testspark.services

import com.intellij.openapi.project.Project

class TestsExecutionResultService(private val project: Project) {
    private val passingTests: MutableSet<String> = mutableSetOf()

    fun addPassingTest(testName: String) { passingTests.add(testName) }

    fun isTestCasePassing(testName: String): Boolean = passingTests.contains(testName)
}
