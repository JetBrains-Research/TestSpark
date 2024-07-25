package org.jetbrains.research.testspark.core.test

/**
 * Language ID string should be the same as the language name in com.intellij.lang.Language
 */
enum class SupportedLanguage(val languageId: String) {
    Java("JAVA"), Kotlin("kotlin")
}
