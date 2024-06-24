package org.jetbrains.research.testspark.kotlin

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.asJava.toLightClass
import org.jetbrains.kotlin.idea.base.psi.kotlinFqName
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.refactoring.isInterfaceClass
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.DescriptorToSourceUtils
import org.jetbrains.research.testspark.core.data.ClassType
import org.jetbrains.research.testspark.core.utils.importPattern
import org.jetbrains.research.testspark.core.utils.packagePattern
import org.jetbrains.research.testspark.langwrappers.PsiClassWrapper
import org.jetbrains.research.testspark.langwrappers.PsiMethodWrapper

class KotlinPsiClassWrapper(private val psiClass: KtClassOrObject) : PsiClassWrapper {
    override val name: String get() = psiClass.name ?: ""

    override val qualifiedName: String get() = psiClass.fqName!!.asString() ?: ""

    override val text: String? get() = psiClass.text

    override val methods: List<PsiMethodWrapper>
        get() = psiClass.body?.functions?.filter { it.name != null }?.map { KotlinPsiMethodWrapper(it) } ?: emptyList()

    override val allMethods: List<PsiMethodWrapper> get() = methods

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
            return if (psiClass.fqName != null) {
                superClassPsiClass?.let { KotlinPsiClassWrapper(it) }
            } else {
                null
            }
        }

    override val virtualFile: VirtualFile get() = psiClass.containingFile.virtualFile

    override val containingFile: PsiFile get() = psiClass.containingFile

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
            return when {
                psiClass is KtObjectDeclaration -> ClassType.OBJECT
                psiClass.isInterfaceClass() -> ClassType.INTERFACE
                psiClass.hasModifier(KtTokens.ABSTRACT_KEYWORD) -> ClassType.ABSTRACT_CLASS
                psiClass.isData() -> ClassType.DATA_CLASS
                psiClass.annotationEntries.any { it.text == "@JvmInline" } -> ClassType.INLINE_VALUE_CLASS
                else -> ClassType.CLASS
            }
        }

    override fun searchSubclasses(project: Project): Collection<PsiClassWrapper> {
        val scope = GlobalSearchScope.projectScope(project)
        val lightClass = psiClass.toLightClass()
        return if (lightClass != null) {
            val query = ClassInheritorsSearch.search(lightClass, scope, false)
            query.findAll().filter { it.kotlinFqName != null }.map { KotlinPsiClassWrapper(it as KtClass) }
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
                if (psiClass != null && psiClass.fqName != null && !psiClass.fqName.toString().startsWith("kotlin.")) {
                    interestingPsiClasses.add(KotlinPsiClassWrapper(psiClass))
                }
            }
        }

        interestingPsiClasses.add(this)
        return interestingPsiClasses
    }
}
