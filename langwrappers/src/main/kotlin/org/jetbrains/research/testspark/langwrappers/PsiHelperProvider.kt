package org.jetbrains.research.testspark.langwrappers

import com.intellij.lang.LanguageExtension
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiFile

interface PsiHelperProvider {

    fun getPsiHelper(file: PsiFile): PsiHelper

    companion object {
        private val EP = LanguageExtension<PsiHelperProvider>("org.jetbrains.research.testspark.psiHelperProvider")
        
        fun getPsiHelper(file: PsiFile): PsiHelper? {
            val language = file.language ?: return null
            return EP.forLanguage(language)?.getPsiHelper(file)
        }
    }
}