package org.jetbrains.research.testspark.core.test.parsers.kotlin

import org.jetbrains.research.testspark.core.data.JUnitVersion
import org.jetbrains.research.testspark.core.test.data.TestSuiteGeneratedByLLM
import org.jetbrains.research.testspark.core.test.parsers.TestSuiteParser
import org.jetbrains.research.testspark.core.test.parsers.strategies.JUnitTestSuiteParserStrategy

class KotlinJUnitTestSuiteParser(
    private val packageName: String,
    private val junitVersion: JUnitVersion,
    private val importPattern: Regex,
) : TestSuiteParser {
    override fun parseTestSuite(rawText: String): TestSuiteGeneratedByLLM? {
        return JUnitTestSuiteParserStrategy.parseTestSuite(
            rawText,
            junitVersion,
            importPattern,
            packageName,
            testNamePattern = "fun",
        )
    }
}
