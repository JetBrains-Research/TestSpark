package org.jetbrains.research.testspark.tools.evosuite

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import org.jetbrains.research.testspark.actions.createPipeline
import org.jetbrains.research.testspark.actions.getSurroundingLine
import org.jetbrains.research.testspark.actions.getSurroundingMethod
import org.jetbrains.research.testspark.data.Level
import org.jetbrains.research.testspark.data.FragmentToTestData
import org.jetbrains.research.testspark.helpers.generateMethodDescriptor
import org.jetbrains.research.testspark.services.SettingsProjectService
import org.jetbrains.research.testspark.tools.evosuite.generation.EvoSuiteProcessManager
import org.jetbrains.research.testspark.tools.template.Tool

/**
 * Represents the EvoSuite class, which is a tool used to generate tests for Java code.
 * Implements the Tool interface.
 *
 * @param name The name of the EvoSuite tool.
 */
class EvoSuite(override val name: String = "EvoSuite") : Tool {
    private val log = Logger.getInstance(this::class.java)

    private fun getEvoSuiteProcessManager(e: AnActionEvent): EvoSuiteProcessManager {
        val project: Project = e.project!!
        val projectClassPath: String = ProjectRootManager.getInstance(project).contentRoots.first().path
        val settingsProjectState = project.service<SettingsProjectService>().state
        val buildPath = "$projectClassPath/${settingsProjectState.buildPath}"
        return EvoSuiteProcessManager(project, buildPath)
    }

    override fun generateTestsForClass(e: AnActionEvent) {
        log.info("Starting tests generation for class by EvoSuite")
        createPipeline(e).runTestGeneration(getEvoSuiteProcessManager(e), FragmentToTestData(Level.CLASS))
    }

    override fun generateTestsForMethod(e: AnActionEvent) {
        log.info("Starting tests generation for method by EvoSuite")
        val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE)!!
        val caret: Caret = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret!!
        val psiMethod: PsiMethod = getSurroundingMethod(psiFile, caret)!!
        createPipeline(e).runTestGeneration(getEvoSuiteProcessManager(e), FragmentToTestData(Level.METHOD, generateMethodDescriptor(psiMethod)))
    }

    override fun generateTestsForLine(e: AnActionEvent) {
        log.info("Starting tests generation for line by EvoSuite")
        val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE)!!
        val caret: Caret = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret!!
        val selectedLine: Int = getSurroundingLine(psiFile, caret)?.plus(1)!!
        createPipeline(e).runTestGeneration(getEvoSuiteProcessManager(e), FragmentToTestData(Level.LINE, selectedLine))
    }
}
