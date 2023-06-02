package org.jetbrains.research.testgenie.tools.llm.generation

import org.jetbrains.research.testgenie.tools.llm.test.TestCaseGeneratedByLLM
import org.jetbrains.research.testgenie.tools.llm.test.TestSuiteGeneratedByLLM

class TestsAssembler(
    val testSuite: TestSuiteGeneratedByLLM = TestSuiteGeneratedByLLM(),
    var activeTestCase: TestCaseGeneratedByLLM = TestCaseGeneratedByLLM()
) {

    fun receiveResponse(word: String) {
        print(word)
        // ToDo(Needs to be implemented)
    }
}
