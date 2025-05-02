package org.jetbrains.research.testspark.core.generation.llm

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jetbrains.research.testspark.core.data.Report
import org.jetbrains.research.testspark.core.data.TestCase
import org.jetbrains.research.testspark.core.error.LlmError
import org.jetbrains.research.testspark.core.error.Result
import org.jetbrains.research.testspark.core.error.TestSparkError
import org.jetbrains.research.testspark.core.generation.llm.prompt.PromptSizeReductionStrategy
import org.jetbrains.research.testspark.core.test.ExecutionResult
import org.jetbrains.research.testspark.core.test.SupportedLanguage
import org.jetbrains.research.testspark.core.test.TestCompiler
import org.jetbrains.research.testspark.core.test.TestsAssembler
import org.jetbrains.research.testspark.core.test.TestsPersistentStorage
import org.jetbrains.research.testspark.core.test.TestsPresenter
import org.jetbrains.research.testspark.core.test.data.TestCaseGeneratedByLLM
import org.jetbrains.research.testspark.core.test.data.TestSuiteGeneratedByLLM
import java.io.File

/**
 * LLMWithFeedbackCycle class represents a feedback cycle for an LLM.
 *
 * 1. Make a request
 *
 * @property report The `Report` instance used for storing generated tests.
 * @property language The `SupportedLanguage` enum value representing the programming language used.
 * @property initialPromptMessage The initial prompt message to start the feedback cycle.
 * @property promptSizeReductionStrategy The `PromptSizeReductionStrategy` instance used for reducing the prompt size.
 * @property testSuiteFilename The name of the file in which the test suite is saved in the result path.
 * @property resultPath The temporary path where all the generated tests and their Jacoco report are saved.
 * @property buildPath All the directories where the compiled code of the project under test is saved.
 * @property chatSessionManager A ChatSession manager which holds chat history and manages requests to LLM.
 * @property testsAssembler The `TestsAssembler` instance used for assembling generated tests.
 * @property testCompiler The `TestCompiler` instance used for compiling tests.
 * @property testStorage The `TestsPersistentStorage` instance used for storing generated tests.
 * @property testsPresenter The `TestsPresenter` instance used for presenting generated tests.
 * @property indicator The `CustomProgressIndicator` instance used for tracking progress.
 * @property requestsCountThreshold The threshold for the maximum number of requests in the feedback cycle.
 */
