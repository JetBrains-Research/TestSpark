package org.jetbrains.research.testspark.tools

import org.jetbrains.research.testspark.core.test.SupportedLanguage
import org.jetbrains.research.testspark.helpers.TestClassCodeAnalyzer
import org.jetbrains.research.testspark.helpers.java.JavaTestClassCodeAnalyzer
import org.jetbrains.research.testspark.helpers.kotlin.KotlinTestClassCodeAnalyzer

object TestClassCodeAnalyzerFactory {
    /**
     * Creates an instance of TestClassCodeAnalyzer for the specified language.
     *
     * @param language the programming language for which to create the analyzer
     * @return an instance of TestClassCodeAnalyzer
     */
    fun create(language: SupportedLanguage): TestClassCodeAnalyzer {
        return when (language) {
            SupportedLanguage.Kotlin -> KotlinTestClassCodeAnalyzer
            SupportedLanguage.Java -> JavaTestClassCodeAnalyzer
        }
    }
}
