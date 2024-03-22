package org.jetbrains.research.testspark.core.test

import org.jetbrains.research.testspark.core.test.data.TestSuiteGeneratedByLLM


interface TestsPresenter {
    fun representTestSuite(testSuite: TestSuiteGeneratedByLLM): String
    fun representTestCase(testSuite: TestSuiteGeneratedByLLM, testCaseIndex: Int): String
}