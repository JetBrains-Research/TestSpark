package nl.tudelft.ewi.se.ciselab.testgenie.actions

import nl.tudelft.ewi.se.ciselab.testgenie.evosuite.ResultWatcher
import nl.tudelft.ewi.se.ciselab.testgenie.evosuite.Runner
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiSubstitutor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
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
        val project: Project = e.project ?: return

        val psiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE) ?: return
        val cursor = e.dataContext.getData(CommonDataKeys.CARET) ?: return
        val offset = cursor.offset

        val psiMethod = getSurroundingMethod(psiFile, offset) ?: return  // This has to be handled in update method
        val containingClass: PsiClass = psiMethod.containingClass ?: return

        val method = psiMethod.name
        val signature: Array<String> =
            psiMethod.getSignature(PsiSubstitutor.EMPTY).parameterTypes.map2Array { it.canonicalText }
        val returnType: String = psiMethod.returnType?.canonicalText ?: "void"
        val classFQN = containingClass.qualifiedName ?: return

        val projectPath: String = ProjectRootManager.getInstance(project).contentRoots.first().path
        val projectClassPath = "$projectPath/target/classes/"

        log.info("Generating tests for project $projectPath with classpath $projectClassPath")

        log.info("Selected method is $classFQN::$method${signature.contentToString()}$returnType")
        println("Selected method is $classFQN::$method${signature.contentToString()}$returnType")

        val resultPath = Runner(projectPath, projectClassPath, classFQN).forMethod(method).runEvoSuite()

        AppExecutorUtil.getAppScheduledExecutorService().execute(ResultWatcher(project, resultPath))
    }

    /**
     * Makes the action visible only if a method has been selected.
     *
     * @param e AnActionEvent class that contains useful information about the action event
     */
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = false

        val cursor = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret ?: return
        val psiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE) ?: return
        e.presentation.isEnabledAndVisible = getSurroundingMethod(psiFile, cursor.offset) != null
    }

    /**
     * Gets the method on which the user has clicked (the click has to be inside the contents of the method).
     *
     * @param psiFile the current PSI file (where the user makes the click)
     * @param offset the offset of the primary caret
     * @return PsiMethod element if has been found, null otherwise
     */
    private fun getSurroundingMethod(psiFile: PsiFile, offset: Int): PsiMethod? {
        val methodElements = PsiTreeUtil.findChildrenOfAnyType(psiFile, PsiMethod::class.java)

        var surroundingMethod: PsiMethod? = null
        for (method: PsiMethod in methodElements) {
            if (method.startOffset <= offset && method.endOffset >= offset) {
                surroundingMethod = method
                break
            }
        }
        return surroundingMethod
    }
}