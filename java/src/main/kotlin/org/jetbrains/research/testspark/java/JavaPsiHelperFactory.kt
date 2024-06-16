package org.jetbrains.research.testspark.java

import com.intellij.psi.PsiFile
import org.jetbrains.research.testspark.langwrappers.PsiHelper
import org.jetbrains.research.testspark.langwrappers.PsiHelperFactory

class JavaPsiHelperFactory : PsiHelperFactory {
    override fun create(psiFile: PsiFile): PsiHelper = JavaPsiHelper(psiFile)
}