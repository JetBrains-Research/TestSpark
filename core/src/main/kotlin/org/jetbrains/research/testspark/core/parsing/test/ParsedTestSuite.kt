package org.jetbrains.research.testspark.core.parsing.test

data class ParsedTestSuite(
    val imports: Set<String>,
    val packageString: String,
    val runWith: String,
    val otherInfo: String,
    val testCases: MutableList<TestCaseGeneratedByLLM>
)