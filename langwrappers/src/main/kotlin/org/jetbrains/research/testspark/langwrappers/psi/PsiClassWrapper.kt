package org.jetbrains.research.testspark.langwrappers.psi

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import org.jetbrains.research.testspark.core.data.ClassType

/**
 * Interface representing a wrapper for PSI classes,
 * providing a common API to handle class-related data for different languages.
 * @property name The name of a class
 * @property qualifiedName The qualified name of the class.
 * @property text The text of the class.
 * @property methods All methods in the class
 * @property allMethods All methods in the class and all its superclasses
 * @property constructorSignatures The signatures of all constructors in the class
 * @property superClass The superclass of the class
 * @property virtualFile Virtual file where the class is located
 * @property containingFile File where the method is located
 * @property fullText The source code of the class (with package and imports).
 * @property classType The type of the class
 * @property rBrace The offset of the closing brace
 * */
interface PsiClassWrapper {
    val name: String
    val qualifiedName: String
    val text: String?
    val methods: List<PsiMethodWrapper>
    val allMethods: List<PsiMethodWrapper>
    val constructorSignatures: List<String>
    val superClass: PsiClassWrapper?
    val virtualFile: VirtualFile
    val containingFile: PsiFile
    val fullText: String
    val classType: ClassType
    val rBrace: Int?

    /**
     * Searches for subclasses of the current class within the given project.
     *
     * @param project The project within which to search for subclasses.
     * @return A collection of found subclasses.
     */
    fun searchSubclasses(project: Project): Collection<PsiClassWrapper>

    /**
     * Retrieves a set of interesting PSI classes based on a given method.
     *
     * @param psiMethod The method to use for finding interesting PSI classes.
     * @return A mutable set of interesting PSI classes.
     */
    fun getInterestingPsiClassesWithQualifiedNames(psiMethod: PsiMethodWrapper): MutableSet<PsiClassWrapper>
}