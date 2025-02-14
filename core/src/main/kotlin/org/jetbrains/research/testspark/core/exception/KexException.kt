package org.jetbrains.research.testspark.core.exception

sealed class KexException() : TestSparkException() {
    class MissingGeneratedTests() : KexException()
    class IncorrectJavaVersion() : KexException()
    class Common(override val message: String?) : KexException()
}