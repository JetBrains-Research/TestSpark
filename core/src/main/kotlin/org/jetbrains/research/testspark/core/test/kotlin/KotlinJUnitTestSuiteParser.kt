package org.jetbrains.research.testspark.core.test.kotlin

import org.jetbrains.research.testspark.core.data.JUnitVersion
import org.jetbrains.research.testspark.core.generation.llm.getPackageFromTestSuiteCode
import org.jetbrains.research.testspark.core.test.SupportedLanguage
import org.jetbrains.research.testspark.core.test.TestBodyPrinter
import org.jetbrains.research.testspark.core.test.JUnitTestSuiteParser
import org.jetbrains.research.testspark.core.test.data.TestSuiteGeneratedByLLM
import org.jetbrains.research.testspark.core.test.strategies.JUnitTestSuiteParserStrategy
import org.jetbrains.research.testspark.core.utils.kotlinImportPattern

class KotlinJUnitTestSuiteParser(
    private var packageName: String,
    private val junitVersion: JUnitVersion,
    private val testBodyPrinter: TestBodyPrinter,
) : JUnitTestSuiteParser {
    override fun parseTestSuite(rawText: String): TestSuiteGeneratedByLLM? {
        val packageInsideTestText = getPackageFromTestSuiteCode(rawText, SupportedLanguage.Kotlin)
        if (packageInsideTestText.isNotBlank()) {
            packageName = packageInsideTestText
        }

        return JUnitTestSuiteParserStrategy.parseJUnitTestSuite(
            rawText,
            kotlinImportPattern,
            packageName,
            testNamePattern = "fun",
            testBodyPrinter,
        )
    }
}
