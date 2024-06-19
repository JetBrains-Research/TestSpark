package org.jetbrains.research.testspark.langwrappers

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiFile

object PsiHelperFactory {
    private val EP_NAME = ExtensionPointName.create<PsiHelper>("org.jetbrains.research.testspark.langwrappers.psiHelper")
    private val EP_NAME2 = ExtensionPointName.create<PsiHelperFactory>("org.jetbrains.research.testspark.langwrappers.psiHelper")

    fun getPsiHelper(psiFile: PsiFile): PsiHelper? {
        val language = psiFile.language.id
        for (helper in EP_NAME.extensionList) {
            if (helper.supportsLanguage(language)) {
                helper.initialize(psiFile)
                return helper
            }
        }
        return null
    }
}