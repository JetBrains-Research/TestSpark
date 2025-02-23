package org.jetbrains.research.testspark.core.error

import org.jetbrains.research.testspark.core.data.LlmModuleType
import org.jetbrains.research.testspark.core.data.TestSparkModule

sealed class LlmError(
    cause: Throwable? = null,
    module: LlmModuleType? = null
) : TestSparkError(module = TestSparkModule.LLM(module), cause = cause) {
    data object PromptTooLong : LlmError()
    data object GrazieNotAvailable : LlmError(module = LlmModuleType.Grazie)
    data object EmptyLlmResponse : LlmError()
    data object TestSuiteParsingError : LlmError()
    data object FeedbackCycleCancelled : LlmError()
    data object NoCompilableTestCasesGenerated : LlmError()
    data object FailedToSaveTestFiles : LlmError()
}