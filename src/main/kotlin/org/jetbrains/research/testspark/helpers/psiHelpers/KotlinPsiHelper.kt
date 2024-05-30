package org.jetbrains.research.testspark.helpers.psiHelpers

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import com.intellij.util.Query
import org.jetbrains.kotlin.asJava.toLightClass
import org.jetbrains.kotlin.asJava.toLightMethods
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.caches.resolve.resolveToDescriptorIfAny
import org.jetbrains.kotlin.idea.refactoring.fqName.getKotlinFqName
import org.jetbrains.kotlin.idea.util.module
import org.jetbrains.kotlin.load.kotlin.toSourceElement
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.DescriptorToSourceUtils
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode
import org.jetbrains.research.testspark.core.utils.importPattern
import org.jetbrains.research.testspark.core.utils.packagePattern
import org.jetbrains.research.testspark.tools.llm.SettingsArguments
import java.util.stream.Collectors

class KotlinPsiMethodWrapper(val psiFunction: KtNamedFunction) : PsiMethodWrapper {
    override val name: String
        get() = psiFunction.name ?: ""

    override val methodDescriptor: String
        get() {
            val parameterTypes = psiFunction.valueParameters.joinToString("") { generateFieldType(it.typeReference) }
            val returnType = generateReturnDescriptor(psiFunction)
            return "${psiFunction.name}($parameterTypes)$returnType"
        }

    override val signature: String
        get() {
            val bodyStart = psiFunction.bodyExpression?.startOffset ?: psiFunction.textLength
            return psiFunction.text.substring(0, bodyStart).replace('\n', ' ').trim()
        }

    override val text: String? = psiFunction.text

    override val containingClass: PsiClassWrapper? =
        psiFunction.parentOfType<KtClass>()?.let { KotlinPsiClassWrapper(it) }

    override val containingFile: PsiFile = psiFunction.containingFile

    override fun isLineIn(lineNumber: Int): Boolean {
        val psiFile = psiFunction.containingFile
        val document = PsiDocumentManager.getInstance(psiFile.project).getDocument(psiFile) ?: return false
        val textRange = psiFunction.textRange
        val startLine = document.getLineNumber(textRange.startOffset) + 1
        val endLine = document.getLineNumber(textRange.endOffset) + 1
        return lineNumber in startLine..endLine
    }

    val body: KtExpression? = psiFunction.bodyExpression

    /**
     * Generates the return descriptor for a method.
     *
     * @param psiFunction the function
     * @return the return descriptor
     */
    private fun generateReturnDescriptor(psiFunction: KtNamedFunction): String {
        val returnType = psiFunction.typeReference?.text ?: "Unit"
        return generateFieldType(returnType)
    }

    /**
     * Generates the field descriptor for a type.
     *
     * @param typeReference the type reference to generate the descriptor for
     * @return the field descriptor
     */
    private fun generateFieldType(typeReference: KtTypeReference?): String {
        val type = typeReference?.text ?: "Unit"
        return generateFieldType(type)
    }

    /**
     * Generates the field descriptor for a type.
     *
     * @param type the type to generate the descriptor for
     * @return the field descriptor
     */
    private fun generateFieldType(type: String): String {
        return when (type) {
            "Int" -> "I"
            "Long" -> "J"
            "Float" -> "F"
            "Double" -> "D"
            "Boolean" -> "Z"
            "Byte" -> "B"
            "Char" -> "C"
            "Short" -> "S"
            "Unit" -> "V"
            else -> "L${type.replace('.', '/')};"
        }
    }
}

class KotlinPsiClassWrapper(private val psiClass: KtClass) : PsiClassWrapper {
    override val name: String get() = psiClass.name ?: ""

    override val methods: List<PsiMethodWrapper>
        get() = psiClass.body?.functions?.map { KotlinPsiMethodWrapper(it) } ?: emptyList()

    override val allMethods: List<PsiMethodWrapper> get() = methods

    override val qualifiedName: String get() = psiClass.fqName?.asString() ?: ""

    override val text: String? get() = psiClass.text

