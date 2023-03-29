package org.jetbrains.research.testgenie.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import org.jetbrains.research.testgenie.pipeline.Pipeline
import org.jetbrains.research.testgenie.helpers.generateMethodDescriptor
import org.jetbrains.research.testgenie.services.RunnerService

/**
 * This class contains all the logic related to generating tests for a method.
 * No actual generation happens in this class, rather it is responsible for displaying the action option to the user when it is available,
 *   getting the information about the selected method and passing it to (EvoSuite) Pipeline.
 */
class GenerateTestsActionMethod : AnAction() {
    private val logger: Logger = Logger.getInstance(this.javaClass)

    /**
     * Creates and calls (EvoSuite) Pipeline to generate tests for a method when the action is invoked.
     *
     * @param e an action event that contains useful information and corresponds to the action invoked by the user
     */
    override fun actionPerformed(e: AnActionEvent) {
        val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE) ?: return
        val caret: Caret = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret ?: return

        val project = e.project ?: return
        val runnerService = project.service<RunnerService>()
        if (!runnerService.verify(psiFile)) return

        val psiMethod: PsiMethod = getSurroundingMethod(psiFile, caret) ?: return
        val methodDescriptor = generateMethodDescriptor(psiMethod)

        logger.info("Selected method is $methodDescriptor")

        val doc: Document = PsiDocumentManager.getInstance(psiFile.project).getDocument(psiFile) ?: return
        val cacheStartLine: Int = doc.getLineNumber(psiMethod.startOffset)
        val cacheEndLine: Int = doc.getLineNumber(psiMethod.endOffset)
        logger.info("Selected method is on lines $cacheStartLine to $cacheEndLine")
        val linesToInvalidateFromCache = calculateLinesToInvalidate(psiFile)

        val evoSuitePipeline: Pipeline = createEvoSuitePipeline(e) ?: return
        evoSuitePipeline
            .forMethod(methodDescriptor)
            .withCacheLines(cacheStartLine, cacheEndLine)
            .invalidateCache(linesToInvalidateFromCache)
            .runTestGeneration()
    }

    /**
     * Makes the action visible only if a method has been selected.
     * It also updates the action name depending on which method has been selected.
     *
     * @param e an action event that contains useful information and corresponds to the action invoked by the user
     */
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = false

        val caret: Caret = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret ?: return
        val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE) ?: return

        val psiMethod: PsiMethod = getSurroundingMethod(psiFile, caret) ?: return

        e.presentation.isEnabledAndVisible = true
        e.presentation.text = "Generate Tests For ${getMethodDisplayName(psiMethod)}"
    }
}
