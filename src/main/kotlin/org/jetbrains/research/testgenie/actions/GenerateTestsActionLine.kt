package org.jetbrains.research.testgenie.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Caret
import com.intellij.psi.PsiFile
import org.jetbrains.research.testgenie.pipeline.Pipeline
import org.jetbrains.research.testgenie.services.RunnerService

/**
 * This class contains all the logic related to generating tests for a line.
 * No actual generation happens in this class, rather it is responsible for displaying the action option to the user when it is available,
 *   getting the information about the selected class and passing it to (EvoSuite) Pipeline.
 */
class GenerateTestsActionLine : AnAction() {
    private val logger: Logger = Logger.getInstance(this.javaClass)

    /**
     * Creates and calls (EvoSuite) Pipeline to generate tests for a line when the action is invoked.
     *
     * @param e an action event that contains useful information and corresponds to the action invoked by the user
     */
    override fun actionPerformed(e: AnActionEvent) {
        val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE) ?: return
        val caret: Caret = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret ?: return

        val selectedLine: Int = getSurroundingLine(psiFile, caret)?.plus(1)
            ?: return // lines in the editor and in EvoSuite are one-based

        val project = e.project ?: return
        val runnerService = project.service<RunnerService>()
        if (!runnerService.verify(psiFile)) return

        logger.info("Selected line is $selectedLine")

        val linesToInvalidateFromCache = calculateLinesToInvalidate(psiFile)

        val evoSuitePipeline: Pipeline = createEvoSuitePipeline(e) ?: return
        evoSuitePipeline
            .forLine(selectedLine)
            .withCacheLines(selectedLine - 1, selectedLine - 1)
            .invalidateCache(linesToInvalidateFromCache)
            .runTestGeneration()
    }

    /**
     * Makes the action visible only if a line has been selected.
     * It also updates the action name depending on which line has been selected.
     *
     * @param e an action event that contains useful information and corresponds to the action invoked by the user
     */
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = false

        val caret: Caret = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret ?: return
        val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE) ?: return

        val line: Int = getSurroundingLine(psiFile, caret)?.plus(1)
            ?: return // lines in the editor and in EvoSuite are one-based

        e.presentation.isEnabledAndVisible = true
        e.presentation.text = "Generate Tests For Line $line"
    }
}
