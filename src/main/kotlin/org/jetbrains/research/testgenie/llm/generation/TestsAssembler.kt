package org.jetbrains.research.testgenie.llm.generation

import org.jetbrains.research.testgenie.llm.test.TestCaseGeneratedByLLM
import org.jetbrains.research.testgenie.llm.test.TestSuiteGeneratedByLLM

class TestsAssembler(
    val testSuite: TestSuiteGeneratedByLLM = TestSuiteGeneratedByLLM(),
    var activeTestCase: TestCaseGeneratedByLLM = TestCaseGeneratedByLLM()
) {

    fun receiveResponse(word: String){
        //ToDo(Needs to be implemented)
    }
}