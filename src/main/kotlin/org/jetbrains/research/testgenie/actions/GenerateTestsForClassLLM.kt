package org.jetbrains.research.testgenie.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiFile
import org.jetbrains.research.testgenie.llm.Pipeline

class GenerateTestsForClassLLM : AnAction() {
    /**
     * Creates and calls (GPT) Pipeline to generate tests for a class when the action is invoked.
     *
     * @param e an action event that contains useful information and corresponds to the action invoked by the user
     */
    override fun actionPerformed(e: AnActionEvent) {
        val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE) ?: return
        val project = e.project ?: return

        val llmPipeline: Pipeline = createGPTPipeline(e) ?: return
        llmPipeline.forClass().runTestGeneration()
        TODO("Next steps after sending requests to LLM")
    }
}