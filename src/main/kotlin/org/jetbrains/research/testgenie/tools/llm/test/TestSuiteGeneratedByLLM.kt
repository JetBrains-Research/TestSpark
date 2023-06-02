package org.jetbrains.research.testgenie.tools.llm.test

data class TestSuiteGeneratedByLLM(
     var imports: Set<String> = emptySet(),
     var testCases: Set<TestCaseGeneratedByLLM> = emptySet()
) {

    fun isEmpty(): Boolean {
        return testCases.isEmpty()
    }
}
