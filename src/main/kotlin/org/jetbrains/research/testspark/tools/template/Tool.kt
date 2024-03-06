package org.jetbrains.research.testspark.tools.template

import com.intellij.openapi.editor.Caret
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

/**
 * Represents a tool that can generate tests.
 */
interface Tool {
    val name: String

    /**
     * Generates tests for a given class.
     *
     * @param project the current project
     * @param psiFile the PsiFile containing the class
     * @param caret the Caret position in the editor
     * @param fileUrl the URL of the file
     * @param testSamplesCode the sample code used for generating tests
     */
    fun generateTestsForClass(project: Project, psiFile: PsiFile, caret: Caret, fileUrl: String?, testSamplesCode: String)

    /**
     * Generates test cases for a given method.
     *
     * @param project the current project
     * @param psiFile the PSI file containing the method
     * @param caret the caret position within the method
     * @param fileUrl the URL of the file containing the method
     * @param testSamplesCode the code snippet representing test samples
     */
    fun generateTestsForMethod(project: Project, psiFile: PsiFile, caret: Caret, fileUrl: String?, testSamplesCode: String)

    /**
     * Generates tests for a given line in a PSI file.
     *
     * @param project The current project.
     * @param psiFile The PSI file containing the line.
     * @param caret The caret position on the line.
     * @param fileUrl The URL of the file containing the PSI file.
     * @param testSamplesCode The code for the test samples.
     */
    fun generateTestsForLine(project: Project, psiFile: PsiFile, caret: Caret, fileUrl: String?, testSamplesCode: String)
}
