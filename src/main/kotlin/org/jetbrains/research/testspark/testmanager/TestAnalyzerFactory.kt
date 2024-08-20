package org.jetbrains.research.testspark.testmanager

import org.jetbrains.research.testspark.core.test.SupportedLanguage
import org.jetbrains.research.testspark.testmanager.java.JavaTestAnalyzer
import org.jetbrains.research.testspark.testmanager.kotlin.KotlinTestAnalyzer
import org.jetbrains.research.testspark.testmanager.template.TestAnalyzer

object TestAnalyzerFactory {
    /**
     * Creates an instance of TestClassCodeAnalyzer for the specified language.
     *
     * @param language the programming language for which to create the analyzer
     * @return an instance of TestClassCodeAnalyzer
     */
    fun create(language: SupportedLanguage): TestAnalyzer {
        return when (language) {
            SupportedLanguage.Kotlin -> KotlinTestAnalyzer
            SupportedLanguage.Java -> JavaTestAnalyzer
        }
    }
}
