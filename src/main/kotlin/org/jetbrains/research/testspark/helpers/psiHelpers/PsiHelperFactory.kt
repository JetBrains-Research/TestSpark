package org.jetbrains.research.testspark.helpers.psiHelpers

import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile

object PsiHelperFactory {
    fun getPsiHelper(psiFile: PsiFile?): PsiHelper? {
        val log = Logger.getInstance(this::class.java)
        return when (psiFile) {
            is PsiJavaFile -> {
                log.info("Getting psi helper for JAVA")
                JavaPsiHelper(psiFile)
            }
            // TODO implement KotlinPsiHelper class
            // is KtFile -> KotlinPsiHelper(psiFile)
            else -> null
        }
    }
}
