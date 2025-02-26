package org.jetbrains.research.testspark.core.data

sealed interface TestSparkModule {
    data class LLM(val type: LlmModuleType? = null) : TestSparkModule
    data object EvoSuite : TestSparkModule
    data object Kex : TestSparkModule
    data object UI : TestSparkModule
    data object ProjectBuilder : TestSparkModule
    data object Compiler : TestSparkModule
    data object Common : TestSparkModule
}

enum class LlmModuleType {
    HuggingFace,
    Gemini,
    Grazie,
    OpenAi
}