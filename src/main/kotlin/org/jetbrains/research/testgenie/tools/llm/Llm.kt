package org.jetbrains.research.testgenie.tools.llm

import com.intellij.openapi.actionSystem.AnActionEvent
import org.jetbrains.research.testgenie.TestGenieBundle
import org.jetbrains.research.testgenie.actions.createLLMPipeline
import org.jetbrains.research.testgenie.tools.Tool
import org.jetbrains.research.testgenie.tools.llm.error.LLMErrorManager

class Llm(override val name: String = "Llm") : Tool {
    private val llmErrorManager: LLMErrorManager = LLMErrorManager()

    override fun generateTestsForClass(e: AnActionEvent) {
        val project = e.project ?: return
        if (!SettingsArguments.isTokenSet()) {
            llmErrorManager.errorProcess(TestGenieBundle.message("missingToken"), project)
            return
        }
        val llmPipeline: Pipeline = createLLMPipeline(e)
        llmPipeline.forClass().runTestGeneration()
    }

    override fun generateTestsForMethod(e: AnActionEvent) {
        TODO("Not yet implemented")
    }

    override fun generateTestsForLine(e: AnActionEvent) {
        TODO("Not yet implemented")
    }
}
