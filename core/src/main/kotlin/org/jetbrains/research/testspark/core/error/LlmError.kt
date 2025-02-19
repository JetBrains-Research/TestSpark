package org.jetbrains.research.testspark.core.error

sealed class LlmError(cause: Throwable? = null) : TestSparkError(cause) {
    class HttpInternalError() : LlmError()
    class PromptTooLong() : LlmError()
    class HttpUnauthorized() : LlmError()
    class HttpError(val httpCode: Int): LlmError()
    class GrazieHttpError(val error: String) : LlmError()
    class HttpStatusError(cause: Throwable? = null): LlmError()
    class EmptyLlmResponse() : LlmError()
    class TestSuiteParsingError() : LlmError()
    class GrazieNotAvailable() : LlmError()
    class FeedbackCycleCancelled() : LlmError()
    class NoCompilableTestCasesGenerated() : LlmError()
    class FailedToSaveTestFiles() : LlmError()
}