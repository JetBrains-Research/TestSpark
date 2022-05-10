package nl.tudelft.ewi.se.ciselab.testgenie.actions

import nl.tudelft.ewi.se.ciselab.testgenie.evosuite.ResultWatcher
import nl.tudelft.ewi.se.ciselab.testgenie.evosuite.Runner
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiAnonymousClass
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementsAroundOffsetUp
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import com.intellij.util.concurrency.AppExecutorUtil

/**
 * This class generates tests for a class.
 */
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

        val psiClass = e.dataContext.getData(CommonDataKeys.PSI_ELEMENT) as PsiClass  // Checked in update method
        val classFQN = psiClass.qualifiedName ?: return

        log.info("Selected class is $classFQN")

        val resultPath = Runner(projectPath, projectClassPath, classFQN).forClass().runEvoSuite()

        AppExecutorUtil.getAppScheduledExecutorService().execute(ResultWatcher(project, resultPath))
    }

    /**
     * Makes the action visible only if a class has been selected.
     *
     * @param e AnActionEvent class that contains useful information about the action event
     */
    override fun update(e: AnActionEvent) {
        val cursor = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret ?: return
        val doc = e.dataContext.getData(CommonDataKeys.EDITOR)?.document ?: return
        val psiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE) ?: return

        val clazz = getSurroundingClass(psiFile, cursor.offset) ?: return

        val lineRange: TextRange = TextRange(clazz.startOffset, doc.getLineEndOffset(doc.getLineNumber(clazz.startOffset)))
        val line: String = doc.getText(lineRange) //cursor.visualLineStart, cursor.visualLineEnd))

        Messages.showInfoMessage(line, "lololo")
        if (line.contains("abstract ")) return
        //e.presentation.isEnabledAndVisible = psiElement is PsiClass // TODO: check for the current project
    }

    /**
     * Gets the class on which the user has clicked (the click has to be inside the contents of the class).
     *
     * @param psiFile the current PSI file (where the user makes the click)
     * @param offset the offset of the primary caret
     * @return psiClass element if has been found, null otherwise
     */
    private fun getSurroundingClass(psiFile: PsiFile, offset: Int): PsiClass? {
        val classElements = PsiTreeUtil.findChildrenOfAnyType(psiFile, PsiClass::class.java)

        var surroundingClass: PsiClass? = null
        for (clazz: PsiClass in classElements) {
            if (clazz.startOffset <= offset && clazz.endOffset >= offset) {
                surroundingClass = clazz
                break
            }
        }

        if (surroundingClass == null || surroundingClass.isEnum || surroundingClass.isInterface || surroundingClass is PsiAnonymousClass) return null
        return surroundingClass
    }
}