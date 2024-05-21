package org.jetbrains.research.testspark.helpers.psiHelpers

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod

/**
 * Interface that declares all the methods needed for parsing and handling the PSI (Program Structure Interface) for different languages.
 */
interface PsiHelperInterface {
    /**
     * Helper for generating method descriptors for methods.
     *
     * @param psiMethod the method to extract the descriptor from
     * @return the method descriptor
     */
    fun generateMethodDescriptor(psiMethod: PsiMethod): String

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
    fun getSurroundingClass(psiFile: PsiFile, caretOffset: Int): PsiClass?

    /**
     * Returns the surrounding method of the given PSI file based on the caret offset.
     *
     * @param psiFile The PSI file in which to search for the surrounding method.
     * @param caretOffset The caret offset within the PSI file.
     * @return The surrounding method if found, otherwise null.
     */
    fun getSurroundingMethod(psiFile: PsiFile, caretOffset: Int): PsiMethod?

    /**
     * Returns the line number of the selected line where the caret is positioned.
     *
     * @param psiFile the PSI file where the caret is positioned
     * @param caretOffset the caret offset within the PSI file
     * @return the line number of the selected line, or null if unable to determine
     */
    fun getSurroundingLine(psiFile: PsiFile, caretOffset: Int): Int?

    /**
     * Gets the current list of code types based on the given AnActionEvent.
     *
     * @param e The AnActionEvent representing the current action event.
     * @return An array containing the current code types. If no caret or PSI file is found, an empty array is returned.
     *         The array contains the class display name, method display name (if present), and the line number (if present).
     *         The line number is prefixed with "Line".
     */
    fun getCurrentListOfCodeTypes(e: AnActionEvent): Array<*>?
}
