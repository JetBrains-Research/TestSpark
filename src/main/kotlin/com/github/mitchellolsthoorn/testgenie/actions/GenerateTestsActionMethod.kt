package com.github.mitchellolsthoorn.testgenie.actions

import com.intellij.model.psi.PsiSymbolReference
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDeclarationStatement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiReference
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil

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

        val psiElement = e.dataContext.getData(CommonDataKeys.PSI_ELEMENT)
        //psiElement is PsiReference
        val psiMethod = e.dataContext.getData(CommonDataKeys.PSI_ELEMENT) as PsiMethod  // Checked in update method
        val surroundingClass : PsiClass = PsiTreeUtil.getParentOfType(psiMethod, PsiClass::class.java) as PsiClass
        // TODO: remove this line
        // TODO: deal with overloads
        Messages.showInfoMessage("Called generate tests action on a method $psiMethod. Surrounding class is $surroundingClass. References to it: ${ReferencesSearch.search(psiMethod).findAll().isEmpty()}", "GenerateTestsActionMethod")
        val classFQN = surroundingClass.qualifiedName ?: return

        log.info("Selected class is $classFQN")

        //val resultPath = EvoSuiteRunner.runEvoSuite(projectPath, projectClassPath, classFQN)

        //AppExecutorUtil.getAppScheduledExecutorService().execute(EvoSuiteResultWatcher(project, resultPath))
    }

    /**
     * Makes the action visible only if a method has been selected.
     *
     * @param e AnActionEvent class that contains useful information about the action event
     */
    override fun update(e: AnActionEvent) {
        val psiElement = e.dataContext.getData(CommonDataKeys.PSI_ELEMENT)
        e.presentation.isVisible = psiElement is PsiMethod // TODO: check for method declaration
    }
}