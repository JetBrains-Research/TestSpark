package org.jetbrains.research.testspark.core.test

/**
 * Language ID string should be the same as the language name in com.intellij.lang.Language
 */
enum class SupportedLanguage(
    val languageId: String,
    val extension: String,
) {
    Java(languageId = "JAVA", extension = "java"),
    Kotlin(languageId = "kotlin", extension = "kt"),
}
