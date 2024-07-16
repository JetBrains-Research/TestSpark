package org.jetbrains.research.testspark.core.test

import org.jetbrains.research.testspark.core.data.JUnitVersion
import org.jetbrains.research.testspark.core.test.data.TestSuiteGeneratedByLLM

interface TestSuiteParserStrategy {
    fun parseTestSuite(
        rawText: String,
        junitVersion: JUnitVersion,
        importPattern: Regex,
        packageName: String,
        testNamePattern: String,
    ): TestSuiteGeneratedByLLM?
}
