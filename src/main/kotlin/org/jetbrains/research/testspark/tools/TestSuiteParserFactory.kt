package org.jetbrains.research.testspark.tools

import org.jetbrains.research.testspark.core.data.JUnitVersion
import org.jetbrains.research.testspark.core.test.SupportedLanguage
import org.jetbrains.research.testspark.core.test.TestBodyPrinter
import org.jetbrains.research.testspark.core.test.TestSuiteParser
import org.jetbrains.research.testspark.core.test.java.JavaJUnitTestSuiteParser
import org.jetbrains.research.testspark.core.test.kotlin.KotlinJUnitTestSuiteParser

class TestSuiteParserFactory {
    companion object {
        fun createJUnitTestSuiteParser(
            jUnitVersion: JUnitVersion,
            language: SupportedLanguage,
            testBodyPrinter: TestBodyPrinter,
            packageName: String = "",
        ): TestSuiteParser = when (language) {
            SupportedLanguage.Java -> JavaJUnitTestSuiteParser(
                packageName,
                jUnitVersion,
                testBodyPrinter,
            )

            SupportedLanguage.Kotlin -> KotlinJUnitTestSuiteParser(
                packageName,
                jUnitVersion,
                testBodyPrinter,
            )
        }
    }
}
