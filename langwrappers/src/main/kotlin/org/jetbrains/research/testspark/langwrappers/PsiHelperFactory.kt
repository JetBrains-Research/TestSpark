package org.jetbrains.research.testspark.langwrappers

import com.intellij.lang.Language
import com.intellij.lang.LanguageExtension
import com.intellij.psi.PsiFile

interface PsiHelperFactory {
    fun create(psiFile: PsiFile): PsiHelper
    companion object {
        val EP = LanguageExtension<PsiHelperFactory>("org.jetbrains.research.testspark.psiHelperFactory")

        fun get(language: Language): PsiHelperFactory? = EP.forLanguage(language)

        fun all(): List<PsiHelperFactory> {
            return EP.point?.extensions?.map { it.instance } ?: emptyList()
        }
    }
}