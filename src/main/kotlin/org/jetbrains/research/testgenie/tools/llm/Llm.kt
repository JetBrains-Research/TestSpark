package org.jetbrains.research.testgenie.tools.llm

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import org.jetbrains.research.testgenie.TestGenieBundle
import org.jetbrains.research.testgenie.actions.createLLMPipeline
import org.jetbrains.research.testgenie.actions.getInterestingPsiClasses
import org.jetbrains.research.testgenie.actions.getPolymorphismRelations
import org.jetbrains.research.testgenie.actions.getSurroundingClass
import org.jetbrains.research.testgenie.data.CodeType
import org.jetbrains.research.testgenie.data.CodeTypeAndAdditionData
import org.jetbrains.research.testgenie.tools.template.Tool
import org.jetbrains.research.testgenie.tools.llm.error.LLMErrorManager
import org.jetbrains.research.testgenie.tools.Pipeline
import org.jetbrains.research.testgenie.tools.llm.generation.LLMProcessManager

class Llm(override val name: String = "Llm") : Tool {
    private val llmErrorManager: LLMErrorManager = LLMErrorManager()

    private fun getLLMProcessManager(e: AnActionEvent): LLMProcessManager {
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

        return LLMProcessManager(project, classesToTest, interestingPsiClasses, polymorphismRelations)
    }

    override fun generateTestsForClass(e: AnActionEvent) {
        val project = e.project!!
        if (!SettingsArguments.isTokenSet()) {
            llmErrorManager.errorProcess(TestGenieBundle.message("missingToken"), project)
            return
        }
        val llmPipeline: Pipeline = createLLMPipeline(e)
        llmPipeline.runTestGeneration(getLLMProcessManager(e), CodeTypeAndAdditionData(CodeType.CLASS))
    }

    override fun generateTestsForMethod(e: AnActionEvent) {
        TODO("Not yet implemented")
    }

    override fun generateTestsForLine(e: AnActionEvent) {
        TODO("Not yet implemented")
    }
}
