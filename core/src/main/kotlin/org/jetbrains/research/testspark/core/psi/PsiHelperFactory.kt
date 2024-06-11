package org.jetbrains.research.testspark.core.psi

import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile

object PsiHelperFactory {
    fun getPsiHelper(psiFile: PsiFile?): PsiHelper? {
        val log = Logger.getInstance(this::class.java)
        return when (psiFile) {
            is PsiJavaFile -> {
                log.info("Getting psi helper for Java")
                JavaPsiHelper(psiFile)
            }

            is KtFile -> {
                log.info("Getting psi helper for Kotlin")
                KotlinPsiHelper(psiFile)
            }

            else -> {
                log.info("Psi helper for ${psiFile?.fileType} does not exist")
                null
            }
        }
    }
}
