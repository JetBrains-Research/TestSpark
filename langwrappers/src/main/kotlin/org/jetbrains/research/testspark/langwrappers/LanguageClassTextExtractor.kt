package org.jetbrains.research.testspark.langwrappers

import com.intellij.psi.PsiFile

interface LanguageClassTextExtractor {
    fun extract(file: PsiFile, classText: String, packagePattern: Regex, importPattern: Regex): String
}
