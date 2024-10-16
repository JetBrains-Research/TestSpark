package org.jetbrains.research.testspark.java

import com.intellij.psi.PsiFile
import org.jetbrains.research.testspark.langwrappers.PsiHelperProvider

class JavaPsiHelperProvider : PsiHelperProvider {
    override fun getPsiHelper(file: PsiFile) = JavaPsiHelper(file)
}
