package org.jetbrains.research.testspark.core.test

import org.jetbrains.research.testspark.core.data.JUnitVersion
import org.jetbrains.research.testspark.core.test.data.TestSuiteGeneratedByLLM
import org.jetbrains.research.testspark.core.test.java.JavaJUnitTestSuiteParser
import org.jetbrains.research.testspark.core.test.kotlin.KotlinJUnitTestSuiteParser

interface JUnitTestSuiteParser {
    companion object {
        fun create(
            jUnitVersion: JUnitVersion,
            language: SupportedLanguage,
            testBodyPrinter: TestBodyPrinter,
            packageName: String = "",
        ): JUnitTestSuiteParser =
            when (language) {
                SupportedLanguage.Java ->
                    JavaJUnitTestSuiteParser(
                        packageName,
                        jUnitVersion,
                        testBodyPrinter,
                    )

                SupportedLanguage.Kotlin ->
                    KotlinJUnitTestSuiteParser(
                        packageName,
                        jUnitVersion,
                        testBodyPrinter,
                    )
            }
    }

    /**
     * Extracts test cases from raw text and generates a test suite.
     *
     * @param rawText The raw text provided by the LLM that contains the generated test cases.
     * @return A GeneratedTestSuite instance containing the extracted test cases.
     */
    fun parseTestSuite(rawText: String): TestSuiteGeneratedByLLM?
}
