package org.jetbrains.research.testspark.core.test

import org.jetbrains.research.testspark.core.test.data.TestSuiteGeneratedByLLM

interface TestSuiteParser {
    /**
     * Extracts test cases from raw text and generates a test suite.
     *
     * @param rawText The raw text provided by the LLM that contains the generated test cases.
     * @return A GeneratedTestSuite instance containing the extracted test cases.
     */
    fun parseTestSuite(rawText: String): TestSuiteGeneratedByLLM?
}
