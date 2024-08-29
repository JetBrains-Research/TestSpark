package org.jetbrains.research.testspark.testmanager

import org.jetbrains.research.testspark.core.test.SupportedLanguage
import org.jetbrains.research.testspark.testmanager.java.JavaTestGenerator
import org.jetbrains.research.testspark.testmanager.kotlin.KotlinTestGenerator
import org.jetbrains.research.testspark.testmanager.template.TestGenerator

object TestGeneratorFactory {
    /**
     * Creates an instance of TestClassCodeGenerator for the specified language.
     *
     * @param language the programming language for which to create the generator
     * @return an instance of TestClassCodeGenerator
     */
    fun create(language: SupportedLanguage): TestGenerator {
        return when (language) {
            SupportedLanguage.Kotlin -> KotlinTestGenerator
            SupportedLanguage.Java -> JavaTestGenerator
        }
    }
}
