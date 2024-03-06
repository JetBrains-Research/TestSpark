package org.jetbrains.research.testspark.tools.llm

import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import org.jetbrains.research.testspark.data.CodeType
import org.jetbrains.research.testspark.data.FragmentToTestData
import org.jetbrains.research.testspark.helpers.generateMethodDescriptor
import org.jetbrains.research.testspark.helpers.getSurroundingClass
import org.jetbrains.research.testspark.helpers.getSurroundingLine
import org.jetbrains.research.testspark.helpers.getSurroundingMethod
import org.jetbrains.research.testspark.services.LLMChatService
import org.jetbrains.research.testspark.tools.Pipeline
import org.jetbrains.research.testspark.tools.llm.generation.LLMProcessManager
import org.jetbrains.research.testspark.tools.llm.generation.PromptManager
import org.jetbrains.research.testspark.tools.template.Tool

/**
 * The Llm class represents a tool called "Llm" that is used to generate tests for Java code.
 *
 * @param name The name of the tool. Default value is "Llm".
 */
class Llm(override val name: String = "LLM") : Tool {

    /**
     * Returns an instance of LLMProcessManager based on the provided parameters.
     *
     * @param project The current project.
     * @param psiFile The PSI file containing the code.
     * @param caret The caret position in the editor.
     * @param codeType The type of code fragment being tested.
     * @param testSamplesCode The code samples used for testing.
     * @return An instance of LLMProcessManager.
     */
    private fun getLLMProcessManager(project: Project, psiFile: PsiFile, caret: Caret, codeType: FragmentToTestData, testSamplesCode: String): LLMProcessManager {
        val classesToTest = mutableListOf<PsiClass>()
        // check if cut has any none java super class
        val maxPolymorphismDepth = SettingsArguments.maxPolyDepth(project)

        val cutPsiClass: PsiClass = getSurroundingClass(psiFile, caret)!!

        var currentPsiClass = cutPsiClass
        for (index in 0 until maxPolymorphismDepth) {
            if (!classesToTest.contains(currentPsiClass)) {
                classesToTest.add(currentPsiClass)
            }

            if (currentPsiClass.superClass == null ||
                currentPsiClass.superClass!!.qualifiedName == null ||
                currentPsiClass.superClass!!.qualifiedName!!.startsWith("java.")
            ) {
                break
            }
            currentPsiClass = currentPsiClass.superClass!!
        }
        return LLMProcessManager(
            project,
            PromptManager(project, classesToTest[0], classesToTest),
            testSamplesCode,
        )
    }

    /**
     * Generates tests for a given class in the project.
     *
     * @param project The project where the class is located.
     * @param psiFile The PSI file representing the class.
     * @param caret The caret position in the class file.
     * @param fileUrl The URL of the class file.
     * @param testSamplesCode The code samples to be used for generating tests.
     */
    override fun generateTestsForClass(project: Project, psiFile: PsiFile, caret: Caret, fileUrl: String?, testSamplesCode: String) {
        if (project.service<LLMChatService>().isCorrectToken(project)) {
            return
        }
        val codeType = FragmentToTestData(CodeType.CLASS)
        createLLMPipeline(project, psiFile, caret, fileUrl).runTestGeneration(getLLMProcessManager(project, psiFile, caret, codeType, testSamplesCode), codeType)
    }

    /**
     * Generates tests for a given method in the project.
     *
     * @param project the current project
     * @param psiFile the PSI file containing the method
     * @param caret the caret position in the file
     * @param fileUrl the URL of the file
     * @param testSamplesCode the code for test samples
     */
    override fun generateTestsForMethod(project: Project, psiFile: PsiFile, caret: Caret, fileUrl: String?, testSamplesCode: String) {
        if (!project.service<LLMChatService>().isCorrectToken(project)) {
            return
        }
        val psiMethod: PsiMethod = getSurroundingMethod(psiFile, caret)!!
        val codeType = FragmentToTestData(CodeType.METHOD, generateMethodDescriptor(psiMethod))
        createLLMPipeline(project, psiFile, caret, fileUrl).runTestGeneration(getLLMProcessManager(project, psiFile, caret, codeType, testSamplesCode), codeType)
    }

    /**
     * Generates tests for a specific line of code.
     *
     * @param project The project in which the code is located.
     * @param psiFile The PSI file containing the code.
     * @param caret The caret position in the code editor.
     * @param fileUrl The URL of the file containing the code.
     * @param testSamplesCode The code representing test samples.
     */
    override fun generateTestsForLine(project: Project, psiFile: PsiFile, caret: Caret, fileUrl: String?, testSamplesCode: String) {
        if (!project.service<LLMChatService>().isCorrectToken(project)) {
            return
        }
        val selectedLine: Int = getSurroundingLine(psiFile, caret)?.plus(1)!!
        val codeType = FragmentToTestData(CodeType.LINE, selectedLine)
        createLLMPipeline(project, psiFile, caret, fileUrl).runTestGeneration(getLLMProcessManager(project, psiFile, caret, codeType, testSamplesCode), codeType)
    }

    /**
     * Creates a Low-Level Management (LLM) pipeline.
     *
     * @param project The project in which the pipeline belongs.
     * @param psiFile The PSI file in which the pipeline is created.
     * @param caret The caret position in the PSI file.
     * @param fileUrl The URL of the file.
     *
     * @return The created LLM pipeline.
     */
    private fun createLLMPipeline(project: Project, psiFile: PsiFile, caret: Caret, fileUrl: String?): Pipeline {
        val cutPsiClass: PsiClass = getSurroundingClass(psiFile, caret)!!

        val packageList = cutPsiClass.qualifiedName.toString().split(".").toMutableList()
        packageList.removeLast()

        val packageName = packageList.joinToString(".")

        return Pipeline(project, psiFile, caret, fileUrl, packageName)
    }
}
