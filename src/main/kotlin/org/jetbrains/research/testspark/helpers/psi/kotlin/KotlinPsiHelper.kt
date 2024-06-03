package org.jetbrains.research.testspark.helpers.psi.kotlin

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
import org.jetbrains.kotlin.asJava.toLightClass
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.intentions.branchedTransformations.isNullExpressionOrEmptyBlock
import org.jetbrains.kotlin.idea.search.usagesSearch.constructor
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.DescriptorToSourceUtils
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode
import org.jetbrains.research.testspark.core.utils.importPattern
import org.jetbrains.research.testspark.core.utils.packagePattern
import org.jetbrains.research.testspark.helpers.psi.Language
import org.jetbrains.research.testspark.helpers.psi.PsiClassWrapper
import org.jetbrains.research.testspark.helpers.psi.PsiHelper
import org.jetbrains.research.testspark.helpers.psi.PsiMethodWrapper
import org.jetbrains.research.testspark.tools.llm.SettingsArguments

class KotlinPsiHelper(private val psiFile: PsiFile) : PsiHelper {

    override val language: Language get() = Language.Kotlin

    private val log = Logger.getInstance(this::class.java)

    override fun generateMethodDescriptor(psiMethod: PsiMethodWrapper): String {
        val methodDescriptor = psiMethod.methodDescriptor
        log.info("Method description: $methodDescriptor")
        return methodDescriptor
    }

    override fun getSurroundingClass(caretOffset: Int): PsiClassWrapper? {
        val classElements = PsiTreeUtil.findChildrenOfAnyType(psiFile, KtClass::class.java)
        for (cls in classElements) {
            if (cls.containsOffset(caretOffset)) {
                val kotlinClassWrapper = KotlinPsiClassWrapper(cls)
                if (kotlinClassWrapper.isTestableClass()) {
                    log.info("Surrounding class for caret in $caretOffset is ${kotlinClassWrapper.qualifiedName}")
                    return kotlinClassWrapper
                }
            }
        }
        log.info("No surrounding class for caret in $caretOffset")
        return null
    }

    override fun getSurroundingMethod(caretOffset: Int): PsiMethodWrapper? {
        val methodElements = PsiTreeUtil.findChildrenOfAnyType(psiFile, KtNamedFunction::class.java)
        for (method in methodElements) {
            if (method.containsOffset(caretOffset)) {
                val surroundingClass = PsiTreeUtil.getParentOfType(method, KtClass::class.java) ?: continue
                val surroundingClassWrapper = KotlinPsiClassWrapper(surroundingClass)
                if (surroundingClassWrapper.isTestableClass()) {
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
        val doc = PsiDocumentManager.getInstance(psiFile.project).getDocument(psiFile) ?: return null

        val selectedLine = doc.getLineNumber(caretOffset)
        val selectedLineText =
            doc.getText(TextRange(doc.getLineStartOffset(selectedLine), doc.getLineEndOffset(selectedLine)))

        if (selectedLineText.isBlank()) {
            log.info("Line $selectedLine at caret $caretOffset is not valid")
            return null
        }
        log.info("Surrounding line at caret $caretOffset is $selectedLine")
        return selectedLine
    }

    override fun getInterestingPsiClassesWithQualifiedNames(
        project: Project,
        classesToTest: List<PsiClassWrapper>,
        polyDepthReducing: Int,
    ): MutableSet<PsiClassWrapper> {
        val interestingPsiClasses: MutableSet<KotlinPsiClassWrapper> = mutableSetOf()

        var currentLevelClasses = mutableListOf<PsiClassWrapper>().apply { addAll(classesToTest) }

        repeat(SettingsArguments(project).maxInputParamsDepth(polyDepthReducing)) {
            val tempListOfClasses = mutableSetOf<KotlinPsiClassWrapper>()

            currentLevelClasses.forEach { classIt ->
                classIt.methods.forEach { methodIt ->
                    (methodIt as KotlinPsiMethodWrapper).parameterList?.parameters?.forEach { paramIt ->
                        val typeRef = paramIt.typeReference
                        if (typeRef != null) {
                            resolveClassInType(typeRef)?.let { psiClass ->
                                KotlinPsiClassWrapper(psiClass as KtClass).let {
                                    if (!it.qualifiedName.startsWith("kotlin.")) {
                                        interestingPsiClasses.add(it)
                                    }
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

        val ktClass = getSurroundingClass(caret.offset)
        val ktFunction = getSurroundingMethod(caret.offset)
        val line: Int? = getSurroundingLine(caret.offset)?.plus(1)

        ktClass?.let { result.add(getClassDisplayName(it)) }
        ktFunction?.let { result.add(getMethodDisplayName(it)) }
        line?.let { result.add(getLineDisplayName(it)) }

        if (ktClass != null && ktFunction != null) {
            log.info(
                "The test can be generated for: \n " +
                        " 1) Class ${ktClass.qualifiedName} \n" +
                        " 2) Method ${ktFunction.name}" +
                        " 3) Line $line",
            )
        }

        return result.toArray()
    }

    override fun collectClassesToTest(
        project: Project,
        classesToTest: MutableList<PsiClassWrapper>,
        caretOffset: Int,
    ) {
        val maxPolymorphismDepth = SettingsArguments(project).maxPolyDepth(0)
        val cutPsiClass = getSurroundingClass(caretOffset)!!
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

    override fun getLineDisplayName(line: Int): String {
        return "<html><b><font color='orange'>line</font> $line</b></html>"
    }

    override fun getClassDisplayName(psiClass: PsiClassWrapper): String {
        return if ((psiClass as KotlinPsiClassWrapper).isInterface) {
            "<html><b><font color='orange'>interface</font> ${psiClass.qualifiedName}</b></html>"
        } else if (psiClass.isAbstractClass) {
            "<html><b><font color='orange'>abstract class</font> ${psiClass.qualifiedName}</b></html>"
        } else {
            "<html><b><font color='orange'>class</font> ${psiClass.qualifiedName}</b></html>"
        }
    }

    override fun getMethodDisplayName(psiMethod: PsiMethodWrapper): String {
        return if ((psiMethod as KotlinPsiMethodWrapper).isDefaultConstructor) {
            "<html><b><font color='orange'>default constructor</font></b></html>"
        } else if (psiMethod.isConstructor) {
            "<html><b><font color='orange'>constructor</font></b></html>"
        } else if (psiMethod.isMethodDefault) {
            "<html><b><font color='orange'>default method</font> ${psiMethod.name}</b></html>"
        } else {
            "<html><b><font color='orange'>method</font> ${psiMethod.name}</b></html>"
        }
    }

    private fun PsiElement.containsOffset(offset: Int): Boolean {
        return offset in textRange.startOffset..textRange.endOffset
    }

    private fun resolveClassInType(typeReference: KtTypeReference): PsiClass? {
        val context = typeReference.analyze(BodyResolveMode.PARTIAL)
        val type = context[BindingContext.TYPE, typeReference] ?: return null
        val classDescriptor = type.constructor.declarationDescriptor as? ClassDescriptor ?: return null
        return (DescriptorToSourceUtils.getSourceFromDescriptor(classDescriptor) as? KtClass)?.toLightClass()
    }
}
