package org.jetbrains.research.testspark.helpers.psiHelpers

import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile
import org.jetbrains.kotlin.psi.KtFile

object PsiHelperFactory {
    fun getPsiHelper(psiFile: PsiFile): PsiHelperInterface {
        return when (psiFile) {
            is PsiJavaFile -> JavaPsiHelper()
            // TODO implement KotlinPsiHelper class
//            is KtFile -> KotlinPsiHelper()
            else -> throw IllegalArgumentException("Unsupported file type")
        }
    }
}
