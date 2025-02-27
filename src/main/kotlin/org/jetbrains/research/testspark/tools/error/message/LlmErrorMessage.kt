package org.jetbrains.research.testspark.tools.error.message

import org.jetbrains.research.testspark.bundles.llm.LLMMessagesBundle
import org.jetbrains.research.testspark.core.error.HttpError
import org.jetbrains.research.testspark.core.error.LlmError
import java.net.HttpURLConnection

val LlmError.llmErrorDisplayMessage: String?
    get() = when (this) {
        is LlmError.PromptTooLong -> LLMMessagesBundle.get("tooLongPromptRequest")
        is LlmError.GrazieNotAvailable -> LLMMessagesBundle.get("grazieError")
        is LlmError.NoCompilableTestCasesGenerated -> LLMMessagesBundle.get("invalidLLMResult")
        is LlmError.FailedToSaveTestFiles -> LLMMessagesBundle.get("savingTestFileIssue")
        is LlmError.CompilationError -> LLMMessagesBundle.get("compilationError")
        is LlmError.EmptyLlmResponse -> LLMMessagesBundle.get("emptyResponse")
        is LlmError.TestSuiteParsingError -> LLMMessagesBundle.get("emptyResponse")
    }

val HttpError.httpErrorDisplayMessage: String?
    get() = when (httpCode) {
        HttpURLConnection.HTTP_INTERNAL_ERROR -> LLMMessagesBundle.get("serverProblems")
        HttpURLConnection.HTTP_UNAUTHORIZED -> LLMMessagesBundle.get("wrongToken")
        else -> message ?: cause?.message
    }