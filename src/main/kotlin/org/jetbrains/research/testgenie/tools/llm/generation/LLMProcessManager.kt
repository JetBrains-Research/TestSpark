package org.jetbrains.research.testgenie.tools.llm.generation;

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.CompilerModuleExtension
import org.jetbrains.research.testgenie.TestGenieBundle
import org.jetbrains.research.testgenie.services.SettingsProjectService
import org.jetbrains.research.testgenie.tools.llm.error.LLMErrorManager

class LLMProcessManager (
        private val project:Project,
        private val projectPath: String,
        private val projectClassPath: String,
        private val fileUrl: String
){

    private val settingsProjectState = project.service<SettingsProjectService>().state

    fun runLLMTestGenerator(
        indicator: ProgressIndicator,
        prompt: String,
        log: Logger,
    ){

        // update build path
        var buildPath = projectClassPath
        if (settingsProjectState.buildPath.isEmpty()) {
            // User did not set own path
            buildPath = ""
            for (module in ModuleManager.getInstance(project).modules) {
                val compilerOutputPath = CompilerModuleExtension.getInstance(module)?.compilerOutputPath
                compilerOutputPath?.let { buildPath += compilerOutputPath.path.plus(":") }
            }
        }
        indicator.text = TestGenieBundle.message("searchMessage")

        // Send request to LLM
        val generatedTestSuite = LLMRequest().request(prompt,indicator)

        // Check if response is not empty
        if (generatedTestSuite.isEmpty()) {
            LLMErrorManager.displayEmptyTests(project)
            return
        }
    }
}
