package org.jetbrains.research.testspark.helpers.psi.kotlin

import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import org.jetbrains.kotlin.idea.refactoring.isInterfaceClass
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtPrimaryConstructor
import org.jetbrains.kotlin.psi.KtSecondaryConstructor
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.research.testspark.helpers.psi.PsiClassWrapper
import org.jetbrains.research.testspark.helpers.psi.PsiMethodWrapper

class KotlinPsiMethodWrapper(val psiFunction: KtFunction) : PsiMethodWrapper {

    override val name: String get() = psiFunction.name ?: ""

    override val text: String? = psiFunction.text

    override val containingClass: PsiClassWrapper? = psiFunction.run {
        parentOfType<KtClass>()?.let { KotlinPsiClassWrapper(it) }
    }

    override val containingFile: PsiFile = psiFunction.containingFile

    override val methodDescriptor: String
        get() = psiFunction.run {
            val parameterTypes = valueParameters.joinToString("") { generateFieldType(it.typeReference) }
            val returnType = generateReturnDescriptor(psiFunction)
            return "$name($parameterTypes)$returnType"
        }

    override val signature: String
        get() = psiFunction.run {
            val bodyStart = bodyExpression?.startOffsetInParent ?: textLength
            text.substring(0, bodyStart).replace('\n', ' ').trim()
        }

    val parameterList = psiFunction.valueParameterList

    val isPrimaryConstructor: Boolean = psiFunction is KtPrimaryConstructor

    val isSecondaryConstructor: Boolean = psiFunction is KtSecondaryConstructor

    val isTopLevelFunction: Boolean = psiFunction.containingClassOrObject == null

    val isDefaultMethod: Boolean = psiFunction.run {
        val containingClass = PsiTreeUtil.getParentOfType(this, KtClassOrObject::class.java)
        val containingInterface = containingClass?.isInterfaceClass()
        name != "<init>" && bodyExpression != null && containingInterface == true
    }

    override fun containsLine(lineNumber: Int): Boolean {
        val psiFile = psiFunction.containingFile
        val document = PsiDocumentManager.getInstance(psiFile.project).getDocument(psiFile) ?: return false
        val textRange = psiFunction.textRange
        val startLine = document.getLineNumber(textRange.startOffset) + 1
        val endLine = document.getLineNumber(textRange.endOffset) + 1
        return lineNumber in startLine..endLine
    }

    /**
     * Generates the return descriptor for a method.
     *
     * @param psiFunction the function
     * @return the return descriptor
     */
    private fun generateReturnDescriptor(psiFunction: KtFunction): String {
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
