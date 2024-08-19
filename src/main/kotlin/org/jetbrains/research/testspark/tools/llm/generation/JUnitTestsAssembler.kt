package org.jetbrains.research.testspark.tools.llm.generation

import com.intellij.openapi.diagnostic.Logger
import org.jetbrains.research.testspark.bundles.plugin.PluginMessagesBundle
import org.jetbrains.research.testspark.core.data.JUnitVersion
import org.jetbrains.research.testspark.core.data.TestGenerationData
import org.jetbrains.research.testspark.core.progress.CustomProgressIndicator
import org.jetbrains.research.testspark.core.test.TestSuiteParser
import org.jetbrains.research.testspark.core.test.TestsAssembler
import org.jetbrains.research.testspark.core.test.data.TestSuiteGeneratedByLLM

/**
 * Assembler class for generating and organizing test cases.
 *
 * @property indicator The progress indicator to display the progress of test generation.
 * @property log The logger for logging debug information.
 * @property lastTestCount The count of the last generated tests.
 */
class JUnitTestsAssembler(
    val indicator: CustomProgressIndicator,
    private val generationData: TestGenerationData,
    private val testSuiteParser: TestSuiteParser,
    val junitVersion: JUnitVersion,
) : TestsAssembler() {

    private val log: Logger = Logger.getInstance(this.javaClass)

    private var lastTestCount = 0

    /**
     * Receives a response text and updates the progress bar accordingly.
     *
     * @param text part of the LLM response
     */
    override fun consume(text: String) {
        if (text.isEmpty()) return

        // Collect the response and update the progress bar
        super.consume(text)
        updateProgressBar()
    }

    private fun updateProgressBar() {
        val generatedTestsCount = super.getContent().split("@Test").size - 1

        if (lastTestCount != generatedTestsCount) {
            indicator.setText(PluginMessagesBundle.get("generatingTestNumber") + generatedTestsCount)
            lastTestCount = generatedTestsCount
        }
    }

    override fun assembleTestSuite(): TestSuiteGeneratedByLLM? {
        val testSuite = testSuiteParser.parseTestSuite(super.getContent())

        // save RunWith
        if (testSuite?.runWith?.isNotBlank() == true) {
            generationData.runWith = testSuite.runWith
            generationData.importsCode.add(junitVersion.runWithAnnotationMeta.import)
        } else {
            generationData.runWith = ""
            generationData.importsCode.remove(junitVersion.runWithAnnotationMeta.import)
        }

        // save annotations and pre-set methods
        generationData.otherInfo = testSuite?.otherInfo ?: ""

        // logging generated test cases if any
        testSuite?.testCases?.forEach { testCase -> log.info("Generated test case: $testCase") }
        return testSuite
    }
}
