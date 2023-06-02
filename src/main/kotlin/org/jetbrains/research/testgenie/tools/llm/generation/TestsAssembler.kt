package org.jetbrains.research.testgenie.tools.llm.generation

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import org.jetbrains.research.testgenie.tools.llm.test.TestSuiteGeneratedByLLM


//private var rawText: String = ""
private var rawText = ""

val numberOfTestsPattern = Regex(
    pattern = "^Number of test cases are: (\\d+)\$",
    options = setOf(RegexOption.IGNORE_CASE)
)

//var activeTestCase: TestCaseGeneratedByLLM = TestCaseGeneratedByLLM(),
//var importsPassed: Boolean = false,
//var inATest: Boolean = false

var lastTestCount = 0


class TestsAssembler(
    val indicator: ProgressIndicator,
) {
    private val log: Logger = Logger.getInstance(this.javaClass)
    fun receiveResponse(text: String) {
        // Collect the response and update the progress bar
        rawText = rawText.plus(text)
        val generatedTestsCount = rawText.split("@Test").size - 1

        if(lastTestCount != generatedTestsCount){
            indicator.text = "Generating test #$generatedTestsCount"
            lastTestCount = generatedTestsCount
        }

        log.debug(rawText)
    }

    companion object {
        fun returnTestSuite(): TestSuiteGeneratedByLLM {
            val testSuite = TestSuiteGeneratedByLLM()
            // ToDo(Process rawText and add all of the information in the testSuite)

            return testSuite
        }
    }
}
