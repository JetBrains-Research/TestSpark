package org.jetbrains.research.testspark.tools.llm.generation

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.util.io.HttpRequests
import org.jetbrains.research.testspark.bundles.TestSparkBundle
import org.jetbrains.research.testspark.core.data.JUnitVersion
import org.jetbrains.research.testspark.core.test.TestsAssembler
import org.jetbrains.research.testspark.core.test.parsers.java.JUnitTestSuiteParser
import org.jetbrains.research.testspark.core.test.parsers.TestSuiteParser
import org.jetbrains.research.testspark.services.SettingsApplicationService
import org.jetbrains.research.testspark.services.TestGenerationDataService
import org.jetbrains.research.testspark.settings.SettingsApplicationState
import org.jetbrains.research.testspark.tools.llm.generation.openai.OpenAIChoice
import org.jetbrains.research.testspark.core.test.TestSuiteGeneratedByLLM
import org.jetbrains.research.testspark.tools.processStopped

/**
 * Assembler class for generating and organizing test cases.
 *
 * @property project The project to which the tests belong.
 * @property indicator The progress indicator to display the progress of test generation.
 * @property log The logger for logging debug information.
 * @property lastTestCount The count of the last generated tests.
 */
class JUnitTestsAssembler(
    val project: Project,
    val indicator: ProgressIndicator,
) : TestsAssembler() {
    private val settingsState: SettingsApplicationState
        get() = SettingsApplicationService.getInstance().state!!

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

    /**
     * Receives a response text and updates the progress bar accordingly.
     *
     * @param httpRequest the httpRequest sent to OpenAI
     */
    fun consume(httpRequest: HttpRequests.Request) {
        while (true) {
            if (processStopped(project, indicator)) return

            Thread.sleep(50L)
            var text = httpRequest.reader.readLine()

            if (text.isEmpty()) continue

            text = text.removePrefix("data: ")

            val choices =
                Gson().fromJson(
                    JsonParser.parseString(text)
                        .asJsonObject["choices"]
                        .asJsonArray[0].asJsonObject,
                    OpenAIChoice::class.java,
                )

            if (choices.finishedReason == "stop") break

            // Collect the response and update the progress bar
            super.consume(choices.delta.content)
            updateProgressBar()
        }

        log.debug(super.getContent())
    }

    private fun updateProgressBar() {
        val generatedTestsCount = super.getContent().split("@Test").size - 1

        if (lastTestCount != generatedTestsCount) {
            indicator.text = TestSparkBundle.message("generatingTestNumber") + generatedTestsCount
            lastTestCount = generatedTestsCount
        }
    }

    override fun assembleTestSuite(packageName: String): TestSuiteGeneratedByLLM? {
        val junitVersion = settingsState.junitVersion

        val parser = createTestSuiteParser(packageName, junitVersion)
        val testSuite: TestSuiteGeneratedByLLM? = parser.parseTestSuite(super.getContent())

        // save RunWith
        if (testSuite?.runWith?.isNotBlank() == true) {
            project.service<TestGenerationDataService>().runWith = testSuite.runWith
            project.service<TestGenerationDataService>().importsCode.add(junitVersion.runWithAnnotationMeta.import)
        }
        else {
            project.service<TestGenerationDataService>().runWith = ""
            project.service<TestGenerationDataService>().importsCode.remove(junitVersion.runWithAnnotationMeta.import)
        }

        // save annotations and pre-set methods
        project.service<TestGenerationDataService>().otherInfo = testSuite?.otherInfo ?: ""

        // logging generated test cases if any
        testSuite?.testCases?.forEach { testCase -> log.info("Generated test case: $testCase") }
        return testSuite
    }

    private fun createTestSuiteParser(packageName: String, jUnitVersion: JUnitVersion): TestSuiteParser {
        return JUnitTestSuiteParser(packageName, jUnitVersion)
    }
}
