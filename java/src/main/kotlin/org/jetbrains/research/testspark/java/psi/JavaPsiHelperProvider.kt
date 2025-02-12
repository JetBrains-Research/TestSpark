package org.jetbrains.research.testspark.java.psi

import com.intellij.psi.PsiFile
import org.jetbrains.research.testspark.langwrappers.psi.PsiHelperProvider

class JavaPsiHelperProvider : PsiHelperProvider {
    override fun getPsiHelper(file: PsiFile) = JavaPsiHelper(file)
}
