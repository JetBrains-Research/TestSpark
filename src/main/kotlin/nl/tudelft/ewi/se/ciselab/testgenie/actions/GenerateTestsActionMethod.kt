package nl.tudelft.ewi.se.ciselab.testgenie.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import nl.tudelft.ewi.se.ciselab.testgenie.evosuite.Runner
import nl.tudelft.ewi.se.ciselab.testgenie.helpers.generateMethodDescriptor

/**
 * This class contains all the logic related to generating tests for a method.
 * No actual generation happens in this class, rather it is responsible for displaying the action option to the user when it is available,
 *   getting the information about the selected method and passing it to (EvoSuite) Runner.
 */
class GenerateTestsActionMethod : AnAction() {
    private val log = Logger.getInstance(this.javaClass)

    /**
     * Performs test generation for a method when the action is invoked.
     *
     * @param e an action event that contains useful information
     */
    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return

        val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE) ?: return
        val caret: Caret = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret ?: return

        val psiMethod: PsiMethod = getSurroundingMethod(psiFile, caret) ?: return
        val containingClass: PsiClass = psiMethod.containingClass ?: return

        val classFQN = containingClass.qualifiedName ?: return
        val methodDescriptor = generateMethodDescriptor(psiMethod)

        val projectPath: String = ProjectRootManager.getInstance(project).contentRoots.first().path
        val projectClassPath = "$projectPath/target/classes/"

        log.info("Generating tests for project $projectPath with classpath $projectClassPath")

        log.info("Selected class is $classFQN, method is $methodDescriptor")

        Runner(project, projectPath, projectClassPath, classFQN).forMethod(methodDescriptor).runEvoSuite()
    }

    /**
     * Makes the action visible only if a method has been selected.
     * It also updates the action name depending on which method has been selected.
     *
     * @param e an action event that contains useful information
     */
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = false

        val caret: Caret = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret ?: return
        val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE) ?: return

        val psiMethod: PsiMethod = getSurroundingMethod(psiFile, caret) ?: return

        e.presentation.isEnabledAndVisible = true
        e.presentation.text = "Generate Tests For ${getMethodDisplayName(psiMethod)}"
    }
}
