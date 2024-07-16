package org.jetbrains.research.testspark.tools

import org.jetbrains.research.testspark.core.test.Language
import org.jetbrains.research.testspark.core.test.TestBodyPrinter
import org.jetbrains.research.testspark.core.test.java.JavaTestBodyPrinter
import org.jetbrains.research.testspark.core.test.kotlin.KotlinTestBodyPrinter

class TestBodyPrinterFactory {
    companion object {
        fun createTestBodyPrinter(language: Language): TestBodyPrinter {
            return when (language) {
                Language.Kotlin -> KotlinTestBodyPrinter()
                Language.Java -> JavaTestBodyPrinter()
            }
        }
    }
}
