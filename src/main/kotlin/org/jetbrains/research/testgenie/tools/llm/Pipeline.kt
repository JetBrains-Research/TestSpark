package org.jetbrains.research.testgenie.tools.llm

import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.openapi.module.Module
import com.intellij.psi.PsiClass
import org.jetbrains.research.testgenie.TestGenieBundle
import org.jetbrains.research.testgenie.editor.Workspace
import org.jetbrains.research.testgenie.tools.ProjectBuilder
import org.jetbrains.research.testgenie.tools.llm.generation.LLMProcessManager
import org.jetbrains.research.testgenie.tools.llm.generation.PromptManager
import java.io.File
import java.util.UUID

private var prompt = ""

class Pipeline(
    private val project: Project,
    projectClassPath: String,
    interestingPsiClasses: Set<PsiClass>,
    classesToTest: MutableList<PsiClass>,
    private val cutModule: Module,
    private val packageName: String,
    polymorphismRelations: MutableMap<PsiClass, MutableList<PsiClass>>,
    modTs: Long,
    private val fileUrl: String,
    private val classFQN: String,
) {
    private val sep = File.separatorChar

    private val cut = classesToTest[0]

    private val id = UUID.randomUUID().toString()
    private val testResultDirectory = "${FileUtilRt.getTempDirectory()}${sep}testGenieResults$sep"
    private val testResultName = "test_gen_result_$id"

    private val resultPath = "$testResultDirectory$testResultName"

    // TODO move all interactions with Workspace to Manager
    var key = Workspace.TestJobInfo(fileUrl, classFQN, modTs, testResultName, projectClassPath)

    private val promptManager = PromptManager(cut, classesToTest, interestingPsiClasses, polymorphismRelations)

    private val processManager = LLMProcessManager(project, projectClassPath)

    fun forClass(): Pipeline {
        prompt = promptManager.generatePrompt()
        return this
    }

    fun runTestGeneration() {
        // TODO move all interactions with Workspace to Manager
        val workspace = project.service<Workspace>()
        workspace.testGenerationData.clear()
        workspace.testGenerationData.resultName = testResultName
        workspace.addPendingResult(testResultName, key)

        val projectBuilder = ProjectBuilder(project)

        ProgressManager.getInstance()
            .run(object : Task.Backgroundable(project, TestGenieBundle.message("testGenerationMessage")) {
                override fun run(indicator: ProgressIndicator) {
                    if (indicator.isCanceled) {
                        indicator.stop()
                        return
                    }

                    if (projectBuilder.runBuild(indicator)) {
                        processManager.runLLMTestGenerator(indicator, prompt, resultPath, packageName, cutModule, classFQN, fileUrl)
                    }
                }
            })
    }
}
