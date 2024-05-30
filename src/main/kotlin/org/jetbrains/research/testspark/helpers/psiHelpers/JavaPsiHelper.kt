package org.jetbrains.research.testspark.helpers.psiHelpers

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiAnonymousClass
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiCodeBlock
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiSubstitutor
import com.intellij.psi.PsiType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiTypesUtil
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import com.intellij.util.containers.stream
import org.jetbrains.research.testspark.core.utils.importPattern
import org.jetbrains.research.testspark.core.utils.packagePattern
import org.jetbrains.research.testspark.tools.llm.SettingsArguments
import java.util.stream.Collectors

class JavaPsiMethodWrapper(private val psiMethod: PsiMethod) : PsiMethodWrapper {
    override val name: String get() = psiMethod.name

    override val methodDescriptor: String
        get() {
            val parameterTypes =
                psiMethod.getSignature(PsiSubstitutor.EMPTY)
                    .parameterTypes
                    .stream()
                    .map { i -> generateFieldType(i) }
                    .collect(Collectors.joining())

            val returnType = generateReturnDescriptor(psiMethod)

            return "${psiMethod.name}($parameterTypes)$returnType"
        }

    override val signature: String
        get() {
            val bodyStart = psiMethod.body?.startOffsetInParent ?: psiMethod.textLength
            return psiMethod.text.substring(0, bodyStart).replace('\n', ' ').trim()
        }

    override val text: String? = psiMethod.text

    override val containingClass: PsiClassWrapper? = psiMethod.containingClass?.let { JavaPsiClassWrapper(it) }

    override val containingFile: PsiFile = psiMethod.containingFile

    val parameterList = psiMethod.parameterList

    val isConstructor: Boolean = psiMethod.isConstructor

    val body: PsiCodeBlock? = psiMethod.body

    val isMethodDefault: Boolean
        get() {
            if (psiMethod.body == null) return false
            return psiMethod.containingClass?.isInterface ?: return false
        }

    val isDefaultConstructor: Boolean get() = psiMethod.isConstructor && (psiMethod.body?.isEmpty ?: false)

    override fun containsLine(lineNumber: Int): Boolean {
        val psiFile = psiMethod.containingFile ?: return false
        val document = PsiDocumentManager.getInstance(psiFile.project).getDocument(psiFile) ?: return false
        val textRange = psiMethod.textRange
        val startLine = document.getLineNumber(textRange.startOffset) + 1
        val endLine = document.getLineNumber(textRange.endOffset) + 1
        return lineNumber in startLine..endLine
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
}

class JavaPsiClassWrapper(private val psiClass: PsiClass) : PsiClassWrapper {
    override val name: String get() = psiClass.name ?: ""

    override val qualifiedName: String get() = psiClass.qualifiedName ?: ""

    override val text: String get() = psiClass.text

    override val methods: List<PsiMethodWrapper> get() = psiClass.methods.map { JavaPsiMethodWrapper(it) }

    override val allMethods: List<PsiMethodWrapper> get() = psiClass.allMethods.map { JavaPsiMethodWrapper(it) }

    override val superClass: PsiClassWrapper? get() = psiClass.superClass?.let { JavaPsiClassWrapper(it) }

    override val virtualFile: VirtualFile get() = psiClass.containingFile.virtualFile

    override val containingFile: PsiFile get() = psiClass.containingFile

    override val fullText: String
        get() {
            var fullText = ""
            val fileText = psiClass.containingFile.text

            // get package
            packagePattern.findAll(fileText).map {
                it.groupValues[0]
            }.forEach {
                fullText += "$it\n\n"
            }

            // get imports
            importPattern.findAll(fileText).map {
                it.groupValues[0]
            }.forEach {
                fullText += "$it\n"
            }

            // Add class code
            fullText += psiClass.text

            return fullText
        }

    val isInterface: Boolean get() = psiClass.isInterface

    val isAbstractClass: Boolean
        get() {
            psiClass.containingFile.virtualFile
            if (psiClass.isInterface) return false

            val methods = PsiTreeUtil.findChildrenOfType(psiClass, PsiMethod::class.java)
            for (psiMethod: PsiMethod in methods) {
                if (psiMethod.body == null) {
                    return true
                }
            }

            // check if a class is noted as abstract in the text
            return psiClass.text.replace(" ", "")
                .contains("abstractclass${psiClass.name}", ignoreCase = true)
        }

    override fun searchSubclasses(project: Project): Collection<PsiClassWrapper> {
        val scope = GlobalSearchScope.projectScope(project)
        val query = ClassInheritorsSearch.search(psiClass, scope, false)
        return query.findAll().map { JavaPsiClassWrapper(it) }
    }

