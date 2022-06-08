package nl.tudelft.ewi.se.ciselab.testgenie.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiAnonymousClass
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiCodeBlock
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiStatement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import nl.tudelft.ewi.se.ciselab.testgenie.evosuite.Runner
import nl.tudelft.ewi.se.ciselab.testgenie.services.StaticInvalidationService
import nl.tudelft.ewi.se.ciselab.testgenie.services.TestGenieSettingsService

/**
 * This file contains some useful methods related to GenerateTests actions.
 */

/**
 * Extracts the required information from an action event and creates an (EvoSuite) runner.
 *
 * @param e an action event that contains useful information and corresponds to the action invoked by the user
 * @return the created (EvoSuite) Runner, null if some information is missing or if there is no surrounding class
 */
fun createEvoSuiteRunner(e: AnActionEvent): Runner? {
    val project: Project = e.project ?: return null

    val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE) ?: return null
    val caret: Caret = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret ?: return null
    val vFile = e.dataContext.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
    val fileUrl = vFile.presentableUrl
    val modificationStamp = vFile.modificationStamp

    val psiClass: PsiClass = getSurroundingClass(psiFile, caret) ?: return null
    val classFQN = psiClass.qualifiedName ?: return null

    val projectPath: String = ProjectRootManager.getInstance(project).contentRoots.first().path
    val settingsState = TestGenieSettingsService.getInstance().state ?: return null
    val projectClassPath = "$projectPath/" + settingsState.buildPath

    val log = Logger.getInstance("GenerateTestsUtils")

    log.info("Generating tests for project $projectPath with classpath $projectClassPath")

    log.info("Selected class is $classFQN")

    val doc: Document = PsiDocumentManager.getInstance(psiFile.project).getDocument(psiFile) ?: return null
    val cacheStartLine: Int = doc.getLineNumber(psiClass.startOffset)
    val cacheEndLine: Int = doc.getLineNumber(psiClass.endOffset)
    log.info("Selected class is on lines $cacheStartLine to $cacheEndLine")

    return Runner(project, projectPath, projectClassPath, classFQN, fileUrl, modificationStamp)
//        .withCacheLines(cacheStartLine, cacheEndLine)
}

/**
 * Gets the class on which the user has clicked (the click has to be inside the contents of the class).
 * NB! This has to be a concrete class, so enums, abstract classes and interfaces do not count.
 *
 * @param psiFile the current PSI file (where the user makes a click)
 * @param caret the current (primary) caret that did the click
 * @return PsiClass element if it has been found, null otherwise
 */
fun getSurroundingClass(psiFile: PsiFile, caret: Caret): PsiClass? {
    // Get the classes of the PSI file
    val classElements = PsiTreeUtil.findChildrenOfAnyType(psiFile, PsiClass::class.java)

    // Get the surrounding PSI class (i.e. the cursor has to be within that class)
    var surroundingClass: PsiClass? = null
    for (psiClass: PsiClass in classElements) {
        if (withinElement(psiClass, caret)) {
            // Check the constraints on a class
            if (!validateClass(psiClass)) continue
            surroundingClass = psiClass
        }
    }
    return surroundingClass
}

/**
 * Gets the method on which the user has clicked (the click has to be inside the contents of the method).
 *
 * @param psiFile the current PSI file (where the user makes the click)
 * @param caret the current (primary) caret that did the click
 * @return PsiMethod element if has been found, null otherwise
 */
fun getSurroundingMethod(psiFile: PsiFile, caret: Caret): PsiMethod? {
    // Get the methods of the PSI file
    val methodElements = PsiTreeUtil.findChildrenOfAnyType(psiFile, PsiMethod::class.java)

    // Get the surrounding PSI method (i.e. the cursor has to be within that method)
    var surroundingMethod: PsiMethod? = null
    for (method: PsiMethod in methodElements) {
        if (isMethodConcrete(method) && withinElement(method, caret)) {
            val surroundingClass: PsiClass = PsiTreeUtil.getParentOfType(method, PsiClass::class.java) ?: continue
            // Check the constraints on the surrounding class
            if (!validateClass(surroundingClass)) continue
            surroundingMethod = method
        }
    }
    return surroundingMethod
}

/**
 * Gets the selected line if the constraints on the selected line are satisfied,
 *   so that EvoSuite can generate tests for it.
 * Namely, the line is within a method, it is not blank and contains (part of) a statement.
 *
 * @param psiFile the current PSI file (where the user makes the click)
 * @param caret the current (primary) caret that did the click
 * @return line number if the constraints are satisfied else null
 */
fun getSurroundingLine(psiFile: PsiFile, caret: Caret): Int? {
    val psiMethod: PsiMethod = getSurroundingMethod(psiFile, caret) ?: return null

    val doc: Document = PsiDocumentManager.getInstance(psiFile.project).getDocument(psiFile) ?: return null

    val selectedLine: Int = doc.getLineNumber(caret.offset)
    val selectedLineText: String =
        doc.getText(TextRange(doc.getLineStartOffset(selectedLine), doc.getLineEndOffset(selectedLine)))

    if (selectedLineText.isBlank()) return null

    if (!validateLine(selectedLine, psiMethod, psiFile)) return null
    return selectedLine
}

