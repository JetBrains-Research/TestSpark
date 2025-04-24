package org.jetbrains.research.testspark.langwrappers

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Document
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import org.jetbrains.research.testspark.core.generation.llm.ranker.Graph
import org.jetbrains.research.testspark.core.test.SupportedLanguage
import org.jetbrains.research.testspark.core.test.data.CodeType

typealias CodeTypeDisplayName = Pair<CodeType, String>

/**
 * Interface that declares all the methods needed for parsing and
 * handling the PSI (Program Structure Interface) for different languages.
 */
abstract class PsiHelper(
    private val psiFile: PsiFile,
) {
    abstract val language: SupportedLanguage

    abstract val languagePrefix: String

    protected val log = Logger.getInstance(this::class.java)

    protected fun formatAsHTMLHighlighted(text: String) = "<html><b><font color='orange'>$text</font></b></html>"

    protected fun formatAsHTMLHighlightedWithAdditionalText(
        highlightedText: String,
        additionalText: String,
    ) = "<html><b><font color='orange'>$highlightedText</font> $additionalText</b></html>"

    /**
     * Checks if a code construct is valid for unit test generation at the given caret offset.
     *
     * @param e The AnActionEvent representing the current action event.
     * @return `true` if a code construct is valid for unit test generation at the caret offset, `false` otherwise.
     */
    abstract fun availableForGeneration(e: AnActionEvent): Boolean

    /**
     * Returns the surrounding PsiClass object based on the caret position within the specified PsiFile.
     * The surrounding class is determined by finding the PsiClass objects within the PsiFile and checking
     * if the caret is within any of them.
     *
     * @param caretOffset The offset of the caret position within the PsiFile.
     * @return The surrounding `PsiClass` object if found, `null` otherwise.
     */
    abstract fun getSurroundingClass(caretOffset: Int): PsiClassWrapper?

    /**
     * Returns the surrounding method of the given PSI file based on the caret offset.
     *
     * @param caretOffset The caret offset within the PSI file.
     * @return The surrounding method if found, otherwise null.
     */
    abstract fun getSurroundingMethod(caretOffset: Int): PsiMethodWrapper?

    /**
     * Returns the line number of the selected line where the caret is positioned.
     *
     * The returned line number is **1-based**.
     *
     * @param caretOffset The caret offset within the PSI file.
     * @return The line number of the selected line, otherwise null.
     */
    fun getSurroundingLineNumber(caretOffset: Int): Int? {
        val doc = PsiDocumentManager.getInstance(psiFile.project).getDocument(psiFile) ?: return null

        /**
         * See `getLineNumber`'s documentation for details on the numbering.
         * It returns an index of the line in the document, starting from 0.
         *
         * Therefore, we need to increase the result by one to get the line number.
         */
        var selectedLine = doc.getLineNumber(caretOffset)
        val selectedLineText =
            doc.getText(TextRange(doc.getLineStartOffset(selectedLine), doc.getLineEndOffset(selectedLine)))

        // increase by one is necessary due to different start of numbering
        selectedLine++

        if (selectedLineText.isBlank()) {
            log.info("Line $selectedLine at caret $caretOffset is blank")
            return null
        }
        log.info("Surrounding line at caret $caretOffset is $selectedLine")

        return selectedLine
    }

    /**
     * Retrieves a set of interesting PsiClasses based on a given project,
     * a list of classes to test, and a depth reducing factor.
     *
     * @param project The project in which to search for interesting classes.
     * @param classesToTest The list of classes to test for interesting PsiClasses.
     * @return The set of interesting PsiClasses found during the search.
     */
    fun getInterestingPsiClassesWithQualifiedNames(
        classesToTest: List<PsiClassWrapper>,
        maxInputParamsDepth: Int,
    ): MutableSet<PsiClassWrapper> {
        val interestingPsiClasses: MutableSet<PsiClassWrapper> = mutableSetOf()

        var currentLevelClasses =
            mutableListOf<PsiClassWrapper>().apply { addAll(classesToTest) }

        repeat(maxInputParamsDepth) {
            val currentLevelSetOfClasses = mutableSetOf<PsiClassWrapper>()

            currentLevelClasses.forEach { classIt ->
                classIt.methods.forEach { methodIt ->
                    val currentMethodSetOfClasses = methodIt.getInterestingPsiClassesWithQualifiedNames()
                    interestingPsiClasses.addAll(currentMethodSetOfClasses)
                    currentLevelSetOfClasses.addAll(currentMethodSetOfClasses)
                }
            }
            currentLevelClasses = mutableListOf<PsiClassWrapper>().apply { addAll(currentLevelSetOfClasses) }
            interestingPsiClasses.addAll(currentLevelSetOfClasses)
        }
        log.info("There are ${interestingPsiClasses.size} interesting psi classes")
        return interestingPsiClasses.toMutableSet()
    }

    /**
     * Returns a set of interesting PsiClasses based on the given PsiMethod.
     *
     * @param cut The class under test.
     * @param psiMethod The PsiMethod for which to find interesting PsiClasses.
     * @return A mutable set of interesting PsiClasses.
     */
    abstract fun getInterestingPsiClassesWithQualifiedNames(
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
    fun getCurrentListOfCodeTypes(e: AnActionEvent): List<CodeTypeDisplayName> {
        val result: ArrayList<CodeTypeDisplayName> = arrayListOf()
        val caret: Caret =
            e.dataContext
                .getData(CommonDataKeys.CARET)
                ?.caretModel
                ?.primaryCaret ?: return result

        val psiClassWrapped = getSurroundingClass(caret.offset)
        val psiMethodWrapped = getSurroundingMethod(caret.offset)
        val line: Int? = getSurroundingLineNumber(caret.offset)

        psiClassWrapped?.let { result.add(CodeType.CLASS to getClassHTMLDisplayName(it)) }
        psiMethodWrapped?.let { result.add(CodeType.METHOD to getMethodHTMLDisplayName(it)) }
        line?.let { result.add(CodeType.LINE to getLineHTMLDisplayName(it)) }

        log.info(
            "The test can be generated for: \n " +
                " 1) Class ${psiClassWrapped?.qualifiedName ?: "no class"} \n" +
                " 2) Method ${psiMethodWrapped?.name ?: "no method"} \n" +
                " 3) Line $line",
        )

        return result
    }

    /**
     * Retrieves a PsiMethod matching the given method descriptor within the provided PsiClass.
     *
     * @param psiClass The PsiClassWrapper in which to search for the method.
     * @param methodDescriptor The method descriptor to match against.
     * @return The matching PsiMethod if found, otherwise an empty string.
     */
    fun getPsiMethod(
        psiClass: PsiClassWrapper?,
        methodDescriptor: String,
        caret: Int,
    ): PsiMethodWrapper? {
        // Processing function outside the class
        if (psiClass == null) {
            val currentPsiMethod = getSurroundingMethod(caret)!!
            return currentPsiMethod
        }
        for (currentPsiMethod in psiClass.allMethods) {
            if (generateMethodDescriptor(currentPsiMethod) == methodDescriptor) {
                return currentPsiMethod
            }
        }
        return null
    }

    /**
     * Returns the method descriptor of the method containing the given line number in the specified PsiClass.
     *
     * @param psiClass the PsiClassWrapper containing the method
     * @param lineNumber the line number within the file where the method is located
     * @return the method descriptor as `String` if the surrounding method exists, or `null` when no method found
     */
    fun getMethodDescriptor(
        psiClass: PsiClassWrapper?,
        lineNumber: Int,
        caret: Int,
    ): String? {
        if (psiClass != null) {
            val containingPsiMethod = psiClass.allMethods.find { it.containsLine(lineNumber) } ?: return null
            return generateMethodDescriptor(containingPsiMethod)
        } else {
            val currentPsiMethod = getSurroundingMethod(caret) ?: return null
            return generateMethodDescriptor(currentPsiMethod)
        }
    }

    /**
     * Helper for generating method descriptors for methods.
     *
     * @param psiMethod The method to extract the descriptor from.
     * @return The method descriptor.
     */
    fun generateMethodDescriptor(psiMethod: PsiMethodWrapper): String {
        val methodDescriptor = psiMethod.methodDescriptor
        log.info("Method description: $methodDescriptor")
        return methodDescriptor
    }

    /**
     * Fills the classesToTest variable with the data about the classes to test.
     *
     * @param project The project in which to collect classes to test.
     * @param classesToTest The list of classes to test.
     * @param caretOffset The caret offset in the file.
     * @param maxPolymorphismDepth Check if cut has any user-defined superclass
     */
    fun collectClassesToTest(
        classesToTest: MutableList<PsiClassWrapper>,
        caretOffset: Int,
        maxPolymorphismDepth: Int,
    ) {
        val cutPsiClass = getSurroundingClass(caretOffset) ?: return
        // will be null for the top level function
        var currentPsiClass = cutPsiClass
        for (index in 0 until maxPolymorphismDepth) {
            if (!classesToTest.contains(currentPsiClass)) {
                classesToTest.add(currentPsiClass)
            }

            if (currentPsiClass.superClass == null ||
                currentPsiClass.superClass!!.qualifiedName.startsWith(languagePrefix)
            ) {
                break
            }
            currentPsiClass = currentPsiClass.superClass!!
        }
        log.info("There are ${classesToTest.size} classes to test")
    }

    /**
     * Get the package name of the file.
     */
    abstract fun getPackageName(): String

    /**
     * Get the module of the file.
     */
    fun getModuleFromPsiFile(): com.intellij.openapi.module.Module? = ModuleUtilCore.findModuleForFile(psiFile.virtualFile, psiFile.project)

    /**
     * Get the module of the file.
     */
    fun getDocumentFromPsiFile(): Document? = psiFile.fileDocument

    /**
     * Gets the display line number.
     * This is used when displaying the name of a method in the GenerateTestsActionMethod menu entry.
     *
     * @param line The line number.
     * @return The display name of the line.
     */
    fun getLineHTMLDisplayName(line: Int): String = formatAsHTMLHighlightedWithAdditionalText("line", line.toString())

    /**
     * Gets the display name of a class.
     * This is used when displaying the name of a class in the GenerateTestsActionClass menu entry.
     *
     * @param psiClass The PSI class of interest.
     * @return The display name of the PSI class.
     */
    fun getClassHTMLDisplayName(psiClass: PsiClassWrapper): String =
        formatAsHTMLHighlightedWithAdditionalText(psiClass.classType.representation, psiClass.qualifiedName)

    /**
     * Gets the display name of a method, depending on if it is a (default) constructor or a normal method.
     * This is used when displaying the name of a method in the GenerateTestsActionMethod menu entry.
     *
     * @param psiMethod The PSI method of interest.
     * @return The display name of the PSI method.
     */
    abstract fun getMethodHTMLDisplayName(psiMethod: PsiMethodWrapper): String

    abstract fun createGraph(
        graph: Graph,
        classesToTest: List<PsiClassWrapper>,
        interestingClasses: Set<PsiClassWrapper>,
        psiMethod: PsiMethodWrapper? = null,
    ): Graph

    abstract fun getPsiClassFromFqName(fqName: String): PsiClassWrapper?
}
