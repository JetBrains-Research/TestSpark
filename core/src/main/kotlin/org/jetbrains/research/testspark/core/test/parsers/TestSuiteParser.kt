package org.jetbrains.research.testspark.core.test.parsers

import org.jetbrains.research.testspark.core.test.TestSuiteGeneratedByLLM

interface TestSuiteParser {
    /**
     * Extracts test cases from raw text and generates a test suite using the given package name.
     *
     * @param rawText The raw text provided by the LLM that contains the generated test cases.
     * @return A GeneratedTestSuite instance containing the extracted test cases.
     */
    fun parseTestSuite(rawText: String): TestSuiteGeneratedByLLM?
}