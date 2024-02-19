package org.jetbrains.research.testspark.helpers

import com.intellij.psi.impl.PsiJavaParserFacadeImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class MethodDescriptorHelperTest {

    @ParameterizedTest
    @MethodSource("primitiveFieldTypeTestGenerator")
    fun primitiveFieldTypeTest(text: String, expected: String) {
        val psiType = PsiJavaParserFacadeImpl.getPrimitiveType(text)
        assertThat(generateFieldType(psiType)).isEqualTo(expected)
    }

    companion object {
        @JvmStatic
        private fun primitiveFieldTypeTestGenerator(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("int", "I"),
                Arguments.of("long", "J"),
                Arguments.of("float", "F"),
                Arguments.of("double", "D"),
                Arguments.of("boolean", "Z"),
                Arguments.of("byte", "B"),
                Arguments.of("char", "C"),
                Arguments.of("short", "S"),
            )
        }
    }
}
