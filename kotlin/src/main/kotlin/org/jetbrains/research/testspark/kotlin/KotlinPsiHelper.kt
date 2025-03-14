package org.jetbrains.research.testspark.kotlin

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.research.testspark.core.test.SupportedLanguage
import org.jetbrains.research.testspark.core.test.data.CodeType
import org.jetbrains.research.testspark.langwrappers.PsiClassWrapper
import org.jetbrains.research.testspark.langwrappers.PsiHelper
import org.jetbrains.research.testspark.langwrappers.PsiMethodWrapper

class KotlinPsiHelper(
    private val psiFile: PsiFile,
) : PsiHelper(psiFile) {
    override val language: SupportedLanguage get() = SupportedLanguage.Kotlin

    override val languagePrefix: String get() = "kotlin."

    /**
     * When dealing with Kotlin PSI files, we expect that only classes, their methods,
     * top-level functions are tested.
     * Therefore, we expect either a class or a method (top-level function) to surround a cursor offset.
     *
     * This requirement ensures that the user is not trying
     * to generate tests for a line of code outside the aforementioned scopes.
     *
     * @param e `AnActionEvent` representing the current action event.
     * @return `true` if the cursor is inside a class or method, `false` otherwise.
     */
    override fun availableForGeneration(e: AnActionEvent): Boolean =
        getCurrentListOfCodeTypes(e).any { (it.first == CodeType.CLASS) || (it.first == CodeType.METHOD) }

    override fun getSurroundingClass(caretOffset: Int): PsiClassWrapper? {
        val element = psiFile.findElementAt(caretOffset)
        val cls = element?.parentOfType<KtClassOrObject>(withSelf = true)

        if (cls != null && cls.name != null && cls.fqName != null) {
            val kotlinClassWrapper = KotlinPsiClassWrapper(cls)
            if (kotlinClassWrapper.isTestableClass()) {
                log.info("Surrounding class for caret in $caretOffset is ${kotlinClassWrapper.qualifiedName}")
                return kotlinClassWrapper
            }
        }

        log.info("No surrounding class for caret in $caretOffset")
        return null
    }

    override fun getSurroundingMethod(caretOffset: Int): PsiMethodWrapper? {
        val element = psiFile.findElementAt(caretOffset)
        val method = element?.parentOfType<KtFunction>(withSelf = true)

        if (method != null && method.name != null) {
            val wrappedMethod = KotlinPsiMethodWrapper(method)
            if (wrappedMethod.isTestableMethod()) {
                log.info("Surrounding method for caret at $caretOffset is ${wrappedMethod.methodDescriptor}")
                return wrappedMethod
            }
        }

        log.info("No surrounding method for caret at $caretOffset")
        return null
    }

    override fun getInterestingPsiClassesWithQualifiedNames(
        cut: PsiClassWrapper?,
        psiMethod: PsiMethodWrapper,
    ): MutableSet<PsiClassWrapper> {
        val interestingPsiClasses =
            cut?.getInterestingPsiClassesWithQualifiedNames(psiMethod)
                ?: (psiMethod as KotlinPsiMethodWrapper).getInterestingPsiClassesWithQualifiedNames()
        log.info("There are ${interestingPsiClasses.size} interesting psi classes from method ${psiMethod.methodDescriptor}")
        return interestingPsiClasses
    }

    override fun getPackageName() = (psiFile as KtFile).packageFqName.asString()

    override fun getMethodHTMLDisplayName(psiMethod: PsiMethodWrapper): String {
        psiMethod as KotlinPsiMethodWrapper
        return when {
            psiMethod.isTopLevelFunction -> formatAsHTMLHighlightedWithAdditionalText("top-level function", psiMethod.name)
            psiMethod.isSecondaryConstructor -> formatAsHTMLHighlighted("secondary constructor")
            psiMethod.isPrimaryConstructor -> formatAsHTMLHighlighted("constructor")
            psiMethod.isDefaultMethod -> formatAsHTMLHighlightedWithAdditionalText("default method", psiMethod.name)
            else -> formatAsHTMLHighlightedWithAdditionalText("method", psiMethod.name)
        }
    }
}
