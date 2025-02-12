package org.jetbrains.research.testspark.kotlin.psi

import com.intellij.psi.PsiFile
import org.jetbrains.research.testspark.langwrappers.psi.PsiHelperProvider

class KotlinPsiHelperProvider : PsiHelperProvider {
    override fun getPsiHelper(file: PsiFile) = KotlinPsiHelper(file)
}
