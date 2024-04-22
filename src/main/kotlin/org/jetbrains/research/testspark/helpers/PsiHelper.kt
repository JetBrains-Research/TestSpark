package org.jetbrains.research.testspark.helpers

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiAnonymousClass
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiCodeBlock
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile
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

// Implementation of functions related to PSI
class PsiHelper {
    companion object {
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
         * Returns the surrounding PsiClass object based on the caret position within the specified PsiFile.
         * The surrounding class is determined by finding the PsiClass objects within the PsiFile and checking
         * if the caret is within any of them. Additionally, the found class should satisfy the constraints
         * specified by the validateClass() function.
         *
         * @param psiFile The PsiFile object containing the class hierarchy.
         * @param caretOffset The offset of the caret position within the PsiFile.
         * @return The surrounding PsiClass object if found, null otherwise.
         */
        fun getSurroundingClass(
            psiFile: PsiFile,
            caretOffset: Int,
        ): PsiClass? {
            // Get the classes of the PSI file
            val classElements = PsiTreeUtil.findChildrenOfAnyType(psiFile, PsiClass::class.java)

            // Get the surrounding PSI class (i.e. the cursor has to be within that class)
            var surroundingClass: PsiClass? = null
            for (psiClass: PsiClass in classElements) {
                if (withinElement(psiClass, caretOffset)) {
                    // Check the constraints on a class
                    if (!validateClass(psiClass)) continue
                    surroundingClass = psiClass
                }
            }
            return surroundingClass
        }

        /**
         * Returns the surrounding method of the given PSI file based on the caret offset.
         *
         * @param psiFile The PSI file in which to search for the surrounding method.
         * @param caretOffset The caret offset within the PSI file.
         * @return The surrounding method if found, otherwise null.
         */
        fun getSurroundingMethod(
            psiFile: PsiFile,
            caretOffset: Int,
        ): PsiMethod? {
            // Get the methods of the PSI file
            val methodElements = PsiTreeUtil.findChildrenOfAnyType(psiFile, PsiMethod::class.java)

            // Get the surrounding PSI method (i.e. the cursor has to be within that method)
            var surroundingMethod: PsiMethod? = null
            for (method: PsiMethod in methodElements) {
                if (isMethodConcrete(method) && withinElement(method, caretOffset)) {
                    val surroundingClass: PsiClass =
                        PsiTreeUtil.getParentOfType(method, PsiClass::class.java) ?: continue
                    // Check the constraints on the surrounding class
                    if (!validateClass(surroundingClass)) continue
                    surroundingMethod = method
                }
            }
            return surroundingMethod
        }

        /**
         * Returns the line number of the selected line where the caret is positioned.
         *
         * @param psiFile the PSI file where the caret is positioned
         * @param caretOffset the caret offset within the PSI file
         * @return the line number of the selected line, or null if unable to determine
         */
        fun getSurroundingLine(
            psiFile: PsiFile,
            caretOffset: Int,
        ): Int? {
            val psiMethod: PsiMethod = getSurroundingMethod(psiFile, caretOffset) ?: return null

            val doc: Document = PsiDocumentManager.getInstance(psiFile.project).getDocument(psiFile) ?: return null

            val selectedLine: Int = doc.getLineNumber(caretOffset)
            val selectedLineText: String =
                doc.getText(TextRange(doc.getLineStartOffset(selectedLine), doc.getLineEndOffset(selectedLine)))

            if (selectedLineText.isBlank()) return null

            if (!validateLine(selectedLine, psiMethod, psiFile)) return null
            return selectedLine
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
            val caret: Caret =
                e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret ?: return result.toArray()
            val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE) ?: return result.toArray()

            if (psiFile !is PsiJavaFile) return result.toArray()

            val psiClass: PsiClass? = getSurroundingClass(psiFile, caret.offset)

            psiClass ?: return null

            val psiMethod: PsiMethod? = getSurroundingMethod(psiFile, caret.offset)
            val line: Int? = getSurroundingLine(psiFile, caret.offset)?.plus(1)

            result.add(getClassDisplayName(psiClass))
            psiMethod?.let { result.add(getMethodDisplayName(it)) }
            line?.let { result.add("<html><b><font color='orange'>line</font> $line</b></html>") }

            return result.toArray()
        }

        /**
         * Generates the return descriptor for a method.
         *
         * @param psiMethod the method
         * @return the return descriptor
         */
        private fun generateReturnDescriptor(psiMethod: PsiMethod): String {
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
        private fun generateFieldType(psiType: PsiType): String {
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
         * Checks if a method is concrete (non-abstract in case of an abstract class and non-default in case of an interface).
         *
         * @param psiMethod the PSI method to check
         * @return true if the method has a body (thus, is concrete), false otherwise
         */
        private fun isMethodConcrete(psiMethod: PsiMethod): Boolean {
            return psiMethod.body != null
        }

        /**
         * Checks if the method is a default method of an interface.
         *
         * @param psiMethod the PSI method of interest
         * @return true if the method is a default method of an interface, false otherwise
         */
        private fun isMethodDefault(psiMethod: PsiMethod): Boolean {
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
         * Determines whether a given PsiElement contains a specified caret offset.
         *
         * @param psiElement The PsiElement to check if it contains the caret offset.
         * @param caretOffset The caret offset to check.
         * @return `true` if the PsiElement contains the caret offset, `false` otherwise.
         */
        private fun withinElement(
            psiElement: PsiElement,
            caretOffset: Int,
        ): Boolean {
            return (psiElement.startOffset <= caretOffset) && (psiElement.endOffset >= caretOffset)
        }

        /**
         * Gets the display name of a class, depending on if it is a normal class, an abstract class or an interface.
         * This is used when displaying the name of a class in GenerateTestsActionClass menu entry.
         *
         * @param psiClass the PSI class of interest
         * @return the display name of the PSI class
         */
        private fun getClassDisplayName(psiClass: PsiClass): String {
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
        private fun getMethodDisplayName(psiMethod: PsiMethod): String {
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
    }
}
