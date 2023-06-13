package org.jetbrains.research.testgenie.tools.toolImpls

import com.intellij.openapi.actionSystem.AnActionEvent
import org.jetbrains.research.testgenie.actions.createLLMPipeline
import org.jetbrains.research.testgenie.tools.Tool
import org.jetbrains.research.testgenie.tools.llm.SettingsArguments
import org.jetbrains.research.testgenie.tools.llm.error.LLMErrorManager

class Llm(override val name: String = "Llm") : Tool {
    override fun generateTestsForClass(e: AnActionEvent) {
        val project = e.project ?: return
        if (!SettingsArguments.isTokenSet()) {
            LLMErrorManager().displayMissingTokenNotification(project)
            return
        }
        val llmPipeline: org.jetbrains.research.testgenie.tools.llm.Pipeline = createLLMPipeline(e) ?: return
        llmPipeline.forClass().runTestGeneration()
    }

    override fun generateTestsForMethod(e: AnActionEvent) {
        TODO("Not yet implemented")
    }

    override fun generateTestsForLine(e: AnActionEvent) {
        TODO("Not yet implemented")
    }
}
