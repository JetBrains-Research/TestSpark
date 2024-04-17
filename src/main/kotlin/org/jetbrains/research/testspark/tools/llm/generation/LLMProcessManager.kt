package org.jetbrains.research.testspark.tools.llm.generation

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.bundles.TestSparkBundle
import org.jetbrains.research.testspark.data.FragmentToTestData
import org.jetbrains.research.testspark.data.Report
import org.jetbrains.research.testspark.data.TestCase
import org.jetbrains.research.testspark.services.ErrorService
import org.jetbrains.research.testspark.services.JavaClassBuilderService
import org.jetbrains.research.testspark.services.LLMChatService
import org.jetbrains.research.testspark.services.ProjectContextService
import org.jetbrains.research.testspark.services.SettingsProjectService
import org.jetbrains.research.testspark.services.TestGenerationDataService
import org.jetbrains.research.testspark.services.TestStorageProcessingService
import org.jetbrains.research.testspark.tools.*
import org.jetbrains.research.testspark.tools.llm.SettingsArguments
import org.jetbrains.research.testspark.tools.llm.error.LLMErrorManager
import org.jetbrains.research.testspark.tools.llm.test.TestCaseGeneratedByLLM
import org.jetbrains.research.testspark.tools.llm.test.TestSuiteGeneratedByLLM
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
) : ProcessManager {
    private val settingsProjectState = project.service<SettingsProjectService>().state
    private val testFileName: String = "GeneratedTest.java"
    private val log = Logger.getInstance(this::class.java)
    private val llmErrorManager: LLMErrorManager = LLMErrorManager()
    private val maxRequests = SettingsArguments.maxLLMRequest()

    /**
     * Runs the test generator process.
     *
     * @param indicator The progress indicator for tracking the progress of the test generation process.
     * @param codeType The type of code to generate tests for.
     * @param packageName The package name of the code being tested.
     */
    override fun runTestGenerator(
        indicator: ProgressIndicator?,
        codeType: FragmentToTestData,
        packageName: String,
    ) {
        ProjectUnderTestFileCreator.log("LLM test generation begins")
        // println("LLM test generation begins")
        log.info("LLM test generation begins")

        if (processStopped(project, indicator)) return

        // update build path
        var buildPath = project.service<ProjectContextService>().projectClassPath!!
        ProjectUnderTestFileCreator.log("buildPath from ProjectContextService: '$buildPath'")
        // println("buildPath from ProjectContextService: '$buildPath'")

        if (settingsProjectState.buildPath.isEmpty()) {
            // User did not set own path
            buildPath = getBuildPath(project)
        }
        ProjectUnderTestFileCreator.log("Final buildPath: '$buildPath'")
        // println("Final buildPath: '$buildPath'")

        if (buildPath.isEmpty() || buildPath.isBlank()) {
            llmErrorManager.errorProcess(TestSparkBundle.message("emptyBuildPath"), project)
            return
        }
        indicator?.text = TestSparkBundle.message("searchMessage")

        var generatedTestsArePassing = false

        val report = Report()

        var requestsCount = 0
        var warningMessage = ""
        var messageToPrompt = promptManager.generatePrompt(codeType)
        var generatedTestSuite: TestSuiteGeneratedByLLM? = null

        // notify LLMChatService to restart the chat process.
        project.service<LLMChatService>().newSession()

        ProjectUnderTestFileCreator.log("Max requests count is $maxRequests")
        // println("Max requests count is $maxRequests")

        // Asking LLM to generate test. Here, we have a loop to make feedback cycle for LLm in case of wrong responses.
        while (!generatedTestsArePassing) {
            requestsCount++

            ProjectUnderTestFileCreator.log(
                "============================== Feedback Cycle Iteration $requestsCount/$maxRequests ==============================")
            // println("============================== Feedback Cycle Iteration $requestsCount/$maxRequests ==============================")
            log.info(
                "============================== Feedback Cycle Iteration $requestsCount/$maxRequests ==============================")

            // Process stopped checking
            if (processStopped(project, indicator)) return

            // Ending loop checking
            if (isLastIteration(requestsCount) && project.service<TestGenerationDataService>().compilableTestCases.isEmpty()) {
                ProjectUnderTestFileCreator.log("No compilable test cases on last feedback cycle iteration")

                if (generatedTestSuite != null) {
                    ProjectUnderTestFileCreator.log("Remove test cases from test suite")
                    generatedTestSuite.testCases = mutableListOf()
                }

                // println("No compilable test cases on the last feedback cycle iteration")
                llmErrorManager.errorProcess(TestSparkBundle.message("invalidLLMResult"), project)
                break
            }

            // Send request to LLM
            if (warningMessage.isNotEmpty()) {
                llmErrorManager.warningProcess(warningMessage, project)
            }

            val requestResult: Pair<String, TestSuiteGeneratedByLLM?> =
                project.service<LLMChatService>().testGenerationRequest(
                    messageToPrompt, indicator, packageName, project, llmErrorManager)

            if (requestResult.first == TestSparkBundle.message("tooLongPrompt")) {
                ProjectUnderTestFileCreator.log("The generated prompt is too long: ${messageToPrompt.length} characters")
                // println("The generated prompt is too long: ${messageToPrompt.length} characters")
                if (promptManager.reducePromptSize()) {
                    ProjectUnderTestFileCreator.log("Prompt size reduction is possible, reducing prompt...")
                    // println("Prompt size reduction is possible, reducing prompt...")
                    messageToPrompt = promptManager.generatePrompt(codeType)
                    requestsCount--
                    continue
                } else {
                    ProjectUnderTestFileCreator.log("Prompt size reduction is not possible, aborting...")
                    // println("Prompt size reduction is not possible, aborting...")
                    llmErrorManager.errorProcess(TestSparkBundle.message("tooLongPromptRequest"), project)
                    return
                }
            }
            generatedTestSuite = requestResult.second

            // Process stopped checking
            if (processStopped(project, indicator)) return

            // Bad response checking
            if (generatedTestSuite == null) {
                messageToPrompt = requestResult.first
                continue
            }

            // Empty response checking
            if (generatedTestSuite.testCases.isEmpty()) {
                warningMessage = TestSparkBundle.message("emptyResponse")
                messageToPrompt =
                    "You have provided an empty answer! Please answer my previous question with the same formats."
                continue
            }

            // Save the generated TestSuite into a temp file
            val generatedTestCasesPaths: MutableList<String> = mutableListOf()
            if (isLastIteration(requestsCount)) {
                val cnt = project.service<TestGenerationDataService>().compilableTestCases.size
                ProjectUnderTestFileCreator.log("Update the test suite with $cnt compilable test cases (last iteration)")
                // println("Update the test suite with $cnt compilable test cases (last iteration)")
                generatedTestSuite.updateTestCases(project.service<TestGenerationDataService>().compilableTestCases.toMutableList())
            } else {
                for (testCaseIndex in generatedTestSuite.testCases.indices) {
                    generatedTestCasesPaths.add(
                        project.service<TestStorageProcessingService>().saveGeneratedTest(
                            generatedTestSuite.packageString,
                            generatedTestSuite.toStringSingleTestCaseWithoutExpectedException(testCaseIndex),
                            project.service<ProjectContextService>().resultPath!!,
                            "${project.service<JavaClassBuilderService>().getClassWithTestCaseName(generatedTestSuite.testCases[testCaseIndex].name)}.java",
                        ),
                    )
                }
            }

            val generatedTestPath: String = project.service<TestStorageProcessingService>().saveGeneratedTest(
                generatedTestSuite.packageString,
                generatedTestSuite.toStringWithoutExpectedException(),
                project.service<ProjectContextService>().resultPath!!,
                testFileName,
            )

            // Correct files creating checking
            var isFilesExists = true
            for (path in generatedTestCasesPaths) {
                isFilesExists = isFilesExists && File(path).exists()
            }
            if (!isFilesExists || !File(generatedTestPath).exists()) {
                llmErrorManager.errorProcess(TestSparkBundle.message("savingTestFileIssue"), project)
                break
            }

            ProjectUnderTestFileCreator.log("Test suite is saved in: '${generatedTestPath}'")
            ProjectUnderTestFileCreator.log("Test cases are saved in:\n'${generatedTestCasesPaths}'")
            // println("Test suite is saved in: '${generatedTestPath}'")
            // println("Test cases are saved in:\n'${generatedTestCasesPaths}'")

            // Get test cases
            val testCases: MutableList<TestCaseGeneratedByLLM> =
                if (!isLastIteration(requestsCount)) {
                    generatedTestSuite.testCases
                } else {
                    project.service<TestGenerationDataService>().compilableTestCases.toMutableList()
                }

            // Compile the test file
            indicator?.text = TestSparkBundle.message("compilationTestsChecking")
            val separateCompilationResult = project.service<TestStorageProcessingService>().compileTestCases(generatedTestCasesPaths, buildPath, testCases)
            val commonCompilationResult = project.service<TestStorageProcessingService>().compileCode(File(generatedTestPath).absolutePath, buildPath)

            ProjectUnderTestFileCreator.log(
                "Separate compilation of test cases: $separateCompilationResult, compilation of test suite: ${commonCompilationResult.first}")

            if (!separateCompilationResult && !isLastIteration(requestsCount)) {
                ProjectUnderTestFileCreator.log("Some test cases were not compilable (iteration $requestsCount/$maxRequests)")
                ProjectUnderTestFileCreator.log("Test suite compilation failed with an error:\n\"${commonCompilationResult.second}\"")
                // println("Some test cases were not compilable (iteration $requestsCount/$maxRequests)")
                // println("Test suite compilation failed with an error:\n\"${commonCompilationResult.second}\"")

                log.info("Incorrect result: \n$generatedTestSuite")

                warningMessage = TestSparkBundle.message("compilationError")
                messageToPrompt = "I cannot compile the tests that you provided. The error is:\n${commonCompilationResult.second}\n Fix this issue in the provided tests.\n return the fixed tests between ```"
                continue
            }

            ProjectUnderTestFileCreator.log("${testCases.size} test cases are compilable. Finishing feedback cycle...")
            // println("${testCases.size} test cases are compilable. Finishing feedback cycle...")
            log.info("Result is compilable")

            generatedTestsArePassing = true

            for (index in testCases.indices) {
                report.testCaseList[index] = TestCase(index, testCases[index].name, testCases[index].toString(), setOf(), setOf(), setOf())
            }
        }

        ProjectUnderTestFileCreator.log("Generation has finished: generatedTestsArePassing: $generatedTestsArePassing, iterations used: $requestsCount/$maxRequests\"")
        // println("Generation has finished: generatedTestsArePassing: $generatedTestsArePassing, iterations used: $requestsCount/$maxRequests")

        if (processStopped(project, indicator)) return

        // Error during the collecting
        if (project.service<ErrorService>().isErrorOccurred()) return

        saveData(
            project,
            report,
            getPackageFromTestSuiteCode(generatedTestSuite.toString()),
            getImportsCodeFromTestSuiteCode(generatedTestSuite.toString(), project.service<ProjectContextService>().classFQN!!),
        )
    }

    private fun isLastIteration(requestsCount: Int) = requestsCount > maxRequests
}
