package org.jetbrains.research.testspark.core.test.strategies

import org.jetbrains.research.testspark.core.data.JUnitVersion
import org.jetbrains.research.testspark.core.test.TestBodyPrinter
import org.jetbrains.research.testspark.core.test.data.TestSuiteGeneratedByLLM

interface TestSuiteParserStrategy {
    fun parseTestSuite(
        rawText: String,
        junitVersion: JUnitVersion,
        importPattern: Regex,
        packageName: String,
        testNamePattern: String,
        printTestBodyStrategy: TestBodyPrinter,
    ): TestSuiteGeneratedByLLM?
}
