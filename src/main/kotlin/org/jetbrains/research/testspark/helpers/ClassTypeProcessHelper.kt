package org.jetbrains.research.testspark.helpers

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.research.testspark.core.data.ClassType

object ClassTypeProcessHelper {
    /**
     * Returns the type of the given class.
     *
     * @param psiClass the PSI class for which to retrieve the type
     * @return the type of the class
     */
    fun getClassType(psiClass: PsiClass): ClassType {
        if (psiClass.isInterface) {
            return ClassType.INTERFACE
        }
        if (isAbstract(psiClass)) {
            return ClassType.ABSTRACT_CLASS
        }
        return ClassType.CLASS
    }

    /**
     * Returns the type of the given class as a string.
     *
     * @param psiClass the PSI class for which to retrieve the type
     * @return the type of the class as a string
     */
    private fun getClassTypeName(psiClass: PsiClass): String {
        val classType = getClassType(psiClass)
        return when (classType) {
            ClassType.INTERFACE -> "interface"
            ClassType.ABSTRACT_CLASS -> "abstract class"
            ClassType.CLASS -> "class"
        }
    }

    /**
     * Gets the display name of a class, depending on if it is a normal class, an abstract class or an interface.
     * This is used when displaying the name of a class in GenerateTestsActionClass menu entry.
     *
     * @param psiClass the PSI class of interest
     * @return the display name of the PSI class
     */
    fun getClassDisplayName(psiClass: PsiClass): String {
        val classTypeName = getClassTypeName(psiClass)
        return "<html><b><font color='orange'>$classTypeName</font> ${psiClass.qualifiedName}</b></html>"
    }

    /**
     * Checks if a PSI class is an abstract class.
     *
     * @param psiClass the PSI class of interest
     * @return true if the PSI class is an abstract class, false otherwise
     */
    private fun isAbstract(psiClass: PsiClass): Boolean {
        val methods = PsiTreeUtil.findChildrenOfType(psiClass, PsiMethod::class.java)
        for (psiMethod: PsiMethod in methods) {
            if (psiMethod.hasModifierProperty(PsiModifier.ABSTRACT)) {
                return true
            }
        }

        // check if a class is noted as abstract in the text
        return psiClass.text.replace(" ", "")
            .contains("abstractclass${psiClass.name}", ignoreCase = true)
    }
}
