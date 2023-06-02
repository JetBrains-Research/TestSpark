package org.jetbrains.research.testgenie.tools.llm.generation

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import org.jetbrains.research.testgenie.tools.llm.test.TestSuiteGeneratedByLLM


//private var rawText: String = ""
private var rawText = ""

val importPattern = Regex(
    pattern = "^import\\s+(static\\s)?((?:[a-zA-Z_]\\w*\\.)*[a-zA-Z_](?:\\w*\\.?)*)(?:\\.\\*)?;",
    options = setOf(RegexOption.MULTILINE)
)

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

            // save imports
            testSuite.imports = importPattern.findAll(rawText,0).map {
                it.groupValues[0]
            }.toSet()

            // ToDo(Process rawText and add test cases)

            return testSuite
        }
    }
}