class LLMWithFeedbackCycle(
    private val report: Report,
    private val language: SupportedLanguage,
    private val initialPromptMessage: String,
    private val promptSizeReductionStrategy: PromptSizeReductionStrategy,
    // filename in which the test suite is saved in the result path
    private val testSuiteFilename: String,
    // temp path where all the generated tests and their jacoco report are saved
    private val resultPath: String,
    // all the directories where the compiled code of the project under test is saved. This path will be used as a classpath to run each test case
    private val buildPath: String,
    private val chatSessionManager: ChatSessionManager,
    private val testsAssembler: TestsAssembler,
    private val testCompiler: TestCompiler,
    private val testStorage: TestsPersistentStorage,
    private val testsPresenter: TestsPresenter,
    private val requestsCountThreshold: Int,
) {
    private val log = KotlinLogging.logger { this::class.java }
    private lateinit var generatedTestSuite: TestSuiteGeneratedByLLM

    fun run(): Flow<Result<TestSuiteGeneratedByLLM>> = flow {
        var iteration = 0
        var nextPromptMessage = initialPromptMessage
        val generatedTestSuites: MutableList<TestSuiteGeneratedByLLM> = mutableListOf()

        while (iteration < requestsCountThreshold) {
            iteration++
            log.info { "Iteration #$iteration of feedback cycle" }

            val chunks: Flow<Result<String>> = chatSessionManager.request(
                prompt = nextPromptMessage,
                isUserFeedback = false,
            )
            val testSuiteResult: Result<TestSuiteGeneratedByLLM> = chunks.collectChunks(testsAssembler)

            when (testSuiteResult) {
                is Result.Success -> log.info { "Test suite generated successfully: ${testSuiteResult.data}" }

                is Result.Failure -> {
                    log.info { "Cannot parse a test suite from the LLM response. LLM response: '$testSuiteResult'" }
                    emit(testSuiteResult)
                    nextPromptMessage = generatePromptMessage(testSuiteResult.error) ?: break

                    /**
                     * The current attempt does not count as a failure since it was rejected due to the prompt size
                     * exceeding the threshold
                     */
                    if (testSuiteResult.error is LlmError.PromptTooLong) iteration--
                    continue
                }
            }

            val testSuite = testSuiteResult.data
            generatedTestSuites.add(testSuite)
            compileTestCases(testSuite)

            if (testSuite.testCases.any { it.isCompilable.not() }) {
                log.info { "Non-compilable test suite: \n${testsPresenter.representTestSuite(generatedTestSuite)}" }
                emit(Result.Failure(LlmError.CompilationError))
                nextPromptMessage = generateCompilationErrorPrompt(testSuite)
                continue
            }

            break
        }

        log.info { "Result is compilable" }
        val resultingTestSuite = joinTestSuites(generatedTestSuites)
        if (resultingTestSuite != null) {
            emit(Result.Success(resultingTestSuite))
            recordReport(report, resultingTestSuite.testCases)
        }
    }

    private fun compileTestCases(testSuite: TestSuiteGeneratedByLLM) {
        testSuite.testCases.forEachIndexed { index, testCase ->
            val testCaseName = getClassWithTestCaseName(testCase.name)
            val testCaseFilename = "$testCaseName.${language.extension}"
            val testCaseRepresentation = testsPresenter.representTestCase(testSuite, index)
            val saveFilepath = testStorage.saveGeneratedTest(
                packageString = testSuite.packageName,
                code = testCaseRepresentation,
                resultPath = resultPath,
                testFileName = testCaseFilename,
            )
            testCase.isCompilable = compileTest(saveFilepath).isSuccessful()
        }
    }

    private fun compileTest(filePath: String): ExecutionResult {
        // TODO replace with custom exception
        require(File(filePath).exists()) { "Failed to save test file $filePath" }

        return testCompiler.compileCode(
            path = File(filePath).absolutePath,
            projectBuildPath = buildPath,
            workingDir = resultPath
        )
    }

    private fun generatePromptMessage(error: TestSparkError) = when (error) {
        is LlmError.EmptyLlmResponse -> {
            "You have provided an empty answer! Please, answer my previous question with the same formats"
        }

        is LlmError.PromptTooLong -> {
            if (promptSizeReductionStrategy.isReductionPossible()) {
                promptSizeReductionStrategy.reduceSizeAndGeneratePrompt()
            } else null
        }

        is LlmError.TestSuiteParsingError -> {
            "The provided code is not parsable. Please, generate the correct code"
        }

        else -> null
    }

    private fun generateCompilationErrorPrompt(testSuite: TestSuiteGeneratedByLLM): String {
        val generatedTestSuitePath: String =
            testStorage.saveGeneratedTest(
                testSuite.packageName,
                testsPresenter.representTestSuite(generatedTestSuite),
                resultPath,
                testSuiteFilename,
            )
        val testSuiteCompilationResult = compileTest(generatedTestSuitePath)
        val prompt =
            """
            I cannot compile the tests that you provided. The error is:
            ```
            ${testSuiteCompilationResult.executionMessage}
            ```
            Fix this issue in the provided tests.\nGenerate public classes and public methods. Response only a code with tests between ```, do not provide any other text.
            """.trimIndent()

        return prompt
    }

    /**
     * Records the generated test cases in the given report.
     *
     * @param report The report object to store the test cases in.
     * @param testCases The list of test cases generated by LLM.
     */
    private fun recordReport(
        report: Report,
        testCases: List<TestCaseGeneratedByLLM>,
    ) {
        testCases.forEachIndexed { index, test ->
            report.testCaseList[index] = TestCase(index, test.name, test.toString(), setOf())
        }
    }

    private companion object {
        fun joinTestSuites(testSuites: List<TestSuiteGeneratedByLLM>): TestSuiteGeneratedByLLM? {
            if (testSuites.isEmpty()) return null
            return TestSuiteGeneratedByLLM(
                testCases = testSuites.map { it.testCases }.flatten().toMutableList(),
                imports = testSuites.map { it.imports }.flatten().toMutableSet(),
                otherInfo = testSuites.joinToString(separator = "\n") { it.otherInfo },
                packageName = testSuites.last().packageName,
                annotation = testSuites.last().annotation,
            )
        }
    }
}
