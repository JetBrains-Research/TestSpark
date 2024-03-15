package org.jetbrains.research.testspark.core.generation.network

import org.jetbrains.research.testspark.core.test.TestSuiteGeneratedByLLM



enum class ResponseErrorCode {
    OK,
    PROMPT_TOO_LONG,
    EMPTY_LLM_RESPONSE,
    TEST_SUITE_PARSING_FAILURE,
}

data class LLMResponse(
    val errorCode: ResponseErrorCode,
    val llmResponseMessage: String,
    val testSuite: TestSuiteGeneratedByLLM?,
) {
    init {
        if (errorCode == ResponseErrorCode.OK && testSuite == null) {
            throw IllegalArgumentException("Test suite must be provided when ErrorCode is OK, got null")
        }
        else if (errorCode != ResponseErrorCode.OK && testSuite != null) {
            throw IllegalArgumentException("Test suite must not be provided when ErrorCode is not OK, got $testSuite")
        }
    }
}
