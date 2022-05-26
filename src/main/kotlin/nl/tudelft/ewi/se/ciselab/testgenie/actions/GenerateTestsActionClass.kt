package nl.tudelft.ewi.se.ciselab.testgenie.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Caret
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile

/**
 * This class contains all the logic related to generating tests for a class.
 * No actual generation happens in this class, rather it is responsible for displaying the action option to the user when it is available,
 *   getting the information about the selected class and passing it to (EvoSuite) Runner.
 */
class GenerateTestsActionClass : AnAction() {
    /**
     * Creates and calls (EvoSuite) Runner to generate tests for a class when the action is invoked.
     *
     * @param e an action event that contains useful information and corresponds to the action invoked by the user
     */
    override fun actionPerformed(e: AnActionEvent) {
        createEvoSuiteRunner(e)?.forClass()?.runEvoSuite()
        val project: Project = e.project ?: return

        val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE) ?: return
        val caret: Caret = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret ?: return
        val vFile = e.dataContext.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val fileName = vFile.presentableUrl
        val modificationStamp = vFile.modificationStamp

        val psiClass: PsiClass = getSurroundingClass(psiFile, caret) ?: return
        val classFQN = psiClass.qualifiedName ?: return

        val projectPath: String = ProjectRootManager.getInstance(project).contentRoots.first().path
        val settingsState = TestGenieSettingsService.getInstance().state ?: return
        val projectClassPath = "$projectPath/" + settingsState.buildPath

        log.info("Generating tests for project $projectPath with classpath $projectClassPath")

        log.info("Selected class is $classFQN")

        Runner(project, projectPath, projectClassPath, classFQN, fileName, modificationStamp).forClass().runEvoSuite()
    }

    /**
     * Makes the action visible only if a class has been selected.
     * It also updates the action name depending on which class has been selected.
     *
     * @param e an action event that contains useful information and corresponds to the action invoked by the user
     */
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = false

        val caret: Caret = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret ?: return
        val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE) ?: return

        val psiClass: PsiClass = getSurroundingClass(psiFile, caret) ?: return

        e.presentation.isEnabledAndVisible = true
        e.presentation.text = "Generate Tests For ${getClassDisplayName(psiClass)}"
    }
}
