package org.jetbrains.research.testspark.tools.llm.generation

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import org.jetbrains.research.testspark.bundles.llm.LLMMessagesBundle
import org.jetbrains.research.testspark.bundles.plugin.PluginMessagesBundle
import org.jetbrains.research.testspark.core.data.TestGenerationData
import org.jetbrains.research.testspark.core.generation.llm.FeedbackCycleExecutionResult
import org.jetbrains.research.testspark.core.generation.llm.LLMWithFeedbackCycle
import org.jetbrains.research.testspark.core.generation.llm.prompt.PromptSizeReductionStrategy
import org.jetbrains.research.testspark.core.monitor.ErrorMonitor
import org.jetbrains.research.testspark.core.progress.CustomProgressIndicator
import org.jetbrains.research.testspark.core.test.TestsPresenter
import org.jetbrains.research.testspark.core.test.data.TestSuiteGeneratedByLLM
import org.jetbrains.research.testspark.data.FragmentToTestData
import org.jetbrains.research.testspark.data.IJReport
import org.jetbrains.research.testspark.data.ProjectContext
import org.jetbrains.research.testspark.data.UIContext
import org.jetbrains.research.testspark.services.LLMSettingsService
import org.jetbrains.research.testspark.services.PluginSettingsService
import org.jetbrains.research.testspark.settings.llm.LLMSettingsState
import org.jetbrains.research.testspark.core.ProjectUnderTestFileCreator
import org.jetbrains.research.testspark.tools.TestCompilerFactory
import org.jetbrains.research.testspark.tools.TestProcessor
import org.jetbrains.research.testspark.tools.ToolUtils
import org.jetbrains.research.testspark.tools.llm.SettingsArguments
import org.jetbrains.research.testspark.tools.llm.error.LLMErrorManager
import org.jetbrains.research.testspark.tools.llm.test.JUnitTestSuitePresenter
import org.jetbrains.research.testspark.tools.template.generation.ProcessManager

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
    projectSDK: Sdk? = null
) : ProcessManager {
    private val llmSettingsState: LLMSettingsState
        get() = project.getService(LLMSettingsService::class.java).state

    private val testFileName: String = "GeneratedTest.java"
    private val log = Logger.getInstance(this::class.java)
    private val llmErrorManager: LLMErrorManager = LLMErrorManager()
    private val maxRequests = SettingsArguments(project).maxLLMRequest()
    private val testProcessor = TestProcessor(project, projectSDK)
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
        generatedTestsData: TestGenerationData,
        errorMonitor: ErrorMonitor,
    ): UIContext? {
        log.info("LLM test generation begins")

        if (ToolUtils.isProcessStopped(errorMonitor, indicator)) return null

        // update build path
        var buildPath = projectContext.projectClassPath!!
        ProjectUnderTestFileCreator.log("buildPath from ProjectContextService: '$buildPath'")

        if (project.service<PluginSettingsService>().state.buildPath.isEmpty()) {
            // User did not set own path
            buildPath = ToolUtils.getBuildPath(project)
        }
        ProjectUnderTestFileCreator.log("Final buildPath: '$buildPath'")
        // println("Final buildPath: '$buildPath'")

        if (buildPath.isEmpty() || buildPath.isBlank()) {
            llmErrorManager.errorProcess(LLMMessagesBundle.get("emptyBuildPath"), project, errorMonitor)
            return null
        }
        indicator.setText(PluginMessagesBundle.get("searchMessage"))

        val report = IJReport()

        val initialPromptMessage = promptManager.generatePrompt(codeType, testSamplesCode, generatedTestsData.polyDepthReducing)

        val testCompiler = testProcessor.testCompiler

        // initiate a new RequestManager
        val requestManager = StandardRequestManagerFactory(project).getRequestManager(project)

        // adapter for the existing prompt reduction functionality
        val promptSizeReductionStrategy = object : PromptSizeReductionStrategy {
            override fun isReductionPossible(): Boolean = promptManager.isPromptSizeReductionPossible(generatedTestsData)

            override fun reduceSizeAndGeneratePrompt(): String {
                if (!isReductionPossible()) {
                    throw IllegalStateException("Prompt size reduction is not possible yet requested")
                }
                val reductionSuccess = promptManager.reducePromptSize(generatedTestsData)
                assert(reductionSuccess)

                return promptManager.generatePrompt(codeType, testSamplesCode, generatedTestsData.polyDepthReducing)
            }
        }

        // adapter for the existing test case/test suite string representing functionality
        val testsPresenter = object : TestsPresenter {
            private val testSuitePresenter = JUnitTestSuitePresenter(project, generatedTestsData)

            override fun representTestSuite(testSuite: TestSuiteGeneratedByLLM): String {
                return testSuitePresenter.toStringWithoutExpectedException(testSuite)
            }

            override fun representTestCase(testSuite: TestSuiteGeneratedByLLM, testCaseIndex: Int): String {
                return testSuitePresenter.toStringSingleTestCaseWithoutExpectedException(testSuite, testCaseIndex)
            }
        }

        // Asking LLM to generate a test suite. Here we have a feedback cycle for LLM in case of wrong responses
        val llmFeedbackCycle = LLMWithFeedbackCycle(
            report = report,
            initialPromptMessage = initialPromptMessage,
            promptSizeReductionStrategy = promptSizeReductionStrategy,
            testSuiteFilename = testFileName,
            packageName = packageName,
            resultPath = generatedTestsData.resultPath,
            buildPath = buildPath,
            requestManager = requestManager,
            testsAssembler = JUnitTestsAssembler(project, indicator, generatedTestsData),
            testCompiler = testCompiler,
            testStorage = testProcessor,
            testsPresenter = testsPresenter,
            indicator = indicator,
            requestsCountThreshold = maxRequests,
            errorMonitor = errorMonitor,
        )

        val feedbackResponse = llmFeedbackCycle.run { warning ->
            when (warning) {
                LLMWithFeedbackCycle.WarningType.TEST_SUITE_PARSING_FAILED ->
                    llmErrorManager.warningProcess(LLMMessagesBundle.get("emptyResponse"), project)
                LLMWithFeedbackCycle.WarningType.NO_TEST_CASES_GENERATED ->
                    llmErrorManager.warningProcess(LLMMessagesBundle.get("emptyResponse"), project)
                LLMWithFeedbackCycle.WarningType.COMPILATION_ERROR_OCCURRED ->
                    llmErrorManager.warningProcess(LLMMessagesBundle.get("compilationError"), project)
            }
        }

        // Process stopped checking
        if (ToolUtils.isProcessStopped(errorMonitor, indicator)) return null
        log.info("Feedback cycle finished execution with ${feedbackResponse.executionResult} result code")

        when (feedbackResponse.executionResult) {
            FeedbackCycleExecutionResult.OK -> {
                log.info("Add ${feedbackResponse.compilableTestCases.size} compilable test cases into generatedTestsData")
                // store compilable test cases
                generatedTestsData.compilableTestCases.addAll(feedbackResponse.compilableTestCases)
            }
            FeedbackCycleExecutionResult.NO_COMPILABLE_TEST_CASES_GENERATED -> {
                llmErrorManager.errorProcess(LLMMessagesBundle.get("invalidLLMResult"), project, errorMonitor)
            }
            FeedbackCycleExecutionResult.CANCELED -> {
                log.info("Process stopped")
                return null
            }
            FeedbackCycleExecutionResult.PROVIDED_PROMPT_TOO_LONG -> {
                llmErrorManager.errorProcess(LLMMessagesBundle.get("tooLongPromptRequest"), project, errorMonitor)
                return null
            }
            FeedbackCycleExecutionResult.SAVING_TEST_FILES_ISSUE -> {
                llmErrorManager.errorProcess(LLMMessagesBundle.get("savingTestFileIssue"), project, errorMonitor)
            }
        }

        if (ToolUtils.isProcessStopped(errorMonitor, indicator)) return null

        // Error during the collecting
        if (errorMonitor.hasErrorOccurred()) return null

        log.info("Save generated test suite and test cases into the project workspace")

        val testSuitePresenter = JUnitTestSuitePresenter(project, generatedTestsData)
        val generatedTestSuite: TestSuiteGeneratedByLLM? = feedbackResponse.generatedTestSuite
        val testSuiteRepresentation =
            if (generatedTestSuite != null) testSuitePresenter.toString(generatedTestSuite) else null

        ToolUtils.transferToIJTestCases(report)

        ToolUtils.saveData(
            project,
            report,
            ToolUtils.getPackageFromTestSuiteCode(testSuiteCode = testSuiteRepresentation),
            ToolUtils.getImportsCodeFromTestSuiteCode(testSuiteRepresentation, projectContext.classFQN!!),
            projectContext.fileUrlAsString!!,
            generatedTestsData,
        )

        return UIContext(projectContext, generatedTestsData, requestManager, errorMonitor)
    }
}
