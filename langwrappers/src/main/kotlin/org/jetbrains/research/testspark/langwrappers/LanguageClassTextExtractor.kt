package org.jetbrains.research.testspark.langwrappers

import com.intellij.psi.PsiFile

interface LanguageClassTextExtractor {
    fun extract(file: PsiFile, classText: String): String
}
