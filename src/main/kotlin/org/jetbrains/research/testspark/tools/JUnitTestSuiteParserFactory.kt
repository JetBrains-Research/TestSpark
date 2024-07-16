package org.jetbrains.research.testspark.tools

import org.jetbrains.research.testspark.core.data.JUnitVersion
import org.jetbrains.research.testspark.core.test.Language
import org.jetbrains.research.testspark.core.test.TestSuiteParser
import org.jetbrains.research.testspark.core.test.java.JavaJUnitTestSuiteParser
import org.jetbrains.research.testspark.core.test.kotlin.KotlinJUnitTestSuiteParser
import org.jetbrains.research.testspark.core.test.strategies.TestSuiteParserStrategy
import org.jetbrains.research.testspark.core.utils.javaImportPattern
import org.jetbrains.research.testspark.core.utils.kotlinImportPattern

class JUnitTestSuiteParserFactory {
    companion object {
        fun createJUnitTestSuiteParser(
            packageName: String,
            jUnitVersion: JUnitVersion,
            language: Language,
            parsingStrategy: TestSuiteParserStrategy,
        ): TestSuiteParser = when (language) {
            Language.Java -> JavaJUnitTestSuiteParser(
                packageName,
                jUnitVersion,
                javaImportPattern,
                parsingStrategy,
            )

            Language.Kotlin -> KotlinJUnitTestSuiteParser(
                packageName,
                jUnitVersion,
                kotlinImportPattern,
                parsingStrategy,
            )
        }
    }
}
