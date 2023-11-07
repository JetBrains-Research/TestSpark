package org.jetbrains.research.testspark.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
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
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiStatement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import org.jetbrains.research.testspark.services.SettingsProjectService
import org.jetbrains.research.testspark.services.StaticInvalidationService
import org.jetbrains.research.testspark.tools.Pipeline

fun createPipeline(e: AnActionEvent): Pipeline {
    val project: Project = e.project!!

    val projectClassPath: String = ProjectRootManager.getInstance(project).contentRoots.first().path

    val settingsProjectState = project.service<SettingsProjectService>().state
    val packageName = "$projectClassPath/${settingsProjectState.buildPath}"

    return Pipeline(e, packageName)
}

fun PsiMethod.getSignatureString(): String {
    val bodyStart = body?.startOffsetInParent ?: this.textLength
    return text.substring(0, bodyStart).replace('\n', ' ').trim()
}

fun createLLMPipeline(e: AnActionEvent): Pipeline {
    val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE)!!
    val caret: Caret = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret!!

    val cutPsiClass: PsiClass = getSurroundingClass(psiFile, caret)!!

    val packageList = cutPsiClass.qualifiedName.toString().split(".").toMutableList()
    packageList.removeLast()

    val packageName = packageList.joinToString(".")

    return Pipeline(e, packageName)
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

    // check if a class is noted as abstract in the text
    return psiClass.text.replace(" ", "")
        .contains("abstractclass${psiClass.name}", ignoreCase = true)
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
    return if (psiClass.isInterface) {
        "Interface ${psiClass.qualifiedName}"
    } else if (isAbstractClass(psiClass)) {
        "Abstract Class ${psiClass.qualifiedName}"
    } else {
        "Class ${psiClass.qualifiedName}"
    }
}

/**
 * Gets the display name of a method, depending on if it is a (default) constructor or a normal method.
 * This is used when displaying the name of a method in GenerateTestsActionMethod menu entry.
 *
 * @param psiMethod the PSI method of interest
 * @return the display name of the PSI method
 */
fun getMethodDisplayName(psiMethod: PsiMethod): String {
    return if (isDefaultConstructor(psiMethod)) {
        "Default Constructor"
    } else if (psiMethod.isConstructor) {
        "Constructor"
    } else if (isMethodDefault(psiMethod)) {
        "Default Method ${psiMethod.name}"
    } else {
        "Method ${psiMethod.name}"
    }
}

/**
 * Makes the action visible only if a class has been selected.
 * It also updates the action name depending on which class has been selected.
 *
 * @param e an action event that contains useful information and corresponds to the action invoked by the user
 * @param name a name of the test generator
 */
fun updateForClass(e: AnActionEvent, name: String) {
    e.presentation.isEnabledAndVisible = false

    val caret: Caret = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret ?: return
    val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE) ?: return

    if (psiFile !is PsiJavaFile) return

    val psiClass: PsiClass? = getSurroundingClass(psiFile, caret)

    psiClass ?: return

    e.presentation.isEnabledAndVisible = true
    e.presentation.text = "Generate Tests For ${getClassDisplayName(psiClass)} by $name"
}

/**
 * Makes the action visible only if a method has been selected.
 * It also updates the action name depending on which method has been selected.
 *
 * @param e an action event that contains useful information and corresponds to the action invoked by the user
 * @param name a name of the test generator
 */
fun updateForMethod(e: AnActionEvent, name: String) {
    e.presentation.isEnabledAndVisible = false

    val caret: Caret = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret ?: return
    val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE) ?: return

    if (psiFile !is PsiJavaFile) return

    val psiMethod: PsiMethod = getSurroundingMethod(psiFile, caret) ?: return

    e.presentation.isEnabledAndVisible = true
    e.presentation.text = "Generate Tests For ${getMethodDisplayName(psiMethod)} by $name"
}

/**
 * Makes the action visible only if a line has been selected.
 * It also updates the action name depending on which line has been selected.
 *
 * @param e an action event that contains useful information and corresponds to the action invoked by the user
 * @param name a name of the test generator
 */
fun updateForLine(e: AnActionEvent, name: String) {
    e.presentation.isEnabledAndVisible = false

    val caret: Caret = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret ?: return
    val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE) ?: return

    if (psiFile !is PsiJavaFile) return

    val line: Int = getSurroundingLine(psiFile, caret)?.plus(1)
        ?: return // lines in the editor and in EvoSuite are one-based

    e.presentation.isEnabledAndVisible = true
    e.presentation.text = "Generate Tests For Line $line by $name"
}

val importPattern = Regex(
    pattern = "^import\\s+(static\\s)?((?:[a-zA-Z_]\\w*\\.)*[a-zA-Z_](?:\\w*\\.?)*)(?:\\.\\*)?;",
    options = setOf(RegexOption.MULTILINE),
)

val packagePattern = Regex(
    pattern = "^package\\s+((?:[a-zA-Z_]\\w*\\.)*[a-zA-Z_](?:\\w*\\.?)*)(?:\\.\\*)?;",
    options = setOf(RegexOption.MULTILINE),
)

val runWithPattern = Regex(
    pattern = "@RunWith\\([^)]*\\)",
    options = setOf(RegexOption.MULTILINE),
)

/**
 * Returns the full text of a given class including the package, imports, and class code.
 *
 * @param cl The PsiClass object representing the class.
 * @return The full text of the class.
 */
fun getClassFullText(cl: PsiClass): String {
    var fullText = ""
    val fileText = cl.containingFile.text

    // get package
    packagePattern.findAll(fileText, 0).map {
        it.groupValues[0]
    }.forEach {
        fullText += "$it\n\n"
    }

    // get imports
    importPattern.findAll(fileText, 0).map {
        it.groupValues[0]
    }.forEach {
        fullText += "$it\n"
    }

    // Add class code
    fullText += cl.text

    return fullText
}
