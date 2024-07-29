package org.jetbrains.research.testspark.tools

import org.jetbrains.research.testspark.core.test.SupportedLanguage
import org.jetbrains.research.testspark.helpers.TestClassCodeGenerator
import org.jetbrains.research.testspark.helpers.java.JavaTestClassCodeGenerator
import org.jetbrains.research.testspark.helpers.kotlin.KotlinTestClassCodeGenerator

object TestClassCodeGeneratorFactory {
    /**
     * Creates an instance of TestClassCodeGenerator for the specified language.
     *
     * @param language the programming language for which to create the generator
     * @return an instance of TestClassCodeGenerator
     */
    fun create(language: SupportedLanguage): TestClassCodeGenerator {
        return when (language) {
            SupportedLanguage.Kotlin -> KotlinTestClassCodeGenerator
            SupportedLanguage.Java -> JavaTestClassCodeGenerator
        }
    }
}
