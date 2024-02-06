package org.jetbrains.research.testspark.helpers

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiAnonymousClass
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiCodeBlock
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiStatement
import com.intellij.psi.PsiSubstitutor
import com.intellij.psi.PsiType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import com.intellij.util.containers.stream
import java.util.stream.Collectors

// Grammar taken from: https://docs.oracle.com/javase/specs/jvms/se11/html/jvms-4.html#jvms-4.3

/**
 * Helper for generating method descriptors for methods.
 *
 * @param psiMethod the method to extract the descriptor from
 * @return the method descriptor
 */
fun generateMethodDescriptor(psiMethod: PsiMethod): String {
    val parameterTypes =
        psiMethod.getSignature(PsiSubstitutor.EMPTY)
            .parameterTypes
            .stream()
            .map { i -> generateFieldType(i) }
            .collect(Collectors.joining())

    val returnType = generateReturnDescriptor(psiMethod)

    return "${psiMethod.name}($parameterTypes)$returnType"
}

/**
 * Generates the return descriptor for a method.
 *
 * @param psiMethod the method
 * @return the return descriptor
 */
fun generateReturnDescriptor(psiMethod: PsiMethod): String {
    if (psiMethod.returnType == null || psiMethod.returnType!!.canonicalText == "void") {
        // void method
        return "V"
    }

    return generateFieldType(psiMethod.returnType!!)
}

/**
 * Generates the field descriptor for a type.
 *
 * @param psiType the type to generate the descriptor for
 * @return the field descriptor
 */
fun generateFieldType(psiType: PsiType): String {
    // arrays (ArrayType)
    if (psiType.arrayDimensions > 0) {
        val arrayType = generateFieldType(psiType.deepComponentType)
        return "[".repeat(psiType.arrayDimensions) + arrayType
    }

    //  objects (ObjectType)
    if (psiType is PsiClassType) {
        val classType = psiType.resolve()
        if (classType != null) {
            val className = classType.qualifiedName?.replace('.', '/')

            // no need to handle generics: they are not part of method descriptors

            return "L$className;"
        }
    }

    // primitives (BaseType)
    psiType.canonicalText.let {
        return when (it) {
            "int" -> "I"
            "long" -> "J"
            "float" -> "F"
            "double" -> "D"
            "boolean" -> "Z"
            "byte" -> "B"
            "char" -> "C"
            "short" -> "S"
            else -> throw IllegalArgumentException("Unknown type: $it")
        }
    }
}

/**
 * Gets the class on which the user has clicked (the click has to be inside the contents of the class).
 * NB! This has to be a concrete class, so enums, abstract classes and interfaces do not count.
 *
 * @param psiFile the current PSI file (where the user makes a click)
 * @param caret the current (primary) caret that did the click
 * @return PsiClass element if it has been found, null otherwise
 */
fun getSurroundingClass(
    psiFile: PsiFile,
    caret: Caret,
): PsiClass? {
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
fun getSurroundingMethod(
    psiFile: PsiFile,
    caret: Caret,
): PsiMethod? {
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
fun getSurroundingLine(
    psiFile: PsiFile,
    caret: Caret,
): Int? {
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
private fun validateLine(
    selectedLine: Int,
    psiMethod: PsiMethod,
    psiFile: PsiFile,
): Boolean {
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
 * Checks if the caret is within the given PsiElement.
 *
 * @param psiElement PSI element of interest
 * @param caret the current (primary) caret that did the click
 * @return true if the caret is within the PSI element, false otherwise
 */
private fun withinElement(
    psiElement: PsiElement,
    caret: Caret,
): Boolean {
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
        "<html><b><font color='orange'>interface</font> ${psiClass.qualifiedName}</b></html>"
    } else if (isAbstractClass(psiClass)) {
        "<html><b><font color='orange'>abstract class</font> ${psiClass.qualifiedName}</b></html>"
    } else {
        "<html><b><font color='orange'>class</font> ${psiClass.qualifiedName}</b></html>"
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
        "<html><b><font color='orange'>default constructor</font></b></html>"
    } else if (psiMethod.isConstructor) {
        "<html><b><font color='orange'>constructor</font></b></html>"
    } else if (isMethodDefault(psiMethod)) {
        "<html><b><font color='orange'>default method</font> ${psiMethod.name}</b></html>"
    } else {
        "<html><b><font color='orange'>method</font> ${psiMethod.name}</b></html>"
    }
}

/**
 * Gets the current list of code types based on the given AnActionEvent.
 *
 * @param e The AnActionEvent representing the current action event.
 * @return An array containing the current code types. If no caret or PSI file is found, an empty array is returned.
 *         The array contains the class display name, method display name (if present), and the line number (if present).
 *         The line number is prefixed with "Line".
 */
fun getCurrentListOfCodeTypes(e: AnActionEvent): Array<*>? {
    val result: ArrayList<String> = arrayListOf()
    val caret: Caret = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret ?: return result.toArray()
    val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE) ?: return result.toArray()

    if (psiFile !is PsiJavaFile) return result.toArray()

    val psiClass: PsiClass? = getSurroundingClass(psiFile, caret)

    psiClass ?: return null

    val psiMethod: PsiMethod? = getSurroundingMethod(psiFile, caret)
    val line: Int? = getSurroundingLine(psiFile, caret)?.plus(1)

    result.add(getClassDisplayName(psiClass))
    psiMethod?.let { result.add(getMethodDisplayName(it)) }
    line?.let { result.add("<html><b><font color='orange'>line</font> $line</b></html>") }

    return result.toArray()
}

/**
 * Retrieves a list of test samples from the given project.
 *
 * @param project The project to retrieve the test samples from.
 * @return A list of strings, representing the names of the test samples.
 */
fun getTestSamples(project: Project): List<String> {
    val testSamples = listOf<String>()

    val projectFileIndex: ProjectFileIndex = ProjectRootManager.getInstance(project).fileIndex
    val javaFileType: FileType = FileTypeManager.getInstance().getFileTypeByExtension("java")

    projectFileIndex.iterateContent { file ->
        if (file.fileType === javaFileType) {
            WriteCommandAction.runWriteCommandAction(project) {
                val psiJavaFile = (PsiManager.getInstance(project).findFile(file) as PsiJavaFile)
                val psiClass = psiJavaFile.classes[
                    psiJavaFile.classes.stream().map { it.name }.toArray()
                        .indexOf(psiJavaFile.name.removeSuffix(".java"))]
                if (isTestClass(psiClass)) {
                    testSamples.plus(psiClass.name)
                }
            }
        }
        true
    }
    return testSamples
}

fun isTestClass(psiClass: PsiClass): Boolean {
    // Get all methods of your class
    val methods = psiClass.allMethods

    // Iterate over methods
    methods.forEach { method ->
        // Get list of method annotations
        val annotations = method.modifierList.annotations
        // Check if '@Test' (from JUnit) annotation exists
        annotations.forEach { annotation ->
            if (annotation.qualifiedName == "org.junit.jupiter.api.Test" || annotation.qualifiedName == "org.junit.Test") {
                return true
            }
        }
    }

    return false
}
