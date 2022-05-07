package com.github.mitchellolsthoorn.testgenie.actions

import com.github.mitchellolsthoorn.testgenie.evosuite.ResultWatcher
import com.github.mitchellolsthoorn.testgenie.evosuite.Runner
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiSubstitutor
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.containers.map2Array

/**
 * This class generates tests for a method.
 */
class GenerateTestsActionMethod : AnAction() {
    private val log = Logger.getInstance(this.javaClass)

    /**
     * Performs test generation for a method when the action is invoked.
     *
     * @param e AnActionEvent class that contains useful information about the action event
     */
    override fun actionPerformed(e: AnActionEvent) {
        // determine class path
        val project: Project = e.project ?: return

        val projectPath: String = ProjectRootManager.getInstance(project).contentRoots.first().path
        val projectClassPath = "$projectPath/target/classes/"

        log.info("Generating tests for project $projectPath with classpath $projectClassPath")

        val psiMethod =
            e.dataContext.getData(CommonDataKeys.PSI_ELEMENT) as PsiMethod  // The type is checked in update method
        val containingClass: PsiClass = psiMethod.containingClass ?: return

        val method = psiMethod.name
        val classFQN = containingClass.qualifiedName ?: return
        println(psiMethod.returnType.toString())

        val signature: Array<String> =
            psiMethod.getSignature(PsiSubstitutor.EMPTY).parameterTypes.map2Array { it.canonicalText }

        log.info("Selected method is $classFQN::$method${signature.contentToString()}")

        val resultPath = Runner.runEvoSuiteForMethod(projectPath, projectClassPath, classFQN, method)

        AppExecutorUtil.getAppScheduledExecutorService().execute(ResultWatcher(project, resultPath))
    }

    /**
     * Makes the action visible only if a method has been selected.
     *
     * @param e AnActionEvent class that contains useful information about the action event
     */
    override fun update(e: AnActionEvent) {
        val psiElement = e.dataContext.getData(CommonDataKeys.PSI_ELEMENT)
        e.presentation.isEnabledAndVisible = psiElement is PsiMethod // TODO: check for the current project
    }
}