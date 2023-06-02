package org.jetbrains.research.testgenie.tools.llm.test

data class TestSuiteGeneratedByLLM(
    private var imports: Set<String> = emptySet(),
    private var testCases: Set<TestCaseGeneratedByLLM> = emptySet()
) {


    fun isEmpty(): Boolean {
        return testCases.isEmpty()
    }
}