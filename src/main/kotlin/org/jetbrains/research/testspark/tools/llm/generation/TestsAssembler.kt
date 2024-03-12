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
import org.jetbrains.research.testspark.core.parsing.parsers.java.JUnitTestSuiteParser
import org.jetbrains.research.testspark.core.parsing.parsers.TestSuiteParser
import org.jetbrains.research.testspark.core.parsing.test.ParsedTestSuite
import org.jetbrains.research.testspark.services.SettingsApplicationService
import org.jetbrains.research.testspark.services.TestGenerationDataService
import org.jetbrains.research.testspark.settings.SettingsApplicationState
import org.jetbrains.research.testspark.tools.llm.generation.openai.OpenAIChoice
import org.jetbrains.research.testspark.tools.llm.test.TestSuiteGeneratedByLLM
import org.jetbrains.research.testspark.tools.processStopped

/**
 * Assembler class for generating and organizing test cases.
 *
 * @property project The project to which the tests belong.
 * @property indicator The progress indicator to display the progress of test generation.
 * @property log The logger for logging debug information.
 * @property rawText The raw text containing the generated tests.
 * @property lastTestCount The count of the last generated tests.
 */
class TestsAssembler(
    val project: Project,
    val indicator: ProgressIndicator,
) {
    private val settingsState: SettingsApplicationState
        get() = SettingsApplicationService.getInstance().state!!

    private val log: Logger = Logger.getInstance(this.javaClass)
    var rawText = ""
    private var lastTestCount = 0

    /**
     * Receives a response text and updates the progress bar accordingly.
     *
     * @param text part of the LLM response
     */
    fun receiveResponse(text: String) {
        if (text.isEmpty()) return

        // Collect the response and update the progress bar
        rawText = rawText.plus(text)
        updateProgressBar()
    }

    /**
     * Receives a response text and updates the progress bar accordingly.
     *
     * @param httpRequest the httpRequest sent to OpenAI
     */
    fun receiveResponse(httpRequest: HttpRequests.Request) {
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
            rawText = rawText.plus(choices.delta.content)
            updateProgressBar()
        }

        log.debug(rawText)
    }

    private fun updateProgressBar() {
        val generatedTestsCount = rawText.split("@Test").size - 1

        if (lastTestCount != generatedTestsCount) {
            indicator.text = TestSparkBundle.message("generatingTestNumber") + generatedTestsCount
            lastTestCount = generatedTestsCount
        }
    }

    /**
     * Extracts test cases from raw text and generates a TestSuite using the given package name.
     *
     * @param packageName The package name to be set in the generated TestSuite.
     * @return A TestSuiteGeneratedByLLM object containing the extracted test cases and package name.
     */
    fun returnTestSuite(packageName: String): TestSuiteGeneratedByLLM? {
        val junitVersion = settingsState.junitVersion

        val testSuiteParser = createTestSuiteParser(packageName, junitVersion)
        val testSuite: ParsedTestSuite? = testSuiteParser.parseTestSuite(rawText)

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

        if (testSuite != null) {
            // logging generated test cases
            testSuite.testCases.forEach { testCase -> log.info("Generated test case: $testCase") }

            return TestSuiteGeneratedByLLM(
                project = project,
                imports = testSuite.imports,
                packageString = testSuite.packageString,
                runWith = testSuite.runWith,
                otherInfo = testSuite.otherInfo,
                testCases = testSuite.testCases
            )
        }
        else {
            return null
        }

        /*
        if (rawText.isBlank()) return null
        try {
            val testSuite = TestSuiteGeneratedByLLM(project)
            if (rawText.contains("```")) {
                rawText = rawText.split("```")[1]
            }

            testSuite.packageString = packageName

            // save imports
            testSuite.imports =
                importPattern.findAll(rawText, 0)
                    .map { it.groupValues[0] }
                    .toSet()

            val junitVersion = settingsState.junitVersion

            // save RunWith
            val runWith = junitVersion.runWithAnnotationMeta.extract(rawText)
            if (runWith != null) {
                testSuite.runWith = runWith
                project.service<TestGenerationDataService>().runWith = runWith
                project.service<TestGenerationDataService>().importsCode.add(junitVersion.runWithAnnotationMeta.import)
            } else {
                project.service<TestGenerationDataService>().runWith = ""
                project.service<TestGenerationDataService>().importsCode.remove(junitVersion.runWithAnnotationMeta.import)
            }

            val testSet: MutableList<String> = rawText.split("@Test").toMutableList()

            // save annotations and pre-set methods
            val otherInfoList = testSet.removeAt(0).split("public class")[1].split("{").toMutableList()
            otherInfoList.removeFirst()
            val otherInfo = otherInfoList.joinToString("{").trimEnd() + "\n\n"
            if (otherInfo.isNotBlank()) {
                testSuite.otherInfo = otherInfo
            }
            project.service<TestGenerationDataService>().otherInfo = otherInfo

            // Save the main test cases
            testSet.forEach ca@{
                val rawTest = "@Test$it"
                val currentTest = TestCaseGeneratedByLLM()

                // Get expected Exception
                if (rawTest.startsWith("@Test(expected =")) {
                    currentTest.expectedException =
                        rawTest
                            .split(")")[0]
                            .trim()
                }

                // Get unexpected exceptions
                if (!rawTest.contains("public void")) {
                    println("WARNING: The raw Test does not contain public void:\n $rawTest")
                    return@ca
                }
                val interestingPartOfSignature =
                    rawTest
                        .split("public void")[1]
                        .split("{")[0]
                        .split("()")[1]
                        .trim()
                if (interestingPartOfSignature.contains("throws")) {
                    currentTest.throwsException =
                        interestingPartOfSignature
                            .split("throws")[1]
                            .trim()
                }

                // Get test's name
                currentTest.name =
                    rawTest
                        .split("public void ")[1]
                        .split("()")[0]
                        .trim()

                // Get test's body
                // remove opening bracket
                var testBody =
                    rawTest.split("{").toMutableList().apply {
                        removeFirst()
                    }.joinToString("{").trim()

                // remove closing bracket

                val tempList = testBody.split("}").toMutableList()
                tempList.removeLast()

                if (testSuite.testCases.size == testSet.size - 1) {
                    // it is the last test. So we should remove another closing bracket
                    if (tempList.isNotEmpty()) {
                        tempList.removeLast()
                    } else {
                        println("WARNING: the final test does not have to brackets:\n $testBody")
                    }
                }

                testBody = tempList.joinToString("}")

                // Save each line
                val lines = testBody.split("\n").toMutableList()
                lines.forEach { rawLine ->
                    val line = rawLine.trim()

                    val type: TestLineType =
                        when {
                            line.startsWith("//") -> TestLineType.COMMENT
                            line.isBlank() -> TestLineType.BREAK
                            line.lowercase().startsWith("assert") -> TestLineType.ASSERTION
                            else -> TestLineType.CODE
                        }

                    currentTest.lines.add(TestLine(type, line))
                }

                testSuite.testCases.add(currentTest)

                log.info("New test case: $currentTest")
            }
            return testSuite
        } catch (e: Exception) {
            return null
        }
        */
    }

    private fun createTestSuiteParser(packageName: String, jUnitVersion: JUnitVersion): TestSuiteParser {
        return JUnitTestSuiteParser(packageName, jUnitVersion)
    }
}
