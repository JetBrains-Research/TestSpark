package org.jetbrains.research.testspark.core.parsing.parsers

import org.jetbrains.research.testspark.core.data.JUnitVersion
import org.jetbrains.research.testspark.core.parsing.test.ParsedTestSuite

interface TestSuiteParser {
    /**
     * Extracts test cases from raw text and generates a test suite using the given package name.
     *
     * @param packageName The package name to be set in the generated TestSuite.
     * @param rawText The raw text provided by the LLM that contains the generated test cases.
     * @return A GeneratedTestSuite instance containing the extracted test cases with the provided package name assigned.
     */
    fun parseTestSuite(
        packageName: String,
        rawText: String,
        junitVersion: JUnitVersion,
    ): ParsedTestSuite?
}