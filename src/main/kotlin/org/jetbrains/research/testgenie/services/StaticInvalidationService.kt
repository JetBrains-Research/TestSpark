package org.jetbrains.research.testgenie.services

import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiCodeBlock
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiModifierList
import com.intellij.psi.PsiReferenceParameterList
import com.intellij.psi.PsiWhiteSpace
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import org.jetbrains.research.testgenie.actions.isMethodConcrete

/**
 * Service used to invalidate cache statically
 * @param project - the project
 */
class StaticInvalidationService(private val project: Project) {

    // HashMap of Class filePath -> (HashMap of Method Signature -> (Method Body, Covered Lines))
    private var savedMethods: HashMap<String, HashMap<String, Pair<ArrayList<PsiElement>, Set<Int>>>> = HashMap()

    /**
     * Method to invalidate the changed parts of a cache.
     *
     * @param fileUrl the url of a file
     * @param lines the lines to invalidate tests
     */
    fun invalidateCacheLines(fileUrl: String, lines: Set<Int>) {
        val cache = project.service<TestCaseCachingService>()
        for (line in lines) {
            cache.invalidateFromCache(fileUrl, line + 1, line + 1)
        }
    }

    /**
     * Checks for a file what lines have been modified
     * @param file the file in question
     * @return list of lines that have been modified
     */
    fun invalidate(file: PsiFile): Set<Int> {
        // Initialize list
        val linesToDiscard: MutableSet<Int> = mutableSetOf()
        // Get basic details
        val filePath = file.virtualFile.presentableUrl
        val classToValidate = file.children.filterIsInstance<PsiClass>()
        val doc: Document = PsiDocumentManager.getInstance(file.project).getDocument(file) ?: return setOf()
        // Go over all classes in file
        classToValidate.forEach { currentClass ->
            val className = currentClass.name!!
            val methods = currentClass.methods
            val map: HashMap<String, Pair<ArrayList<PsiElement>, Set<Int>>> = HashMap()
            methods.forEach {
                if (isMethodConcrete(it)) {
                    val startLine = doc.getLineNumber(it.startOffset)
                    val endLine = doc.getLineNumber(it.endOffset)
                    map[it.hierarchicalMethodSignature.toString()] =
                        Pair(recursePsiMethodBody(it.body!!), startLine.rangeTo(endLine).toSet())
                }
            }
            // validate each class
            linesToDiscard.addAll(validateClass(filePath, map, className))
        }

        return linesToDiscard
    }

    /**
     * Checks if method has been changed since last test generation (no, if first run)
     * Always updates the lines the method uses (lines can change due to whitespace, but method can still be valid)
     * @param signature the method in question
     * @param body list of the body-elements of the method, without whitespaces
     * @param methods hashmap of previously tested methods and their bodies
     * @return the lines of methods which have been changed (based on previous lines for method)
     */
    private fun validateMethod(
        signature: String,
        body: Pair<ArrayList<PsiElement>, Set<Int>>,
        methods: HashMap<String, Pair<ArrayList<PsiElement>, Set<Int>>>,
    ): Set<Int> {
        val savedBody = methods[signature]

        // if body doesn't exist, method seen first time
        // if amount of elements in body different, method surely changed
        if (savedBody == null) {
            methods[signature] = body
            return setOf()
        }
        if (body.first.size != savedBody.first.size) {
            methods[signature] = body
            return savedBody.second
        }
        // compare each element (no whitespace)
        body.first.zip(savedBody.first).forEach {
            if (!it.first.text.equals(it.second.text)) {
                methods[signature] = body
                return savedBody.second
            }
        }
        methods[signature] = Pair(savedBody.first, body.second)
        return setOf()
    }

    /**
     * Checks for class what methods within it have been modified
     * @param filePath path where the class is located
     * @param methods the methods of the class and the lines they cover
     * @param className the name of the class (used for preciser hashing)
     * @return the lines in the class that have been changed (based on previous lines for methods)
     */
    private fun validateClass(
        filePath: String,
        methods: HashMap<String, Pair<ArrayList<PsiElement>, Set<Int>>>,
        className: String,
    ): MutableSet<Int> {
        val linesToDiscard = mutableSetOf<Int>()

        // get old methods
        val methodsSaved = savedMethods.getOrPut("$filePath/$className") { methods }
        // validate each method against old methods
        methods.keys.forEach {
            val changed = validateMethod(it, methods[it]!!, methodsSaved)
            linesToDiscard.addAll(changed)
        }

        return linesToDiscard
    }

    /**
     * Returns a list of PsiElements that are part of the method psi children.
     *
     * @param psiMethodBody the psiBody of the method
     * @return the list of PsiElements
     */
    private fun recursePsiMethodBody(psiMethodBody: PsiCodeBlock): ArrayList<PsiElement> {
        val psiList: ArrayList<PsiElement> = arrayListOf()
        for (psiStatement in psiMethodBody.statements) {
            if (psiStatement.children.isEmpty()) {
                psiList.add(psiStatement)
                continue
            }
            for (psiElement in psiStatement.children) {
                recurseTree(psiElement, psiList)
            }
        }
        return psiList
    }

    /**
     * Append elements to the list of PsiElements
     *
     * @param psiElement the psi of the element
     * @param psiList list to append psi elements
     */
    private fun recurseTree(psiElement: PsiElement, psiList: ArrayList<PsiElement>) {
        if (psiElement is PsiWhiteSpace || psiElement is PsiReferenceParameterList || psiElement is PsiModifierList) {
            return
        }
        if (psiElement.children.isEmpty()) {
            psiList.add(psiElement)
            return
        }
        for (psiElementChild in psiElement.children) {
            recurseTree(psiElementChild, psiList)
        }
    }
}
