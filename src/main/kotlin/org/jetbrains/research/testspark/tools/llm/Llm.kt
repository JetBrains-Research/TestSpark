package org.jetbrains.research.testspark.tools.llm

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import org.jetbrains.research.testspark.helpers.getSurroundingClass
import org.jetbrains.research.testspark.helpers.getSurroundingLine
import org.jetbrains.research.testspark.helpers.getSurroundingMethod
import org.jetbrains.research.testspark.data.CodeType
import org.jetbrains.research.testspark.data.FragmentToTestData
import org.jetbrains.research.testspark.helpers.generateMethodDescriptor
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

    private fun getLLMProcessManager(e: AnActionEvent, codeType: FragmentToTestData): LLMProcessManager {
        val project: Project = e.project!!

        val classesToTest = mutableListOf<PsiClass>()
        // check if cut has any none java super class
        val maxPolymorphismDepth = SettingsArguments.maxPolyDepth(project)

        val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE)!!
        val caret: Caret = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret!!
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
            // generate the prompt using Prompt manager
            PromptManager(project, classesToTest[0], classesToTest)
                .generatePrompt(
                    codeType,
                ),
        )
    }

    /**
     * Generates tests for a given class.
     *
     * @param e the AnActionEvent object containing information about the action event
     * @throws IllegalArgumentException if the project in the AnActionEvent object is null
     */
    override fun generateTestsForClass(e: AnActionEvent) {
        if (!e.project!!.service<LLMChatService>().isCorrectToken(e.project!!)) {
            return
        }
        val codeType = FragmentToTestData(CodeType.CLASS)
        createLLMPipeline(e).runTestGeneration(getLLMProcessManager(e, codeType), codeType)
    }

    /**
     * Generates tests for a given method.
     *
     * @param e The AnActionEvent that triggered the method generation.
     * @throws IllegalStateException if the project or the surrounding method is null.
     */
    override fun generateTestsForMethod(e: AnActionEvent) {
        if (!e.project!!.service<LLMChatService>().isCorrectToken(e.project!!)) {
            return
        }
        val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE)!!
        val caret: Caret = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret!!
        val psiMethod: PsiMethod = getSurroundingMethod(psiFile, caret)!!
        val codeType = FragmentToTestData(CodeType.METHOD, generateMethodDescriptor(psiMethod))
        createLLMPipeline(e).runTestGeneration(getLLMProcessManager(e, codeType), codeType)
    }

    /**
     * Generates tests for a specific line of code.
     *
     * @param e The AnActionEvent that triggered the generation of tests.
     */
    override fun generateTestsForLine(e: AnActionEvent) {
        if (!e.project!!.service<LLMChatService>().isCorrectToken(e.project!!)) {
            return
        }
        val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE)!!
        val caret: Caret = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret!!
        val selectedLine: Int = getSurroundingLine(psiFile, caret)?.plus(1)!!
        val codeType = FragmentToTestData(CodeType.LINE, selectedLine)
        createLLMPipeline(e).runTestGeneration(getLLMProcessManager(e, codeType), codeType)
    }

    private fun createLLMPipeline(e: AnActionEvent): Pipeline {
        val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE)!!
        val caret: Caret = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret!!

        val cutPsiClass: PsiClass = getSurroundingClass(psiFile, caret)!!

        val packageList = cutPsiClass.qualifiedName.toString().split(".").toMutableList()
        packageList.removeLast()

        val packageName = packageList.joinToString(".")

        return Pipeline(e, packageName)
    }
}
