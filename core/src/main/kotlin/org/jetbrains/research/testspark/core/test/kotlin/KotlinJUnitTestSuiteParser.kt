package org.jetbrains.research.testspark.core.test.kotlin

import org.jetbrains.research.testspark.core.data.JUnitVersion
import org.jetbrains.research.testspark.core.test.TestBodyPrinter
import org.jetbrains.research.testspark.core.test.TestSuiteParser
import org.jetbrains.research.testspark.core.test.data.TestSuiteGeneratedByLLM
import org.jetbrains.research.testspark.core.test.strategies.JUnitTestSuiteParserStrategy
import org.jetbrains.research.testspark.core.utils.kotlinImportPattern
import org.jetbrains.research.testspark.core.utils.kotlinPackagePattern

class KotlinJUnitTestSuiteParser(
    private val junitVersion: JUnitVersion,
    private val testBodyPrinter: TestBodyPrinter,
) : TestSuiteParser {
    override fun parseTestSuite(rawText: String): TestSuiteGeneratedByLLM? {
        val packageName = kotlinPackagePattern.find(rawText)?.groups?.get(1)?.value ?: ""

        return JUnitTestSuiteParserStrategy.parseJUnitTestSuite(
            rawText,
            junitVersion,
            kotlinImportPattern,
            packageName,
            testNamePattern = "fun",
            testBodyPrinter,
        )
    }
}
