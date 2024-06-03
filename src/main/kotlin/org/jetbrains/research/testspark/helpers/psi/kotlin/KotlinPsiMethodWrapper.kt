package org.jetbrains.research.testspark.helpers.psi.kotlin

import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import org.jetbrains.kotlin.idea.intentions.branchedTransformations.isNullExpressionOrEmptyBlock
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.research.testspark.helpers.psi.PsiClassWrapper
import org.jetbrains.research.testspark.helpers.psi.PsiMethodWrapper

class KotlinPsiMethodWrapper(val psiFunction: KtNamedFunction) : PsiMethodWrapper {
    override val name: String
        get() = psiFunction.name ?: ""

    override val text: String? = psiFunction.text

    override val containingClass: PsiClassWrapper? =
        psiFunction.parentOfType<KtClass>()?.let { KotlinPsiClassWrapper(it) }

    override val containingFile: PsiFile = psiFunction.containingFile

    override val methodDescriptor: String
        get() {
            val parameterTypes = psiFunction.valueParameters.joinToString("") { generateFieldType(it.typeReference) }
            val returnType = generateReturnDescriptor(psiFunction)
            return "${psiFunction.name}($parameterTypes)$returnType"
        }

    override val signature: String
        get() {
            val bodyStart = psiFunction.bodyExpression?.startOffsetInParent ?: psiFunction.textLength
            return psiFunction.text.substring(0, bodyStart).replace('\n', ' ').trim()
        }

    val parameterList = psiFunction.valueParameterList

    val isConstructor: Boolean =
        psiFunction.name == "<init>" || (psiFunction.parent as? KtClass)?.name == psiFunction.name

    val isMethodDefault: Boolean = psiFunction.run {
        val parentClass = parent as? KtClass
        name == "<init>" && valueParameters.isEmpty()
                && parentClass?.secondaryConstructors?.isEmpty() == true
    }

    val isDefaultConstructor: Boolean
        get() =
            (psiFunction.name == "<init>" || (psiFunction.parent as? KtClass)?.name == psiFunction.name)
                    && (psiFunction.bodyExpression.isNullExpressionOrEmptyBlock())

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
    private fun generateReturnDescriptor(psiFunction: KtNamedFunction): String {
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
