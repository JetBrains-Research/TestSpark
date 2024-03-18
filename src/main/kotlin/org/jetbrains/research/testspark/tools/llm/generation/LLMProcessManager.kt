package org.jetbrains.research.testspark.tools.llm.generation

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.bundles.TestSparkBundle
import org.jetbrains.research.testspark.bundles.TestSparkToolTipsBundle
import org.jetbrains.research.testspark.core.generation.llm.network.LLMResponse
import org.jetbrains.research.testspark.core.generation.llm.network.ResponseErrorCode
import org.jetbrains.research.testspark.core.progress.CustomProgressIndicator
import org.jetbrains.research.testspark.core.test.data.TestCaseGeneratedByLLM
import org.jetbrains.research.testspark.core.test.data.TestSuiteGeneratedByLLM
import org.jetbrains.research.testspark.data.FragmentToTestData
import org.jetbrains.research.testspark.data.ProjectContext
import org.jetbrains.research.testspark.data.Report
import org.jetbrains.research.testspark.data.TestCase
import org.jetbrains.research.testspark.services.ErrorService
import org.jetbrains.research.testspark.services.SettingsProjectService
import org.jetbrains.research.testspark.services.TestGenerationData
import org.jetbrains.research.testspark.tools.generatedTests.TestUtils
import org.jetbrains.research.testspark.tools.getBuildPath
import org.jetbrains.research.testspark.tools.getImportsCodeFromTestSuiteCode
import org.jetbrains.research.testspark.tools.getPackageFromTestSuiteCode
import org.jetbrains.research.testspark.tools.llm.SettingsArguments
import org.jetbrains.research.testspark.tools.llm.error.LLMErrorManager
import org.jetbrains.research.testspark.tools.llm.getClassWithTestCaseName
import org.jetbrains.research.testspark.tools.llm.test.TestSuitePresenter
import org.jetbrains.research.testspark.tools.processStopped
import org.jetbrains.research.testspark.tools.saveData
import org.jetbrains.research.testspark.tools.template.generation.ProcessManager
import java.io.File

/**
 * LLMProcessManager is a class that implements the ProcessManager interface
 * and is responsible for generating tests using the LLM tool.
 *
 * @property project The project in which the test generation is being performed.
 * @property prompt The prompt to be sent to the LLM tool.
 * @property testFileName The name of the generated test file.
 * @property log An instance of the logger class for logging purposes.
 * @property llmErrorManager An instance of the LLMErrorManager class.
 * @property maxRequests The maximum number of requests to be sent to LLM.
 */
