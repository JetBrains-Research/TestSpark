package org.jetbrains.research.testspark.tools.llm

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import org.jetbrains.research.testspark.TestSparkBundle
import org.jetbrains.research.testspark.actions.createLLMPipeline
import org.jetbrains.research.testspark.actions.getSurroundingClass
import org.jetbrains.research.testspark.actions.getSurroundingLine
import org.jetbrains.research.testspark.actions.getSurroundingMethod
import org.jetbrains.research.testspark.data.CodeType
import org.jetbrains.research.testspark.data.FragmentToTestDada
import org.jetbrains.research.testspark.editor.Workspace
import org.jetbrains.research.testspark.helpers.generateMethodDescriptor
import org.jetbrains.research.testspark.services.LLMChatService
import org.jetbrains.research.testspark.tools.isPromptLengthWithinLimit
import org.jetbrains.research.testspark.tools.llm.error.LLMErrorManager
import org.jetbrains.research.testspark.tools.llm.generation.LLMProcessManager
import org.jetbrains.research.testspark.tools.llm.generation.PromptManager
import org.jetbrains.research.testspark.tools.template.Tool

/**
 * The Llm class represents a tool called "Llm" that is used to generate tests for Java code.
 *
 * @param name The name of the tool. Default value is "Llm".
 */
class Llm(override val name: String = "Llm") : Tool {
    private val log = Logger.getInstance(this::class.java)

    private val llmErrorManager: LLMErrorManager = LLMErrorManager()

    private fun getLLMProcessManager(e: AnActionEvent, codeType: FragmentToTestDada): LLMProcessManager {
        val project: Project = e.project!!

        val classesToTest = mutableListOf<PsiClass>()
        // check if cut has any none java super class
        val maxPolymorphismDepth = SettingsArguments.maxPolyDepth(project)

        val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE)!!
        val caret: Caret = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret!!
        val cutPsiClass: PsiClass = getSurroundingClass(psiFile, caret)

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

        var prompt: String
        while (true) {
            prompt = when (codeType.type!!) {
                CodeType.CLASS -> PromptManager(project, classesToTest[0], classesToTest).generatePromptForClass()
                CodeType.METHOD ->
                    PromptManager(
                        project,
                        classesToTest[0],
                        classesToTest
                    ).generatePromptForMethod(codeType.objectDescription)

                CodeType.LINE -> PromptManager(
                    project,
                    classesToTest[0],
                    classesToTest
                ).generatePromptForLine(codeType.objectIndex)
            }

            // Too big prompt processing
            if (!isPromptLengthWithinLimit(prompt)) {
                // depth of polymorphism reducing
                if (SettingsArguments.maxPolyDepth(project) > 1) {
                    project.service<Workspace>().testGenerationData.polyDepthReducing++
                    log.info("polymorphism depth is: ${SettingsArguments.maxPolyDepth(project)}")
                    continue
                }

                // depth of input params reducing
                if (SettingsArguments.maxInputParamsDepth(project) > 1) {
                    project.service<Workspace>().testGenerationData.inputParamsDepthReducing++
                    log.info("input params depth is: ${SettingsArguments.maxPolyDepth(project)}")
                    continue
                }
            }
            break
        }

        if ((project.service<Workspace>().testGenerationData.polyDepthReducing != 0 || project.service<Workspace>().testGenerationData.inputParamsDepthReducing != 0) &&
            isPromptLengthWithinLimit(prompt)
        ) {
            llmErrorManager.warningProcess(
                TestSparkBundle.message("promptReduction") + "\n" +
                    "Maximum depth of polymorphism is ${SettingsArguments.maxPolyDepth(project)}.\n" +
                    "Maximum depth for input parameters is ${SettingsArguments.maxInputParamsDepth(project)}.",
                project,
            )
        }

        log.info("Prompt is:\n$prompt")

        return LLMProcessManager(project, prompt)
    }

    /**
     * Generates tests for a given class.
     *
     * @param e the AnActionEvent object containing information about the action event
     * @throws IllegalArgumentException if the project in the AnActionEvent object is null
     */
    override fun generateTestsForClass(e: AnActionEvent) {
        if (!e.project!!.service<LLMChatService>()
            .isCorrectToken(e.project!!)
        ) return
        val codeType = FragmentToTestDada(CodeType.CLASS)
        createLLMPipeline(e).runTestGeneration(getLLMProcessManager(e, codeType), codeType)
    }

    /**
     * Generates tests for a given method.
     *
     * @param e The AnActionEvent that triggered the method generation.
     * @throws IllegalStateException if the project or the surrounding method is null.
     */
    override fun generateTestsForMethod(e: AnActionEvent) {
        if (!e.project!!.service<LLMChatService>()
            .isCorrectToken(e.project!!)
        ) return
        val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE)!!
        val caret: Caret = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret!!
        val psiMethod: PsiMethod = getSurroundingMethod(psiFile, caret)!!
        val codeType = FragmentToTestDada(CodeType.METHOD, generateMethodDescriptor(psiMethod))
        createLLMPipeline(e).runTestGeneration(getLLMProcessManager(e, codeType), codeType)
    }

    /**
     * Generates tests for a specific line of code.
     *
     * @param e The AnActionEvent that triggered the generation of tests.
     */
    override fun generateTestsForLine(e: AnActionEvent) {
        if (!e.project!!.service<LLMChatService>()
            .isCorrectToken(e.project!!)
        ) return
        val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE)!!
        val caret: Caret = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret!!
        val selectedLine: Int = getSurroundingLine(psiFile, caret)?.plus(1)!!
        val codeType = FragmentToTestDada(CodeType.LINE, selectedLine)
        createLLMPipeline(e).runTestGeneration(getLLMProcessManager(e, codeType), codeType)
    }
}
