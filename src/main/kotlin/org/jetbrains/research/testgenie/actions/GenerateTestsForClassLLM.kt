package org.jetbrains.research.testgenie.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.jetbrains.research.testgenie.llm.LLMErrorManager
import org.jetbrains.research.testgenie.llm.Pipeline
import org.jetbrains.research.testgenie.llm.SettingsArguments

class GenerateTestsForClassLLM : AnAction() {
    /**
     * Creates and calls (GPT) Pipeline to generate tests for a class when the action is invoked.
     *
     * @param e an action event that contains useful information and corresponds to the action invoked by the user
     */
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        if(!SettingsArguments.isTokenSet()){
            LLMErrorManager().displayMissingTokenNotification(project)
            return
        }

        val llmPipeline: Pipeline = createGPTPipeline(e) ?: return
        llmPipeline.forClass().runTestGeneration()
        TODO("Next steps after collecting tests")
    }
}