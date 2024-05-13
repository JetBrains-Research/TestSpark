package org.jetbrains.research.testspark.tools.template

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.jetbrains.research.testspark.actions.controllers.TestGenerationController
import org.jetbrains.research.testspark.display.TestSparkDisplayFactory

/**
 * Represents a tool that can generate tests.
 */
interface Tool {
    val name: String

    /**
     * Generates tests for a given class.
     *
     * @param project The project context.
     * @param psiFile The PsiFile object representing the class.
     * @param caretOffset The offset of the caret within the class.
     * @param fileUrl The URL of the file.
     * @param testSamplesCode The sample code for generating the tests.
     *
     * @see Project
     * @see PsiFile
     */
    fun generateTestsForClass(project: Project, psiFile: PsiFile, caretOffset: Int, fileUrl: String?, testSamplesCode: String, testGenerationController: TestGenerationController, testSparkDisplayFactory: TestSparkDisplayFactory)

    /**
     * Generates test cases for a given method.
     *
     * @param project The current project.
     * @param psiFile The PSI file object representing the source file.
     * @param caretOffset The offset of the caret position.
     * @param fileUrl The URL of the file where the method is defined (optional).
     * @param testSamplesCode The code snippets for test samples (optional).
     */
    fun generateTestsForMethod(project: Project, psiFile: PsiFile, caretOffset: Int, fileUrl: String?, testSamplesCode: String, testGenerationController: TestGenerationController, testSparkDisplayFactory: TestSparkDisplayFactory)

    /**
     * Generates tests for a specific line in a project.
     *
     * @param project The project in which the line belongs.
     * @param psiFile The PSI file where the line is located.
     * @param caretOffset The offset of the caret in the line.
     * @param fileUrl The URL of the file.
     * @param testSamplesCode The code containing the test samples.
     *
     * @see Project
     * @see PsiFile
     */
    fun generateTestsForLine(project: Project, psiFile: PsiFile, caretOffset: Int, fileUrl: String?, testSamplesCode: String, testGenerationController: TestGenerationController, testSparkDisplayFactory: TestSparkDisplayFactory)
}
