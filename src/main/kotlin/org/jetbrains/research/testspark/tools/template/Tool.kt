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
     * Generates tests for the given class.
     *
     * @param e The AnActionEvent representing the event when the method is invoked.
     */
    fun generateTestsForClass(project: Project, psiFile: PsiFile, caret: Caret, testSamplesCode: String)

    /**
     * Generates tests for a given method.
     *
     * @param e the AnActionEvent object representing the action event
     */
    fun generateTestsForMethod(project: Project, psiFile: PsiFile, caret: Caret, testSamplesCode: String)

    /**
     * Generates test cases for a given line of code based on the provided AnActionEvent.
     *
     * @param e The AnActionEvent representing the context in which the method is called.
     */
    fun generateTestsForLine(project: Project, psiFile: PsiFile, caret: Caret, testSamplesCode: String)
}
