package org.jetbrains.research.testspark.kotlin

import com.intellij.psi.PsiFile
import org.jetbrains.research.testspark.langwrappers.PsiHelperProvider

class KotlinPsiHelperProvider : PsiHelperProvider {
    override fun getPsiHelper(file: PsiFile) = KotlinPsiHelper(file)
}
