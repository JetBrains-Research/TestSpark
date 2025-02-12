package org.jetbrains.research.testspark.langwrappers.strategies

import com.intellij.psi.PsiFile

/**
Direct implementor for the Java and Kotlin PsiWrappers
 */
class DefaultClassTextExtractor : ClassTextExtractor {

    override fun extract(
        file: PsiFile,
        classText: String,
        packagePattern: Regex,
        importPattern: Regex,
    ): String {
        var fullText = ""
        val fileText = file.text

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
