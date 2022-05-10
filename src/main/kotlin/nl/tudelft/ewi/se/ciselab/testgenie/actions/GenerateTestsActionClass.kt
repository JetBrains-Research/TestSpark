package nl.tudelft.ewi.se.ciselab.testgenie.actions

import nl.tudelft.ewi.se.ciselab.testgenie.evosuite.ResultWatcher
import nl.tudelft.ewi.se.ciselab.testgenie.evosuite.Runner
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiAnonymousClass
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
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
        val project: Project = e.project ?: return

        val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE) ?: return
        val caret: Caret = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret ?: return
        val doc: Document = e.dataContext.getData(CommonDataKeys.EDITOR)?.document ?: return

        val psiClass: PsiClass = getSurroundingClass(psiFile, doc, caret) ?: return
        val classFQN = psiClass.qualifiedName ?: return

        val projectPath: String = ProjectRootManager.getInstance(project).contentRoots.first().path
        val projectClassPath = "$projectPath/target/classes/"

        log.info("Generating tests for project $projectPath with classpath $projectClassPath")

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
        e.presentation.isEnabledAndVisible = false

        val caret: Caret = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret ?: return
        val doc: Document = e.dataContext.getData(CommonDataKeys.EDITOR)?.document ?: return
        val psiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE) ?: return

        val psiClass: PsiClass = getSurroundingClass(psiFile, doc, caret) ?: return

        e.presentation.text = "Generate Tests For Class ${psiClass.name}"
        e.presentation.isEnabledAndVisible = true
    }

    /**
     * Gets the class on which the user has clicked (the click has to be inside the contents of the class).
     * NB! This has to be a concrete class, so enums and abstract classes do not count.
     *
     * @param psiFile the current PSI file (where the user makes the click)
     * @param doc the current document (where the user makes the click)
     * @param caret the primary caret that did the click
     * @return PsiClass element if has been found, null otherwise
     */
    private fun getSurroundingClass(psiFile: PsiFile, doc: Document, caret: Caret): PsiClass? {
        // Get the classes of the PSI file
        val classElements = PsiTreeUtil.findChildrenOfAnyType(psiFile, PsiClass::class.java)

        // Get the surrounding PSI class (i.e. the cursor has to be withing that class)
        var surroundingClass: PsiClass? = null
        for (clazz: PsiClass in classElements) {
            if (clazz.startOffset <= caret.offset && clazz.endOffset >= caret.offset) {
                // Check the constraints on a class
                if (!validateClass(clazz, doc)) continue
                surroundingClass = clazz
            }
        }

        return surroundingClass
    }

    /**
     * Checks if the constraints on the selected class are satisfied, so that EvoSuite can generate tests for it.
     * Namely, it is a concrete class (non-abstract, not an interface, not an enum, not an anonymous inner class).
     *
     * @param psiClass the selected PSI class (where the user makes the click)
     * @param doc the current document (where the user makes the click)
     * @return true if the constraints are satisfied, false otherwise
     */
    private fun validateClass(psiClass: PsiClass, doc: Document): Boolean {
        // The class cannot be null, enum, interface or anonymous class
        if (psiClass.isEnum || psiClass.isInterface || psiClass is PsiAnonymousClass) return false

        // The psiClass cannot be abstract
        val lineRange = TextRange(psiClass.startOffset, doc.getLineEndOffset(doc.getLineNumber(psiClass.startOffset)))
        val line: String = doc.getText(lineRange)
        if (line.contains("abstract ")) return false
        return true
    }
}