package org.jetbrains.research.testspark.java

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiTypesUtil
import org.jetbrains.research.testspark.core.test.SupportedLanguage
import org.jetbrains.research.testspark.core.test.data.CodeType
import org.jetbrains.research.testspark.langwrappers.PsiClassWrapper
import org.jetbrains.research.testspark.langwrappers.PsiHelper
import org.jetbrains.research.testspark.langwrappers.PsiMethodWrapper

class JavaPsiHelper(
    private val psiFile: PsiFile,
) : PsiHelper(psiFile) {
    override val language: SupportedLanguage get() = SupportedLanguage.Java

    override val languagePrefix: String get() = "java."

    /**
     * When dealing with Java PSI files, we expect that only classes and their methods are tested.
     * Therefore, we expect a **class** to surround a cursor offset.
     *
     * This requirement ensures that the user is not trying
     * to generate tests for a line of code outside the class scope.
     *
     * @param e `AnActionEvent` representing the current action event.
     * @return `true` if the cursor is inside a class, `false` otherwise.
     */
    override fun availableForGeneration(e: AnActionEvent): Boolean = getCurrentListOfCodeTypes(e).any { it.first == CodeType.CLASS }

    override fun getSurroundingClass(caretOffset: Int): PsiClassWrapper? {
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

    override fun getSurroundingMethod(caretOffset: Int): PsiMethodWrapper? {
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

    override fun collectInterestingPsiClassesFromMethod(
        methodIt: PsiMethodWrapper,
        currentLevelSetOfClasses: MutableSet<PsiClassWrapper>,
        interestingPsiClasses: MutableSet<PsiClassWrapper>
    ) {
        (methodIt as JavaPsiMethodWrapper).parameterList.parameters.forEach { paramIt ->
            PsiTypesUtil.getPsiClass(paramIt.type)?.let { typeIt ->
                JavaPsiClassWrapper(typeIt).let {
                    if (!it.qualifiedName.startsWith(languagePrefix)) {
                        interestingPsiClasses.add(it)
                        currentLevelSetOfClasses.add(it)
                    }
                }
            }
        }
    }

    override fun getInterestingPsiClassesWithQualifiedNames(
        cut: PsiClassWrapper?,
        psiMethod: PsiMethodWrapper,
    ): MutableSet<PsiClassWrapper> {
        // The cut is always not null for Java, because all functions are always inside the class
        val interestingPsiClasses = cut!!.getInterestingPsiClassesWithQualifiedNames(psiMethod)
        log.info("There are ${interestingPsiClasses.size} interesting psi classes from method ${psiMethod.methodDescriptor}")
        return interestingPsiClasses
    }

    override fun getPackageName() = (psiFile as PsiJavaFile).packageName

    override fun getMethodHTMLDisplayName(psiMethod: PsiMethodWrapper): String {
        psiMethod as JavaPsiMethodWrapper
        return when {
            psiMethod.isDefaultConstructor -> formatAsHTMLHighlighted("default constructor")
            psiMethod.isConstructor -> formatAsHTMLHighlighted("constructor")
            psiMethod.isMethodDefault -> formatAsHTMLHighlightedWithAdditionalText("default method", psiMethod.name)
            else -> formatAsHTMLHighlightedWithAdditionalText("method", psiMethod.name)
        }
    }

    private fun PsiElement.containsOffset(caretOffset: Int): Boolean =
        (textRange.startOffset <= caretOffset) && (textRange.endOffset >= caretOffset)
}
