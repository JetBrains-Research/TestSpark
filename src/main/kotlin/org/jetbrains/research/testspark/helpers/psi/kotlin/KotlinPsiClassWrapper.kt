package org.jetbrains.research.testspark.helpers.psi.kotlin

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiAnonymousClass
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.asJava.toLightClass
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.DescriptorToSourceUtils
import org.jetbrains.research.testspark.core.data.ClassType
import org.jetbrains.research.testspark.core.utils.importPattern
import org.jetbrains.research.testspark.core.utils.packagePattern
import org.jetbrains.research.testspark.helpers.psi.PsiClassWrapper
import org.jetbrains.research.testspark.helpers.psi.PsiMethodWrapper

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

    override val classType: ClassType
        get() {
            if (psiClass.isInterface()) {
                return ClassType.INTERFACE
            }
            if (psiClass.hasModifier(KtTokens.ABSTRACT_KEYWORD)) {
                return ClassType.ABSTRACT_CLASS
            }
            return ClassType.CLASS
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

    val isInterface: Boolean get() = psiClass.isInterface()

    val isAbstractClass: Boolean
        get() {
            psiClass.containingFile.virtualFile
            if (psiClass.isInterface()) return false

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

    /**
     * Checks if the constraints on the selected class are satisfied, so that EvoSuite can generate tests for it.
     * Namely, it is not an enum and not an anonymous inner class.
     *
     * @return true if the constraints are satisfied, false otherwise
     */
    fun isTestableClass(): Boolean {
        return !psiClass.isEnum() && psiClass !is PsiAnonymousClass
    }
}
