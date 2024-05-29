package org.jetbrains.research.testspark.helpers

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiModifier
import org.jetbrains.research.testspark.core.data.ClassType

/**
 * Helper class for processing PsiClass objects and determining their types.
 */
object PsiClassHelper {
    /**
     * Returns the type of PsiClass instance.
     *
     * @return the type of the PsiClass instance, represented by the ClassType enum.
     */
    fun PsiClass.getClassType(): ClassType {
        if (this.isInterface) {
            return ClassType.INTERFACE
        }
        if (this.hasModifierProperty(PsiModifier.ABSTRACT)) {
            return ClassType.ABSTRACT_CLASS
        }
        return ClassType.CLASS
    }

    /**
     * Returns the representation of the type of a PsiClass instance.
     *
     * @return the representation of the type of the PsiClass instance.
     */
    fun PsiClass.getClassTypeName(): String = getClassType().representation

    /**
     * Returns the display name of a PsiClass instance.
     *
     * @return the display name of the PsiClass instance in HTML format.
     */
    fun PsiClass.getClassDisplayName(): String =
        "<html><b><font color='orange'>${getClassTypeName()}</font> ${this.qualifiedName}</b></html>"
}
