package org.jetbrains.research.testspark.tools

import org.jetbrains.research.testspark.core.test.SupportedLanguage
import org.jetbrains.research.testspark.helpers.template.TestGenerator
import org.jetbrains.research.testspark.helpers.java.JavaTestGenerator
import org.jetbrains.research.testspark.helpers.kotlin.KotlinTestGenerator

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
