package org.jetbrains.research.testspark.tools.evosuite

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import org.jetbrains.research.testspark.data.CodeType
import org.jetbrains.research.testspark.data.FragmentToTestData
import org.jetbrains.research.testspark.helpers.generateMethodDescriptor
import org.jetbrains.research.testspark.helpers.getSurroundingLine
import org.jetbrains.research.testspark.helpers.getSurroundingMethod
import org.jetbrains.research.testspark.services.SettingsProjectService
import org.jetbrains.research.testspark.tools.Pipeline
import org.jetbrains.research.testspark.tools.evosuite.generation.EvoSuiteProcessManager
import org.jetbrains.research.testspark.tools.template.Tool
import java.io.File

/**
 * Represents the EvoSuite class, which is a tool used to generate tests for Java code.
 * Implements the Tool interface.
 *
 * @param name The name of the EvoSuite tool.
 */
class EvoSuite(override val name: String = "EvoSuite") : Tool {
    private val log = Logger.getInstance(this::class.java)

    private fun getEvoSuiteProcessManager(project: Project): EvoSuiteProcessManager {
        val projectClassPath: String = ProjectRootManager.getInstance(project).contentRoots.first().path
        val settingsProjectState = project.service<SettingsProjectService>().state
        val buildPath = "$projectClassPath${File.separatorChar}${settingsProjectState.buildPath}"
        return EvoSuiteProcessManager(project, buildPath)
    }

    override fun generateTestsForClass(project: Project, psiFile: PsiFile, caret: Caret, testSamplesCode: String) {
        log.info("Starting tests generation for class by EvoSuite")
        createPipeline(project, psiFile, caret).runTestGeneration(getEvoSuiteProcessManager(project), FragmentToTestData(CodeType.CLASS))
    }

    override fun generateTestsForMethod(project: Project, psiFile: PsiFile, caret: Caret, testSamplesCode: String) {
        log.info("Starting tests generation for method by EvoSuite")
        val psiMethod: PsiMethod = getSurroundingMethod(psiFile, caret)!!
        createPipeline(project, psiFile, caret).runTestGeneration(getEvoSuiteProcessManager(project), FragmentToTestData(CodeType.METHOD, generateMethodDescriptor(psiMethod)))
    }

    override fun generateTestsForLine(project: Project, psiFile: PsiFile, caret: Caret, testSamplesCode: String) {
        log.info("Starting tests generation for line by EvoSuite")
        val selectedLine: Int = getSurroundingLine(psiFile, caret)?.plus(1)!!
        createPipeline(project, psiFile, caret).runTestGeneration(getEvoSuiteProcessManager(project), FragmentToTestData(CodeType.LINE, selectedLine))
    }

    private fun createPipeline(project: Project, psiFile: PsiFile, caret: Caret): Pipeline {
        val projectClassPath: String = ProjectRootManager.getInstance(project).contentRoots.first().path

        val settingsProjectState = project.service<SettingsProjectService>().state
        val packageName = "$projectClassPath/${settingsProjectState.buildPath}"

        return Pipeline(project, psiFile, caret, packageName)
    }
}