/**
 * Checks if a method is concrete (non-abstract in case of an abstract class and non-default in case of an interface).
 *
 * @param psiMethod the PSI method to check
 * @return true if the method has a body (thus, is concrete), false otherwise
 */
fun isMethodConcrete(psiMethod: PsiMethod): Boolean {
    return psiMethod.body != null
}

/**
 * Checks if the method is a default method of an interface.
 *
 * @param psiMethod the PSI method of interest
 * @return true if the method is a default method of an interface, false otherwise
 */
fun isMethodDefault(psiMethod: PsiMethod): Boolean {
    if (!isMethodConcrete(psiMethod)) return false
    return psiMethod.containingClass?.isInterface ?: return false
}

/**
 * Checks if a PSI method is a default constructor.
 *
 * @param psiMethod the PSI method of interest
 * @return true if the PSI method is a default constructor, false otherwise
 */
private fun isDefaultConstructor(psiMethod: PsiMethod): Boolean {
    return psiMethod.isConstructor && psiMethod.body?.isEmpty ?: false
}

/**
 * Checks if a PSI class is an abstract class.
 *
 * @param psiClass the PSI class of interest
 * @return true if the PSI class is an abstract class, false otherwise
 */
private fun isAbstractClass(psiClass: PsiClass): Boolean {
    if (psiClass.isInterface) return false

    val methods = PsiTreeUtil.findChildrenOfType(psiClass, PsiMethod::class.java)
    for (psiMethod: PsiMethod in methods) {
        if (!isMethodConcrete(psiMethod)) {
            return true
        }
    }
    return false
}

/**
 * Checks if the constraints on the selected class are satisfied, so that EvoSuite can generate tests for it.
 * Namely, it is not an enum and not an anonymous inner class.
 *
 * @param psiClass the PSI class of interest
 * @return true if the constraints are satisfied, false otherwise
 */
private fun validateClass(psiClass: PsiClass): Boolean {
    return !psiClass.isEnum && psiClass !is PsiAnonymousClass
}

/**
 * Checks if the selected line contains (part of) a statement and is a valid line to be selected for EvoSuite.
 * Namely, the line is within a method, it is not blank and contains (part of) a statement.
 *
 * @param selectedLine selected line number
 * @param psiMethod surrounding PSI method
 * @param psiFile containing PSI file
 * @return true if the line is valid, false otherwise
 */
private fun validateLine(selectedLine: Int, psiMethod: PsiMethod, psiFile: PsiFile): Boolean {
    val doc: Document = PsiDocumentManager.getInstance(psiFile.project).getDocument(psiFile) ?: return false

    val psiMethodBody: PsiCodeBlock = psiMethod.body ?: return false
    if (psiMethodBody.statements.isEmpty()) return false

    val firstStatement: PsiStatement = psiMethodBody.statements.first()
    val lastStatement: PsiStatement = psiMethodBody.statements.last()

    val firstStatementLine: Int = doc.getLineNumber(firstStatement.startOffset)
    val lastStatementLine: Int = doc.getLineNumber(lastStatement.endOffset)

    return (selectedLine in firstStatementLine..lastStatementLine)
}

/**
 * Calculates which lines to invalidate from cache.
 *
 * @param psiFile psiFile of the document
 */
fun calculateLinesToInvalidate(psiFile: PsiFile): Set<Int> {
    val staticInvalidator = psiFile.project.service<StaticInvalidationService>()
    return staticInvalidator.invalidate(psiFile)
}

/**
 * Checks if the caret is within the given PsiElement.
 *
 * @param psiElement PSI element of interest
 * @param caret the current (primary) caret that did the click
 * @return true if the caret is within the PSI element, false otherwise
 */
private fun withinElement(psiElement: PsiElement, caret: Caret): Boolean {
    return (psiElement.startOffset <= caret.offset) && (psiElement.endOffset >= caret.offset)
}

/**
 * Gets the display name of a class, depending on if it is a normal class, an abstract class or an interface.
 * This is used when displaying the name of a class in GenerateTestsActionClass menu entry.
 *
 * @param psiClass the PSI class of interest
 * @return the display name of the PSI class
 */
fun getClassDisplayName(psiClass: PsiClass): String {
    return if (psiClass.isInterface) "Interface ${psiClass.qualifiedName}"
    else if (isAbstractClass(psiClass)) "Abstract Class ${psiClass.qualifiedName}"
    else "Class ${psiClass.qualifiedName}"
}

/**
 * Gets the display name of a method, depending on if it is a (default) constructor or a normal method.
 * This is used when displaying the name of a method in GenerateTestsActionMethod menu entry.
 *
 * @param psiMethod the PSI method of interest
 * @return the display name of the PSI method
 */
fun getMethodDisplayName(psiMethod: PsiMethod): String {
    return if (isDefaultConstructor(psiMethod)) "Default Constructor"
    else if (psiMethod.isConstructor) "Constructor"
    else if (isMethodDefault(psiMethod)) "Default Method ${psiMethod.name}"
    else "Method ${psiMethod.name}"
}
