package org.jetbrains.research.testspark.langwrappers.psi

import com.intellij.lang.Language
import com.intellij.lang.LanguageExtension
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.jetbrains.research.testspark.core.test.SupportedLanguage
import org.jetbrains.research.testspark.core.test.data.CodeType

typealias CodeTypeDisplayName = Pair<CodeType, String>

/**
 * Interface that declares all the methods needed for parsing and
 * handling the PSI (Program Structure Interface) for different languages.
 */
interface PsiHelper {
    val language: SupportedLanguage

    /**
     * Checks if a code construct is valid for unit test generation at the given caret offset.
     *
     * @param e The AnActionEvent representing the current action event.
     * @return `true` if a code construct is valid for unit test generation at the caret offset, `false` otherwise.
     */
    fun availableForGeneration(e: AnActionEvent): Boolean

    /**
     * Returns the surrounding PsiClass object based on the caret position within the specified PsiFile.
     * The surrounding class is determined by finding the PsiClass objects within the PsiFile and checking
     * if the caret is within any of them.
     *
     * @param caretOffset The offset of the caret position within the PsiFile.
     * @return The surrounding `PsiClass` object if found, `null` otherwise.
     */
    fun getSurroundingClass(caretOffset: Int): PsiClassWrapper?

    /**
     * Returns the surrounding method of the given PSI file based on the caret offset.
     *
     * @param caretOffset The caret offset within the PSI file.
     * @return The surrounding method if found, otherwise null.
     */
    fun getSurroundingMethod(caretOffset: Int): PsiMethodWrapper?

    /**
     * Returns the line number of the selected line where the caret is positioned.
     *
     * The returned line number is **1-based**.
     *
     * @param caretOffset The caret offset within the PSI file.
     * @return The line number of the selected line, otherwise null.
     */
    fun getSurroundingLineNumber(caretOffset: Int): Int?

    /**
     * Retrieves a set of interesting PsiClasses based on a given project,
     * a list of classes to test, and a depth reducing factor.
     *
     * @param project The project in which to search for interesting classes.
     * @param classesToTest The list of classes to test for interesting PsiClasses.
     * @param polyDepthReducing The factor to reduce the polymorphism depth.
     * @return The set of interesting PsiClasses found during the search.
     */
    fun getInterestingPsiClassesWithQualifiedNames(
        project: Project,
        classesToTest: List<PsiClassWrapper>,
        polyDepthReducing: Int,
        maxInputParamsDepth: Int,
    ): MutableSet<PsiClassWrapper>

    /**
     * Returns a set of interesting PsiClasses based on the given PsiMethod.
     *
     * @param cut The class under test.
     * @param psiMethod The PsiMethod for which to find interesting PsiClasses.
     * @return A mutable set of interesting PsiClasses.
     */
    fun getInterestingPsiClassesWithQualifiedNames(
        cut: PsiClassWrapper?,
        psiMethod: PsiMethodWrapper,
    ): MutableSet<PsiClassWrapper>

    /**
     * Gets the current list of code types based on the given AnActionEvent.
     *
     * @param e The AnActionEvent representing the current action event.
     * @return An array containing the current code types. If no caret or PSI file is found, an empty array is returned.
     *         The array contains the class display name, method display name (if present), and the line number (if present).
     *         The line number is prefixed with "Line".
     */
    fun getCurrentListOfCodeTypes(e: AnActionEvent): List<CodeTypeDisplayName>

    /**
     * Helper for generating method descriptors for methods.
     *
     * @param psiMethod The method to extract the descriptor from.
     * @return The method descriptor.
     */
    fun generateMethodDescriptor(psiMethod: PsiMethodWrapper): String

    /**
     * Fills the classesToTest variable with the data about the classes to test.
     *
     * @param project The project in which to collect classes to test.
     * @param classesToTest The list of classes to test.
     * @param caretOffset The caret offset in the file.
     * @param maxPolymorphismDepth Check if cut has any user-defined superclass
     */
    fun collectClassesToTest(
        project: Project,
        classesToTest: MutableList<PsiClassWrapper>,
        caretOffset: Int,
        maxPolymorphismDepth: Int,
    )

    /**
     * Get the package name of the file.
     */
    fun getPackageName(): String

    /**
     * Get the module of the file.
     */
    fun getModuleFromPsiFile(): com.intellij.openapi.module.Module

    /**
     * Get the module of the file.
     */
    fun getDocumentFromPsiFile(): Document?

    /**
     * Gets the display line number.
     * This is used when displaying the name of a method in the GenerateTestsActionMethod menu entry.
     *
     * @param line The line number.
     * @return The display name of the line.
     */
    fun getLineHTMLDisplayName(line: Int): String

    /**
     * Gets the display name of a class.
     * This is used when displaying the name of a class in the GenerateTestsActionClass menu entry.
     *
     * @param psiClass The PSI class of interest.
     * @return The display name of the PSI class.
     */
    fun getClassHTMLDisplayName(psiClass: PsiClassWrapper): String

    /**
     * Gets the display name of a method, depending on if it is a (default) constructor or a normal method.
     * This is used when displaying the name of a method in the GenerateTestsActionMethod menu entry.
     *
     * @param psiMethod The PSI method of interest.
     * @return The display name of the PSI method.
     */
    fun getMethodHTMLDisplayName(psiMethod: PsiMethodWrapper): String
}

/**
 * This is the provider interface for a PsiHelper. The PsiHelper allows for
 * custom handling or manipulating PSI (Program Structure Interface) elements.
 */
interface PsiHelperProvider {

    /**
     * Get a PsiHelper for the given file.
     *
     * @param file the PsiFile to get the PsiHelper for.
     * @return a PsiHelper object.
     */
    fun getPsiHelper(file: PsiFile): PsiHelper

    companion object {
        // An extension point that allows for custom PsiHelperProviders to be registered for different languages
        private val EP = LanguageExtension<PsiHelperProvider>("org.jetbrains.research.testgenie.psiHelperProvider")

        /**
         * Retrieves a PsiHelper for the given file based on its language.
         *
         * It attempts to get the PsiHelperProvider registered for the specified language.
         * If none exists, the method will return null.
         * Finally, it uses this PsiHelperProvider to get a PsiHelper for the file.
         *
         * @param file The PsiFile to get the PsiHelper for.
         * @return The PsiHelper for the file or null if it couldn't be obtained.
         */
        fun getPsiHelper(file: PsiFile): PsiHelper? {
            val language: Language = file.language
            return EP.forLanguage(language)?.getPsiHelper(file)
        }
    }
}
