package org.jetbrains.research.testspark.core.test.java

import org.jetbrains.research.testspark.core.data.JUnitVersion
import org.jetbrains.research.testspark.core.generation.llm.getPackageFromTestSuiteCode
import org.jetbrains.research.testspark.core.test.SupportedLanguage
import org.jetbrains.research.testspark.core.test.TestBodyPrinter
import org.jetbrains.research.testspark.core.test.TestSuiteParser
import org.jetbrains.research.testspark.core.test.data.TestSuiteGeneratedByLLM
import org.jetbrains.research.testspark.core.test.strategies.JUnitTestSuiteParserStrategy
import org.jetbrains.research.testspark.core.utils.javaImportPattern

class JavaJUnitTestSuiteParser(
    private val junitVersion: JUnitVersion,
    private val testBodyPrinter: TestBodyPrinter,
) : TestSuiteParser {
    override fun parseTestSuite(rawText: String): TestSuiteGeneratedByLLM? {
        val packageName = getPackageFromTestSuiteCode(rawText, SupportedLanguage.Java)

        return JUnitTestSuiteParserStrategy.parseJUnitTestSuite(
            rawText,
            junitVersion,
            javaImportPattern,
            packageName,
            testNamePattern = "void",
            testBodyPrinter,
        )
    }
}
