package org.jetbrains.research.testspark.langwrappers.strategies

import com.intellij.psi.PsiFile
import org.jetbrains.research.testspark.core.utils.importPattern
import org.jetbrains.research.testspark.core.utils.packagePattern

/**
Direct implementor for the Java and Kotlin PsiWrappers
 */
class JavaKotlinFullTextExtractionStrategy {

    companion object {
        fun extract(containingFile: PsiFile, classText: String): String {
            var fullText = ""
            val fileText = containingFile.text

            // get package
            packagePattern.findAll(fileText, 0).map {
                it.groupValues[0]
            }.forEach {
                fullText += "$it\n\n"
            }

            // get imports
            importPattern.findAll(fileText, 0).map {
                it.groupValues[0]
            }.forEach {
                fullText += "$it\n"
            }

            // Add class code
            fullText += classText

            return fullText
        }
    }
}
