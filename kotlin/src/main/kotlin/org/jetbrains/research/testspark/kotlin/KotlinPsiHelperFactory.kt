package org.jetbrains.research.testspark.kotlin

import com.intellij.psi.PsiFile
import org.jetbrains.research.testspark.langwrappers.PsiHelper
import org.jetbrains.research.testspark.langwrappers.PsiHelperFactory


class KotlinPsiHelperFactory : PsiHelperFactory {
    override fun create(psiFile: PsiFile): PsiHelper = KotlinPsiHelper(psiFile)
}