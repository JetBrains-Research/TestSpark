package org.jetbrains.research.testgenie.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.psi.PsiFile
import org.jetbrains.research.testgenie.llm.Pipeline
import org.jetbrains.research.testgenie.services.RunnerService

class GenerateTestsForClassLLM : AnAction() {
    /**
     * Creates and calls (GPT) Pipeline to generate tests for a class when the action is invoked.
     *
     * @param e an action event that contains useful information and corresponds to the action invoked by the user
     */
    override fun actionPerformed(e: AnActionEvent) {
        val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE) ?: return

        val project = e.project ?: return

        val runnerService = project.service<RunnerService>()
//        if (!runnerService.verify(psiFile)) return

        val llmPipeline: Pipeline= createGPTPipeline(e) ?: return
        llmPipeline.forClass()
        TODO("Not yet implemented")
    }
}