    override fun getInterestingPsiClassesWithQualifiedNames(
        psiMethod: PsiMethodWrapper,
    ): MutableSet<PsiClassWrapper> {
        val interestingMethods = mutableSetOf(psiMethod as JavaPsiMethodWrapper)
        for (currentPsiMethod in allMethods) {
            if ((currentPsiMethod as JavaPsiMethodWrapper).isConstructor) interestingMethods.add(currentPsiMethod)
        }
        val interestingPsiClasses = mutableSetOf(this)
        interestingMethods.forEach { methodIt ->
            methodIt.parameterList.parameters.forEach { paramIt ->
                PsiTypesUtil.getPsiClass(paramIt.type)?.let { typeIt ->
                    JavaPsiClassWrapper(typeIt).let {
                        if (it.qualifiedName != "" && !it.qualifiedName.startsWith("java.")) {
                            interestingPsiClasses.add(it)
                        }
                    }
                }
            }
        }

        return interestingPsiClasses.toMutableSet()
    }

    /**
     * Checks if the constraints on the selected class are satisfied, so that EvoSuite can generate tests for it.
     * Namely, it is not an enum and not an anonymous inner class.
     *
     * @return true if the constraints are satisfied, false otherwise
     */
    fun isTestableClass(): Boolean {
        return !psiClass.isEnum && psiClass !is PsiAnonymousClass
    }
}

class JavaPsiHelper(private val psiFile: PsiFile) : PsiHelper {

    override val language: Language get() = Language.Java

    private val log = Logger.getInstance(this::class.java)

    override fun generateMethodDescriptor(
        psiMethod: PsiMethodWrapper,
    ): String {
        val methodDescriptor = psiMethod.methodDescriptor
        log.info("Method description: $methodDescriptor")
        return methodDescriptor
    }

    override fun getSurroundingClass(
        caretOffset: Int,
    ): PsiClassWrapper? {
        val classElements = PsiTreeUtil.findChildrenOfAnyType(psiFile, PsiClass::class.java)
        for (cls in classElements) {
            if (cls.containsOffset(caretOffset)) {
                val javaClassWrapper = JavaPsiClassWrapper(cls)
                if (javaClassWrapper.isTestableClass()) {
                    log.info("Surrounding class for caret in $caretOffset is ${javaClassWrapper.qualifiedName}")
                    return javaClassWrapper
                }
            }
        }
        log.info("No surrounding class for caret in $caretOffset")
        return null
    }

    override fun getSurroundingMethod(
        caretOffset: Int,
    ): PsiMethodWrapper? {
        val methodElements = PsiTreeUtil.findChildrenOfAnyType(psiFile, PsiMethod::class.java)
        for (method in methodElements) {
            if (method.body != null && method.containsOffset(caretOffset)) {
                val surroundingClass =
                    PsiTreeUtil.getParentOfType(method, PsiClass::class.java) ?: continue
                val surroundingClassWrapper = JavaPsiClassWrapper(surroundingClass)
                if (surroundingClassWrapper.isTestableClass()) {
                    val javaMethod = JavaPsiMethodWrapper(method)
                    log.info("Surrounding method for caret in $caretOffset is ${javaMethod.methodDescriptor}")
                    return javaMethod
                }
            }
        }
        log.info("No surrounding method for caret in $caretOffset")
        return null
    }