    override val fullText: String
        get() {
            var fullText = ""
            val fileText = psiClass.containingFile.text

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
            fullText += psiClass.text

            return fullText
        }

    override val superClass: PsiClassWrapper?
        get() {
            // Get the superTypeListEntries of the Kotlin class
            val superTypeListEntries = psiClass.superTypeListEntries
            // Find the superclass entry (if any)
            val superClassEntry = superTypeListEntries.firstOrNull()
            // Resolve the superclass type reference to a PsiClass
            val superClassTypeReference = superClassEntry?.typeReference
            val superClassDescriptor = superClassTypeReference?.let {
                val bindingContext = it.analyze()
                bindingContext[BindingContext.TYPE, it]
            }
            val superClassPsiClass = superClassDescriptor?.constructor?.declarationDescriptor?.let { descriptor ->
                DescriptorToSourceUtils.getSourceFromDescriptor(descriptor) as? KtClass
            }
            // Wrap the resolved PsiClass in KotlinPsiClassWrapper (or equivalent)
            return superClassPsiClass?.let { KotlinPsiClassWrapper(it) }
        }


    override val virtualFile: VirtualFile get() = psiClass.containingFile.virtualFile

    override val containingFile: PsiFile get() = psiClass.containingFile

    override fun searchSubclasses(project: Project): Collection<PsiClassWrapper> {
        val scope = GlobalSearchScope.projectScope(project)
        val lightClass = psiClass.toLightClass()
        return if (lightClass != null) {
            val query = ClassInheritorsSearch.search(lightClass, scope, false)
            query.findAll().map { KotlinPsiClassWrapper(it as KtClass) }
        } else {
            emptyList()
        }
    }

    override fun getInterestingPsiClassesWithQualifiedNames(
        psiMethod: PsiMethodWrapper,
    ): MutableSet<PsiClassWrapper> {
        val interestingPsiClasses = mutableSetOf<PsiClassWrapper>()
        val method = psiMethod as KotlinPsiMethodWrapper

        method.psiFunction.valueParameters.forEach { parameter ->
            val typeReference = parameter.typeReference
            if (typeReference != null) {
                val psiClass = PsiTreeUtil.getParentOfType(typeReference, KtClass::class.java)
                if (psiClass != null && !psiClass.fqName.toString().startsWith("kotlin.")) {
                    interestingPsiClasses.add(KotlinPsiClassWrapper(psiClass))
                }
            }
        }

        interestingPsiClasses.add(this)
        return interestingPsiClasses
    }
}

class KotlinPsiHelper(private val psiFile: PsiFile) : PsiHelper {

    override val language: String get() = "Kotlin"

    private val log = Logger.getInstance(this::class.java)

    override fun generateMethodDescriptor(psiMethod: PsiMethodWrapper): String {
        val methodDescriptor = psiMethod.methodDescriptor
        log.info("Method description: $methodDescriptor")
        return methodDescriptor
    }

    override fun getSurroundingClass(caretOffset: Int): PsiClassWrapper? {
        val classElements = PsiTreeUtil.findChildrenOfAnyType(psiFile, KtClass::class.java)
        for (cls in classElements) {
            if (withinElement(cls, caretOffset)) {
                val kotlinClassWrapper = KotlinPsiClassWrapper(cls)
                log.info("Surrounding class for caret in $caretOffset is ${kotlinClassWrapper.qualifiedName}")
                return kotlinClassWrapper
            }
        }
        log.info("No surrounding class for caret in $caretOffset")
        return null
    }

    override fun getSurroundingMethod(caretOffset: Int): PsiMethodWrapper? {
        val methodElements = PsiTreeUtil.findChildrenOfAnyType(psiFile, KtNamedFunction::class.java)
        for (method in methodElements) {
            if (withinElement(method, caretOffset)) {
                val surroundingClass = PsiTreeUtil.getParentOfType(method, KtClass::class.java) ?: continue
                val surroundingClassWrapper = KotlinPsiClassWrapper(surroundingClass)
                if (surroundingClassWrapper.isValid()) {
                    val kotlinMethod = KotlinPsiMethodWrapper(method)
                    log.info("Surrounding method for caret in $caretOffset is ${kotlinMethod.methodDescriptor}")
                    return kotlinMethod
                }
            }
        }
        log.info("No surrounding method for caret in $caretOffset")
        return null
    }

