package nl.tudelft.ewi.se.ciselab.testgenie.services

import com.intellij.openapi.editor.Document
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

class StaticInvalidationService {

    // HashMap of Class filePath -> (HashMap of Method Signature -> Method Body)
    private var savedMethods: HashMap<String, HashMap<String, ArrayList<PsiElement>>> = HashMap()

    private fun validateMethod(
        signature: String,
        body: ArrayList<PsiElement>,
        methods: HashMap<String, ArrayList<PsiElement>>
    ): Boolean {
        val savedBody = methods.get(signature)

        // if body doesn't exist, method seen first time
        // if amount of elements in body different, method surely changed
        if (savedBody == null || body.size != savedBody.size) {
            methods.put(signature, body)
            return false
        }
        // compare each element (no whitespace)
        body.zip(savedBody).forEach {
            if (!it.first.text.equals(it.second.text)) {
                methods.put(signature, body)
                return true
            }
        }
        return false
    }

    private fun validateClass(
        filePath: String,
        methods: HashMap<String, ArrayList<PsiElement>>,
        className: String
    ): MutableSet<String> {
        // return true
        val methodsToDiscard = mutableSetOf<String>()

        // Get
        val methodsSaved = savedMethods.getOrPut("$filePath/$className") { methods }
        methods.keys.forEach {
            val modified = validateMethod(it, methods.get(it)!!, methodsSaved)
            if (modified) {
                methodsToDiscard.add(it)
            }
        }

        return methodsToDiscard
    }

    /**
     * Method to invalidate the changed parts of a cache.
     *
     * @param fileUrl the url of a file
     * @param lines the lines to invalidate tests
     * @param cache the c
     */
    fun invalidateCacheLines(fileUrl: String, lines: Set<Int>, cache: TestCaseCachingService) {
        for (line in lines) {
            cache.invalidateFromCache(fileUrl, line + 1, line + 1)
        }
    }

    fun invalidate(file: PsiFile): Set<Int> {
        val linesToDiscard: MutableSet<Int> = mutableSetOf()
        val filePath = file.virtualFile.presentableUrl
        val children = file.children
        val classToValidate = file.children.filterIsInstance<PsiClass>()
        val doc: Document = PsiDocumentManager.getInstance(file.project).getDocument(file) ?: return setOf()
        classToValidate.forEach {
            val className = it.name!!
            val methods = it.methods
            val map: HashMap<String, ArrayList<PsiElement>> = HashMap()
            methods.forEach {
                map.put(it.hierarchicalMethodSignature.toString(), recursePsiMethodBody(it.body!!))
            }
            val methodsToDiscard = validateClass(filePath, map, className)
            methods.forEach {
                if (methodsToDiscard.contains(it.hierarchicalMethodSignature.toString())) {
                    val first = doc.getLineNumber(it.body!!.statements.first().startOffset)
                    val last = doc.getLineNumber(it.body!!.statements.last().endOffset)
                    linesToDiscard.addAll(first.rangeTo(last))
                }
            }
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
