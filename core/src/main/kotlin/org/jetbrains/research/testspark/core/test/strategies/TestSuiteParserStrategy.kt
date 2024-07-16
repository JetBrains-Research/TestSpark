package org.jetbrains.research.testspark.core.test.strategies

import org.jetbrains.research.testspark.core.data.JUnitVersion
import org.jetbrains.research.testspark.core.test.PrintTestBody
import org.jetbrains.research.testspark.core.test.data.TestSuiteGeneratedByLLM

interface TestSuiteParserStrategy {
    fun parseTestSuite(
        rawText: String,
        junitVersion: JUnitVersion,
        importPattern: Regex,
        packageName: String,
        testNamePattern: String,
        printTestBodyStrategy: PrintTestBody,
    ): TestSuiteGeneratedByLLM?
}
