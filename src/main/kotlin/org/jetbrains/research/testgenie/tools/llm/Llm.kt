package org.jetbrains.research.testgenie.tools.llm

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import org.jetbrains.research.testgenie.TestGenieBundle
import org.jetbrains.research.testgenie.actions.createLLMPipeline
import org.jetbrains.research.testgenie.actions.getInterestingPsiClasses
import org.jetbrains.research.testgenie.actions.getPolymorphismRelations
import org.jetbrains.research.testgenie.actions.getSurroundingClass
import org.jetbrains.research.testgenie.actions.getSurroundingLine
import org.jetbrains.research.testgenie.actions.getSurroundingMethod
import org.jetbrains.research.testgenie.data.CodeType
import org.jetbrains.research.testgenie.data.CodeTypeAndAdditionData
import org.jetbrains.research.testgenie.helpers.generateMethodDescriptor
import org.jetbrains.research.testgenie.tools.llm.error.LLMErrorManager
import org.jetbrains.research.testgenie.tools.llm.generation.LLMProcessManager
import org.jetbrains.research.testgenie.tools.llm.generation.PromptManager
import org.jetbrains.research.testgenie.tools.template.Tool

class Llm(override val name: String = "Llm") : Tool {
    private val llmErrorManager: LLMErrorManager = LLMErrorManager()

    private fun getLLMProcessManager(e: AnActionEvent, codeType: CodeTypeAndAdditionData): LLMProcessManager {
        val project: Project = e.project!!

        val classesToTest = mutableListOf<PsiClass>()
        // check if cut has any none java super class
        val maxPolymorphismDepth = SettingsArguments.maxPolyDepth()

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

        val interestingPsiClasses = getInterestingPsiClasses(cutPsiClass, classesToTest)
        val polymorphismRelations = getPolymorphismRelations(project, interestingPsiClasses, cutPsiClass)

        val prompt = when (codeType.type!!) {
            CodeType.CLASS -> PromptManager(classesToTest[0], classesToTest, interestingPsiClasses, polymorphismRelations).generatePromptForClass()
            CodeType.METHOD ->
                PromptManager(classesToTest[0], classesToTest, interestingPsiClasses, polymorphismRelations).generatePromptForMethod(codeType.objectDescription)

            CodeType.LINE -> PromptManager(classesToTest[0], classesToTest, interestingPsiClasses, polymorphismRelations).generatePromptForLine(codeType.objectIndex)
        }

        return LLMProcessManager(project, prompt)
    }

    private fun isCorrectToken(project: Project): Boolean {
        if (!SettingsArguments.isTokenSet()) {
            llmErrorManager.errorProcess(TestGenieBundle.message("missingToken"), project)
            return false
        }
        return true
    }

    override fun generateTestsForClass(e: AnActionEvent) {
        if (!isCorrectToken(e.project!!)) return
        val codeType = CodeTypeAndAdditionData(CodeType.CLASS)
        createLLMPipeline(e).runTestGeneration(getLLMProcessManager(e, codeType), codeType)
    }

    override fun generateTestsForMethod(e: AnActionEvent) {
        if (!isCorrectToken(e.project!!)) return
        val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE)!!
        val caret: Caret = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret!!
        val psiMethod: PsiMethod = getSurroundingMethod(psiFile, caret)!!
        val codeType = CodeTypeAndAdditionData(CodeType.METHOD, generateMethodDescriptor(psiMethod))
        createLLMPipeline(e).runTestGeneration(getLLMProcessManager(e, codeType), codeType)
    }

    override fun generateTestsForLine(e: AnActionEvent) {
        if (!isCorrectToken(e.project!!)) return
        val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE)!!
        val caret: Caret = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret!!
        val selectedLine: Int = getSurroundingLine(psiFile, caret)?.plus(1)!!
        val codeType = CodeTypeAndAdditionData(CodeType.LINE, selectedLine)
        createLLMPipeline(e).runTestGeneration(getLLMProcessManager(e, codeType), codeType)
    }
}
