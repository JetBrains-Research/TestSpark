package com.github.mitchellolsthoorn.testgenie.actions

import com.github.mitchellolsthoorn.testgenie.evo.EvoSuiteRunner
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil


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

        val mainClass: PsiClass = PsiTreeUtil.findChildOfType(psiFile, PsiClass::class.java) ?: return
        val classFQN = mainClass.qualifiedName ?: return

        log.info("Selected class is $classFQN")

        EvoSuiteRunner.runEvoSuite(projectPath, projectClassPath, classFQN)
    }

}