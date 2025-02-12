package org.jetbrains.research.testspark.tools

import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.research.testspark.core.test.SupportedLanguage

fun SupportedLanguage.asIntellijLanguage(): Language = when (this) {
    SupportedLanguage.Java -> JavaLanguage.INSTANCE
    SupportedLanguage.Kotlin -> KotlinLanguage.INSTANCE
}