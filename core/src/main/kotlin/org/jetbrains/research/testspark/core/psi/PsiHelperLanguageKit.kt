package org.jetbrains.research.testspark.core.psi

import com.intellij.lang.LanguageExtension
import com.intellij.lang.Language

interface PsiHelperLanguageKit {
    companion object {
        val EP = LanguageExtension<PsiHelperLanguageKit>("org.jetbrains.research.testspark.psiLanguageKit")

        fun get(language: Language): PsiHelperLanguageKit? = EP.forLanguage(language)

        fun all(): List<PsiHelperLanguageKit> {
            return EP.point?.extensions?.map { it.instance } ?: emptyList()
        }
    }
}
