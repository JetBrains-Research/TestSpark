package com.github.mitchellolsthoorn.testgenie.actions

import com.github.mitchellolsthoorn.testgenie.evosuite.EvoSuiteResultWatcher
import com.github.mitchellolsthoorn.testgenie.evosuite.EvoSuiteRunner
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiClass
import com.intellij.util.concurrency.AppExecutorUtil


class GenerateTestsActionClass : AnAction() {
    private val log = Logger.getInstance(this.javaClass)

    /**
     * Performs test generation for a class when the action is invoked.
     *
     * @param e AnActionEvent class that contains useful information about the action event
     */
    override fun actionPerformed(e: AnActionEvent) {
        // determine class path
        val project: Project = e.project ?: return

        val projectPath: String = ProjectRootManager.getInstance(project).contentRoots.first().path
        val projectClassPath = "$projectPath/target/classes/"

        log.info("Generating tests for project $projectPath with classpath $projectClassPath")

        val psiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE)
        psiFile ?: return

        val psiClass = e.dataContext.getData(CommonDataKeys.PSI_ELEMENT) as PsiClass  // Checked in update method

        // Use FQN of the actually selected class (important in case of multiple classes in the same class file)
        val classFQN = psiClass.qualifiedName
        classFQN ?: return

        log.info("Selected class is $classFQN")

        val resultPath = EvoSuiteRunner.runEvoSuite(projectPath, projectClassPath, classFQN)

        AppExecutorUtil.getAppScheduledExecutorService().execute(EvoSuiteResultWatcher(project, resultPath))
    }

    /**
     * Makes the action visible only if a class has been selected.
     *
     * @param e AnActionEvent class that contains useful information about the action event
     */
    override fun update(e: AnActionEvent) {
        val psiElement = e.dataContext.getData(CommonDataKeys.PSI_ELEMENT)
        e.presentation.isVisible = psiElement is PsiClass
    }

}