    override fun getSurroundingLine(
        caretOffset: Int,
    ): Int? {
        val doc = PsiDocumentManager.getInstance(psiFile.project).getDocument(psiFile) ?: return null

        val selectedLine = doc.getLineNumber(caretOffset)
        val selectedLineText =
            doc.getText(TextRange(doc.getLineStartOffset(selectedLine), doc.getLineEndOffset(selectedLine)))

        if (selectedLineText.isBlank()) {
            log.info("Line $selectedLine at caret $caretOffset is blank")
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
        // check if cut has any none java super class
        val maxPolymorphismDepth = SettingsArguments(project).maxPolyDepth(0)

        val cutPsiClass = psiHelper.getSurroundingClass(caretOffset)!!
        var currentPsiClass = cutPsiClass
        for (index in 0 until maxPolymorphismDepth) {
            if (!classesToTest.contains(currentPsiClass)) {
                classesToTest.add(currentPsiClass)
            }

            if (currentPsiClass.superClass == null ||
                currentPsiClass.superClass!!.qualifiedName.startsWith("java.")
            ) {
                break
            }
            currentPsiClass = currentPsiClass.superClass!!
        }
        log.info("There are ${classesToTest.size} classes to test")
    }

    override fun getInterestingPsiClassesWithQualifiedNames(
        project: Project,
        classesToTest: List<PsiClassWrapper>,
        polyDepthReducing: Int,
    ): MutableSet<PsiClassWrapper> {
        val interestingPsiClasses: MutableSet<JavaPsiClassWrapper> = mutableSetOf()

        var currentLevelClasses =
            mutableListOf<PsiClassWrapper>().apply { addAll(classesToTest) }

        repeat(SettingsArguments(project).maxInputParamsDepth(polyDepthReducing)) {
            val tempListOfClasses = mutableSetOf<JavaPsiClassWrapper>()

            currentLevelClasses.forEach { classIt ->
                classIt.methods.forEach { methodIt ->
                    (methodIt as JavaPsiMethodWrapper).parameterList.parameters.forEach { paramIt ->
                        PsiTypesUtil.getPsiClass(paramIt.type)?.let { typeIt ->
                            JavaPsiClassWrapper(typeIt).let {
                                if (!it.qualifiedName.startsWith("java.")) {
                                    interestingPsiClasses.add(it)
                                }
                            }
                        }
                    }
                }
            }
            currentLevelClasses = mutableListOf<PsiClassWrapper>().apply { addAll(tempListOfClasses) }
            interestingPsiClasses.addAll(tempListOfClasses)
        }
        log.info("There are ${interestingPsiClasses.size} interesting psi classes")
        return interestingPsiClasses.toMutableSet()
    }

    override fun getInterestingPsiClassesWithQualifiedNames(
        cut: PsiClassWrapper,
        psiMethod: PsiMethodWrapper,
    ): MutableSet<PsiClassWrapper> {
        val interestingPsiClasses = cut.getInterestingPsiClassesWithQualifiedNames(psiMethod)
        log.info("There are ${interestingPsiClasses.size} interesting psi classes from method ${psiMethod.methodDescriptor}")
        return interestingPsiClasses
    }

    override fun getCurrentListOfCodeTypes(e: AnActionEvent): Array<*>? {
        val result: ArrayList<String> = arrayListOf()
        val caret: Caret =
            e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret ?: return result.toArray()

        val javaPsiClassWrapped = getSurroundingClass(caret.offset) as JavaPsiClassWrapper?
        val javaPsiMethodWrapped = getSurroundingMethod(caret.offset) as JavaPsiMethodWrapper?
        val line: Int? = getSurroundingLine(caret.offset)?.plus(1)

        javaPsiClassWrapped?.let { result.add(getClassDisplayName(it)) }
        javaPsiMethodWrapped?.let { result.add(getMethodDisplayName(it)) }
        line?.let { result.add(getLineDisplayName(it)) }

        if (javaPsiClassWrapped != null && javaPsiMethodWrapped != null) {
            log.info(
                "The test can be generated for: \n " +
                    " 1) Class ${javaPsiClassWrapped.qualifiedName} \n" +
                    " 2) Method ${javaPsiMethodWrapped.methodDescriptor}" +
                    " 3) Line $line",
            )
        }

        return result.toArray()
    }

    override fun getLineDisplayName(line: Int): String {
        return "<html><b><font color='orange'>line</font> $line</b></html>"
    }

    override fun getClassDisplayName(psiClass: PsiClassWrapper): String {
        return if ((psiClass as JavaPsiClassWrapper).isInterface) {
            "<html><b><font color='orange'>interface</font> ${psiClass.qualifiedName}</b></html>"
        } else if (psiClass.isAbstractClass) {
            "<html><b><font color='orange'>abstract class</font> ${psiClass.qualifiedName}</b></html>"
        } else {
            "<html><b><font color='orange'>class</font> ${psiClass.qualifiedName}</b></html>"
        }
    }

    override fun getMethodDisplayName(psiMethod: PsiMethodWrapper): String {
        return if ((psiMethod as JavaPsiMethodWrapper).isDefaultConstructor) {
            "<html><b><font color='orange'>default constructor</font></b></html>"
        } else if (psiMethod.isConstructor) {
            "<html><b><font color='orange'>constructor</font></b></html>"
        } else if (psiMethod.isMethodDefault) {
            "<html><b><font color='orange'>default method</font> ${psiMethod.name}</b></html>"
        } else {
            "<html><b><font color='orange'>method</font> ${psiMethod.name}</b></html>"
        }
    }

    private fun PsiElement.containsOffset(caretOffset: Int): Boolean {
        return (textRange.startOffset <= caretOffset) && (textRange.endOffset >= caretOffset)
    }
}
