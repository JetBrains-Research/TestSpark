package org.jetbrains.research.testspark.tools.llm.generation

import org.jetbrains.research.testspark.core.test.SupportedLanguage
import org.jetbrains.research.testspark.core.test.TestBodyPrinter
import org.jetbrains.research.testspark.core.test.java.JavaTestBodyPrinter
import org.jetbrains.research.testspark.core.test.kotlin.KotlinTestBodyPrinter

object TestBodyPrinterFactory {
    fun create(language: SupportedLanguage): TestBodyPrinter {
        return when (language) {
            SupportedLanguage.Kotlin -> KotlinTestBodyPrinter()
            SupportedLanguage.Java -> JavaTestBodyPrinter()
        }
    }
}
