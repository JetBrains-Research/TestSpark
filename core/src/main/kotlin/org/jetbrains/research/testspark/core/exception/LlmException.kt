package org.jetbrains.research.testspark.core.exception

sealed class LlmException() : TestSparkException() {
    class MissingGeneratedTests() : LlmException()
    class IncorrectJavaVersion() : LlmException()
    class Common(override val message: String?) : LlmException()
}

class CouldNotRetrieveJavaVersionException() : TestSparkException()