    override fun getSurroundingLine(caretOffset: Int): Int? {
        val psiMethod = (getSurroundingMethod(caretOffset) ?: return null) as KotlinPsiMethodWrapper
        val doc = PsiDocumentManager.getInstance(psiFile.project).getDocument(psiFile) ?: return null

        val selectedLine = doc.getLineNumber(caretOffset)
        val selectedLineText =
            doc.getText(TextRange(doc.getLineStartOffset(selectedLine), doc.getLineEndOffset(selectedLine)))

        if (selectedLineText.isBlank() || !validateLine(selectedLine, psiMethod, psiFile)) {
            log.info("Line $selectedLine at caret $caretOffset is not valid")
            return null
        }
        log.info("Surrounding line at caret $caretOffset is $selectedLine")
        return selectedLine
    }

    override fun collectClassesToTest(
        project: Project,
        classesToTest: MutableList<PsiClassWrapper>,
        psiHelper: PsiHelper,
        caretOffset: Int,
    ) {
        val maxPolymorphismDepth = SettingsArguments(project).maxPolyDepth(0)
        val cutPsiClass = psiHelper.getSurroundingClass(caretOffset)!!
        var currentPsiClass = cutPsiClass
        for (index in 0 until maxPolymorphismDepth) {
            if (!classesToTest.contains(currentPsiClass)) {
                classesToTest.add(currentPsiClass)
            }

            if (currentPsiClass.superClass == null ||
                currentPsiClass.superClass!!.qualifiedName.startsWith("kotlin.")
            ) {
                break
            }
            currentPsiClass = currentPsiClass.superClass!!
        }
        log.info("There are ${classesToTest.size} classes to test")
    }

    override fun getInterestingPsiClassesWithQualifiedNames(
        psiMethod: PsiMethodWrapper,
    ): MutableSet<PsiClassWrapper> {
        return (psiMethod as KotlinPsiMethodWrapper).getInterestingPsiClassesWithQualifiedNames(psiMethod)
    }

    override fun getClassesWithSelectedLineAsString(
        caret: Caret,
        classesToTest: List<PsiClassWrapper>,
    ): List<String> {
        val result: MutableList<String> = mutableListOf()
        for (psiClassWrapper in classesToTest) {
            val methods = psiClassWrapper.methods
            for (psiMethodWrapper in methods) {
                val className = psiClassWrapper.qualifiedName
                val methodName = psiMethodWrapper.methodDescriptor
                val lineNumber = getSurroundingLine(caret.offset)

                val resultElement = "${className}::${methodName}:$lineNumber"
                result.add(resultElement)
            }
        }
        return result
    }

    override fun hasKotlinCode(psiFile: PsiFile): Boolean {
        return PsiTreeUtil.findChildrenOfAnyType(psiFile, KtFile::class.java).isNotEmpty()
    }

    private fun withinElement(element: PsiElement, offset: Int): Boolean {
        return offset in element.textRange.startOffset..element.textRange.endOffset
    }

    private fun validateLine(selectedLine: Int, psiMethod: KtNamedFunction, psiFile: PsiFile): Boolean {
        val doc = PsiDocumentManager.getInstance(psiFile.project).getDocument(psiFile) ?: return false
        val bodyExpression = psiMethod.bodyExpression ?: return false
        if (bodyExpression.children.isEmpty()) return false

        val firstStatement = bodyExpression.children.first()
        val lastStatement = bodyExpression.children.last()

        val firstStatementLine = doc.getLineNumber(firstStatement.textRange.startOffset)
        val lastStatementLine = doc.getLineNumber(lastStatement.textRange.endOffset)

        return selectedLine in firstStatementLine..lastStatementLine
    }

}

