package nl.tudelft.ewi.se.ciselab.testgenie.actions

import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset

/**
 * This class contains some useful methods related to GenerateTests actions.
 */
object GenerateTestsUtils {

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
            if (withinElement(method, caret)) {
                val surroundingClass: PsiClass = PsiTreeUtil.getParentOfType(method, PsiClass::class.java) ?: continue
                // Check the constraints on the surrounding class
                if (!validateClass(surroundingClass)) continue
                surroundingMethod = method
            }
        }
        return surroundingMethod
    }

    /**
     * Checks if the constraints on the selected class are satisfied, so that EvoSuite can generate tests for it.
     * Namely, it is a concrete class (non-abstract, not an interface, not an enum, not an anonymous inner class).
     *
     * @param psiClass the selected PSI class (where the user makes the click)
     * @return true if the constraints are satisfied, false otherwise
     */
    private fun validateClass(psiClass: PsiClass): Boolean {
        // The class cannot be an enum, interface or anonymous class
        if (psiClass.isEnum || psiClass.isInterface || psiClass is PsiAnonymousClass) return false

        // The psiClass cannot be abstract
        return checkNonAbstract(psiClass)
    }

    /**
     * Checks that the element is non-abstract (class or method).
     *
     * @param psiElement the selected PSI element (where the user makes a click)
     * @return true if the element is abstract, false otherwise
     */
    private fun checkNonAbstract(psiElement: PsiElement): Boolean {
        if (psiElement !is PsiClass && psiElement !is PsiMethod) return false

        val mark = psiElement.text.indexOf("(").coerceAtLeast(psiElement.text.indexOf("{"))
        val text = psiElement.text.substring(0, mark)
        return !text.contains("abstract ")
    }

    /**
     * Checks that the caret is within the given PsiElement.
     *
     * @param psiElement PSI element that has to be checked
     * @param caret the current (primary) caret that did the click
     * @return true if the caret is within the PSI element, false otherwise
     */
    private fun withinElement(psiElement: PsiElement, caret: Caret): Boolean {
        return (psiElement.startOffset <= caret.offset) && (psiElement.endOffset >= caret.offset)
    }

    /**
     * Gets the first (logical) line of the provided PSI element.
     *
     * @param psiElement the PSI element of interest
     * @return the first (logical, i.e. until \n character) line of the given PSI element
     */
    private fun getFirstLine(psiElement: PsiElement): String {
        val psiFile: PsiFile = psiElement.containingFile
        val doc: Document = PsiDocumentManager.getInstance(psiFile.project).getDocument(psiFile) ?: return ""
        val lineRange =
            TextRange(psiElement.startOffset, doc.getLineEndOffset(doc.getLineNumber(psiElement.startOffset)))
        return doc.getText(lineRange)
    }
}