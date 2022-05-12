package nl.tudelft.ewi.se.ciselab.testgenie.helpers

import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiSubstitutor
import com.intellij.psi.PsiType
import com.intellij.util.containers.stream
import java.util.stream.Collectors

fun generateMethodDescriptor(psiMethod: PsiMethod): String {
    val parameterTypes = psiMethod.getSignature(PsiSubstitutor.EMPTY)
        .parameterTypes
        .stream()
        .map { i -> generateFieldType(i) }
        .collect(Collectors.joining())

    val returnType = generateReturnDescriptor(psiMethod)

    return "${psiMethod.name}($parameterTypes)$returnType"
}

fun generateReturnDescriptor(psiMethod: PsiMethod): String {
    if (psiMethod.returnType == null) {
        // void method
        return "V"
    }

    return generateFieldType(psiMethod.returnType!!)
}

fun generateFieldType(psiType: PsiType): String {

    psiType.canonicalText.let {
        return when (it) {
            "int" -> "I"
            "long" -> "J"
            "float" -> "F"
            "double" -> "D"
            "boolean" -> "Z"
            "byte" -> "B"
            "char" -> "C"
            "short" -> "S"
            else -> "L$it;" // todo: fix
        }
    }
}