class LLMProcessManager(
    private val project: Project,
    private val promptManager: PromptManager,
    private val testSamplesCode: String,
) : ProcessManager {
    private val testFileName: String = "GeneratedTest.java"
    private val log = Logger.getInstance(this::class.java)
    private val llmErrorManager: LLMErrorManager = LLMErrorManager()
    private val maxRequests = SettingsArguments.maxLLMRequest()
    private val testUtils = TestUtils(project)

    /**
     * Runs the test generator process.
     *
     * @param indicator The progress indicator for tracking the progress of the test generation process.
     * @param codeType The type of code to generate tests for.
     * @param packageName The package name of the code being tested.
     */
    override fun runTestGenerator(
        indicator: CustomProgressIndicator,
        codeType: FragmentToTestData,
        packageName: String,
        projectContext: ProjectContext,
        generatedTestsData: TestGenerationData
    ) {
        log.info("LLM test generation begins")

        if (processStopped(project, indicator)) return

        // update build path
        var buildPath = projectContext.projectClassPath!!
        if (project.service<SettingsProjectService>().state.buildPath.isEmpty()) {
            // User did not set own path
            buildPath = getBuildPath(project)
        }

        if (buildPath.isEmpty() || buildPath.isBlank()) {
            llmErrorManager.errorProcess(TestSparkBundle.message("emptyBuildPath"), project)
            return
        }
        indicator.setText(TestSparkBundle.message("searchMessage"))

        log.info("Generated tests suite received")

        var generatedTestsArePassing = false

        val report = Report()

        var requestsCount = 0
        var warningMessage = ""
        var messageToPrompt = promptManager.generatePrompt(codeType, testSamplesCode)
        var generatedTestSuite: TestSuiteGeneratedByLLM? = null

        // Initiate a new RequestManager
        val requestManager = StandardRequestManagerFactory().getRequestManager(project)
        // Asking LLM to generate test. Here, we have a loop to make feedback cycle for LLm in case of wrong responses.
        while (!generatedTestsArePassing) {
            requestsCount++

            log.info("New iterations of requests")

            // Process stopped checking
            if (processStopped(project, indicator)) {
                return
            }

            // Ending loop checking
            if (isLastIteration(requestsCount) && generatedTestsData.compilableTestCases.isEmpty()) {
                llmErrorManager.errorProcess(TestSparkBundle.message("invalidLLMResult"), project)
                break
            }

            // Send request to LLM
            if (warningMessage.isNotEmpty()) {
                llmErrorManager.warningProcess(warningMessage, project)
            }

            val response: LLMResponse =
                requestManager.request(messageToPrompt, indicator, packageName)
            when(response.errorCode) {
                ResponseErrorCode.OK -> {
                    log.info("Test suite generated successfully: ${response.testSuite!!}")
                }
                ResponseErrorCode.PROMPT_TOO_LONG -> {
                    if (promptManager.reducePromptSize()) {
                        messageToPrompt = promptManager.generatePrompt(codeType, testSamplesCode)
                        requestsCount--
                        continue
                    } else {
                        llmErrorManager.errorProcess(TestSparkBundle.message("tooLongPromptRequest"), project)
                        return
                    }
                }
                ResponseErrorCode.EMPTY_LLM_RESPONSE -> {
                    messageToPrompt = "You have provided an empty answer! Please answer my previous question with the same formats"
                    continue
                }
                ResponseErrorCode.TEST_SUITE_PARSING_FAILURE -> {
                    llmErrorManager.warningProcess(TestSparkBundle.message("emptyResponse") + "LLM response: $response", project)
                    messageToPrompt = "The provided code is not parsable. Please, generate the correct code"
                    continue
                }
            }

            generatedTestSuite = response.testSuite!!

            // Empty response checking
            if (generatedTestSuite.testCases.isEmpty()) {
                warningMessage = TestSparkBundle.message("emptyResponse")
                messageToPrompt =
                    "You have provided an empty answer! Please answer my previous question with the same formats."
                continue
            }

            // Process stopped checking
            if (processStopped(project, indicator)) {
                return
            }

            // Save the generated TestSuite into a temp file
            val generatedTestCasesPaths: MutableList<String> = mutableListOf()
            val testSuitePresenter = TestSuitePresenter(project, generatedTestsData)

            if (isLastIteration(requestsCount)) {
                generatedTestSuite.updateTestCases(generatedTestsData.compilableTestCases.toMutableList())
            }
            else {
                for (testCaseIndex in generatedTestSuite.testCases.indices) {
                    val testFileName = "${getClassWithTestCaseName(generatedTestSuite.testCases[testCaseIndex].name)}.java"

                    val testCaseRepresentation = testSuitePresenter
                        .toStringSingleTestCaseWithoutExpectedException(generatedTestSuite, testCaseIndex)

                    val saveFilepath = testUtils.saveGeneratedTest(
                        generatedTestSuite.packageString,
                        testCaseRepresentation,
                        generatedTestsData.resultPath,
                        testFileName
                    )

                    generatedTestCasesPaths.add(saveFilepath)
                }
            }

            val generatedTestPath: String = testUtils.saveGeneratedTest(
                generatedTestSuite.packageString,
                testSuitePresenter.toStringWithoutExpectedException(generatedTestSuite),
                generatedTestsData.resultPath,
                testFileName,
            )

            // Correct files creating checking
            var isFilesExists = true
            for (path in generatedTestCasesPaths) isFilesExists = isFilesExists && File(path).exists()
            if (!isFilesExists || !File(generatedTestPath).exists()) {
                llmErrorManager.errorProcess(TestSparkBundle.message("savingTestFileIssue"), project)
                break
            }

            // Get test cases
            val testCases: MutableList<TestCaseGeneratedByLLM> =
                if (!isLastIteration(requestsCount)) {
                    generatedTestSuite.testCases
                } else {
                    generatedTestsData.compilableTestCases.toMutableList()
                }

            // Compile the test file
            indicator.setText(TestSparkBundle.message("compilationTestsChecking"))

            val separateCompilationResult = testUtils.compileTestCases(generatedTestCasesPaths, buildPath, testCases, generatedTestsData)
            val commonCompilationResult = testUtils.compileCode(File(generatedTestPath).absolutePath, buildPath)

            if (!separateCompilationResult && !isLastIteration(requestsCount)) {
                log.info("Incorrect result: \n${testSuitePresenter.toString(generatedTestSuite)}")
                warningMessage = TestSparkBundle.message("compilationError")
                messageToPrompt = "I cannot compile the tests that you provided. The error is:\n${commonCompilationResult.second}\n Fix this issue in the provided tests." + TestSparkToolTipsBundle.defaultValue("commonPromptPart")
                continue
            }

            log.info("Result is compilable")

            generatedTestsArePassing = true

            for (index in testCases.indices) {
                report.testCaseList[index] = TestCase(index, testCases[index].name, testCases[index].toString(), setOf(), setOf(), setOf())
            }
        }

        if (processStopped(project, indicator)) return

        // Error during the collecting
        if (project.service<ErrorService>().isErrorOccurred()) return

        log.info("Result is ready")

        val testSuitePresenter = TestSuitePresenter(project,generatedTestsData)
        val testSuiteRepresentation =
            if (generatedTestSuite != null) testSuitePresenter.toString(generatedTestSuite) else null

        saveData(
            project,
            report,
            getPackageFromTestSuiteCode(testSuiteRepresentation/*generatedTestSuite.toString()*/),
            getImportsCodeFromTestSuiteCode(testSuiteRepresentation/*generatedTestSuite.toString()*/, projectContext.classFQN!!,),
            projectContext.fileUrl!!,
            generatedTestsData
            )
    }

    private fun isLastIteration(requestsCount: Int) = requestsCount > maxRequests
}
