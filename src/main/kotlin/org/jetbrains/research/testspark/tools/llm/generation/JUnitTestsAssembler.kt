package org.jetbrains.research.testspark.tools.llm.generation

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.bundles.plugin.PluginMessagesBundle
import org.jetbrains.research.testspark.core.data.TestGenerationData
import org.jetbrains.research.testspark.core.progress.CustomProgressIndicator
import org.jetbrains.research.testspark.core.test.TestsAssembler
import org.jetbrains.research.testspark.core.test.data.TestSuiteGeneratedByLLM
import org.jetbrains.research.testspark.core.test.strategies.JUnitTestSuiteParserStrategy
import org.jetbrains.research.testspark.core.utils.Language
import org.jetbrains.research.testspark.core.utils.javaImportPattern
import org.jetbrains.research.testspark.core.utils.kotlinImportPattern
import org.jetbrains.research.testspark.services.LLMSettingsService
import org.jetbrains.research.testspark.settings.llm.LLMSettingsState

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
    val indicator: CustomProgressIndicator,
    val generationData: TestGenerationData,
) : TestsAssembler() {
    private val llmSettingsState: LLMSettingsState
        get() = project.getService(LLMSettingsService::class.java).state

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

    override fun assembleTestSuite(packageName: String, language: Language): TestSuiteGeneratedByLLM? {
        val junitVersion = llmSettingsState.junitVersion

        val testSuite: TestSuiteGeneratedByLLM? = when (language) {
            Language.Java -> JUnitTestSuiteParserStrategy.parseTestSuite(
                super.getContent(),
                junitVersion,
                javaImportPattern,
                packageName,
                testNamePattern = "void",
                Language.Java,
            )

            Language.Kotlin -> JUnitTestSuiteParserStrategy.parseTestSuite(
                super.getContent(),
                junitVersion,
                kotlinImportPattern,
                packageName,
                testNamePattern = "fun",
                Language.Kotlin,
            )
        }

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

//    private fun createTestSuiteParser(
//        packageName: String,
//        jUnitVersion: JUnitVersion,
//        language: Language,
//    ): TestSuiteParser {
//        return when (language) {
//            Language.Java -> JavaJUnitTestSuiteParser(packageName, jUnitVersion, javaImportPattern)
//            Language.Kotlin -> KotlinJUnitTestSuiteParser(packageName, jUnitVersion, kotlinImportPattern)
//        }
//    }
}
