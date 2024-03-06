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
     * Generates tests for a given class.
     *
     * @param project The current project.
     * @param psiFile The PSI file containing the class.
     * @param caret The caret position.
     * @param testSamplesCode The code for test samples.
     */
    override fun generateTestsForClass(project: Project, psiFile: PsiFile, caret: Caret, testSamplesCode: String) {
        if (project.service<LLMChatService>().isCorrectToken(project)) {
            return
        }
        val codeType = FragmentToTestData(CodeType.CLASS)
        createLLMPipeline(project, psiFile, caret).runTestGeneration(getLLMProcessManager(project, psiFile, caret, codeType, testSamplesCode), codeType)
    }

    /**
     * Generates tests for a given method.
     *
     * @param project The current project.
     * @param psiFile The PSI file in which the method is located.
     * @param caret The position of the caret in the editor.
     * @param testSamplesCode The code of the test samples to be used.
     */
    override fun generateTestsForMethod(project: Project, psiFile: PsiFile, caret: Caret, testSamplesCode: String) {
        if (!project.service<LLMChatService>().isCorrectToken(project)) {
            return
        }
        val psiMethod: PsiMethod = getSurroundingMethod(psiFile, caret)!!
        val codeType = FragmentToTestData(CodeType.METHOD, generateMethodDescriptor(psiMethod))
        createLLMPipeline(project, psiFile, caret).runTestGeneration(getLLMProcessManager(project, psiFile, caret, codeType, testSamplesCode), codeType)
    }

    /**
     * Generates tests for a selected line of code in a given project and PsiFile.
     *
     * @param project The current project in which the code is located.
     * @param psiFile The PsiFile containing the code.
     * @param caret The caret representing the current selection.
     * @param testSamplesCode The test samples code to be used in the test generation process.
     */
    override fun generateTestsForLine(project: Project, psiFile: PsiFile, caret: Caret, testSamplesCode: String) {
        if (!project.service<LLMChatService>().isCorrectToken(project)) {
            return
        }
        val selectedLine: Int = getSurroundingLine(psiFile, caret)?.plus(1)!!
        val codeType = FragmentToTestData(CodeType.LINE, selectedLine)
        createLLMPipeline(project, psiFile, caret).runTestGeneration(getLLMProcessManager(project, psiFile, caret, codeType, testSamplesCode), codeType)
    }

    private fun createLLMPipeline(project: Project, psiFile: PsiFile, caret: Caret): Pipeline {
        val cutPsiClass: PsiClass = getSurroundingClass(psiFile, caret)!!

        val packageList = cutPsiClass.qualifiedName.toString().split(".").toMutableList()
        packageList.removeLast()

        val packageName = packageList.joinToString(".")

        return Pipeline(project, psiFile, caret, packageName)
    }
}
