package org.jetbrains.research.testspark.core.generation.llm.network

import org.jetbrains.research.testspark.core.test.data.TestSuiteGeneratedByLLM

enum class ResponseErrorCode {
    OK,
    PROMPT_TOO_LONG,
    EMPTY_LLM_RESPONSE,
    TEST_SUITE_PARSING_FAILURE,
}
