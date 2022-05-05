package com.github.mitchellolsthoorn.testgenie.actions

import com.github.mitchellolsthoorn.testgenie.evosuite.EvoSuiteResultWatcher
import com.github.mitchellolsthoorn.testgenie.evosuite.EvoSuiteRunner
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.concurrency.AppExecutorUtil


class GenerateTestsAction : AnAction() {
    private val log = Logger.getInstance(this.javaClass)

    override fun actionPerformed(e: AnActionEvent) {
        // determine class path
        val project: Project = e.project ?: return

        val projectPath: String = ProjectRootManager.getInstance(project).contentRoots.first().path
        val projectClassPath = "$projectPath/target/classes/"

        log.info("Generating tests for project $projectPath with classpath $projectClassPath")

        val psiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE)
        psiFile ?: return

        val psiElement = e.dataContext.getData(CommonDataKeys.PSI_ELEMENT)

        //TODO: handle the element being a method
        if (psiElement !is PsiClass) {
            val surroundingClass = PsiTreeUtil.getParentOfType(psiElement, PsiElement::class.java)
            println("selected ${psiElement.toString().split(":")[0].substring(3)}"
                    + "${psiElement.toString().split(":")[1]} of class $surroundingClass")
            return
        }

        val mainClass: PsiClass = PsiTreeUtil.findChildOfType(psiFile, PsiClass::class.java) ?: return
        val classFileFQN = mainClass.qualifiedName ?: return

        val classFQN = classFileFQN.substring(0, classFileFQN.lastIndexOf(".") + 1)
                            .plus(psiElement.toString().split(":")[1])

        log.info("Selected class is $classFQN")

        val resultPath = EvoSuiteRunner.runEvoSuite(projectPath, projectClassPath, classFQN)

        AppExecutorUtil.getAppScheduledExecutorService().execute(EvoSuiteResultWatcher(project, resultPath))
    }

    override fun update(e: AnActionEvent) {
        val psiElement = e.dataContext.getData(CommonDataKeys.PSI_ELEMENT)
        println(psiElement)
        e.presentation.isVisible = psiElement is PsiClass
    }

}