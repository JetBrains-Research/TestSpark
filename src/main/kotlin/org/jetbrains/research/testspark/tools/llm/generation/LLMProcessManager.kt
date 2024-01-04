package org.jetbrains.research.testspark.tools.llm.generation

import com.github.dockerjava.core.DockerClientBuilder
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.TestSparkBundle
import org.jetbrains.research.testspark.data.FragmentToTestData
import org.jetbrains.research.testspark.data.Report
import org.jetbrains.research.testspark.data.TestCase
import org.jetbrains.research.testspark.editor.Workspace
import org.jetbrains.research.testspark.services.ErrorService
import org.jetbrains.research.testspark.services.JavaClassBuilderService
import org.jetbrains.research.testspark.services.LLMChatService
import org.jetbrains.research.testspark.services.SettingsProjectService
import org.jetbrains.research.testspark.services.TestStorageProcessingService
import org.jetbrains.research.testspark.tools.getBuildPath
import org.jetbrains.research.testspark.tools.getImportsCodeFromTestSuiteCode
import org.jetbrains.research.testspark.tools.getPackageFromTestSuiteCode
import org.jetbrains.research.testspark.tools.isPromptLengthWithinLimit
import org.jetbrains.research.testspark.tools.llm.SettingsArguments
import org.jetbrains.research.testspark.tools.llm.error.LLMErrorManager
import org.jetbrains.research.testspark.tools.llm.test.TestCaseGeneratedByLLM
import org.jetbrains.research.testspark.tools.llm.test.TestSuiteGeneratedByLLM
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
    private val prompt: String,
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
        indicator: ProgressIndicator,
        codeType: FragmentToTestData,
        packageName: String,
    ) {
        log.info("LLM test generation begins")

        if (processStopped(project, indicator)) return

        if (!isPromptLengthWithinLimit(prompt)) {
            llmErrorManager.errorProcess(TestSparkBundle.message("tooLongPrompt"), project)
            return
        }

        // update build path
        var buildPath = project.service<Workspace>().projectClassPath!!
        if (settingsProjectState.buildPath.isEmpty()) {
            // User did not set own path
            buildPath = getBuildPath(project)
        }

        if (buildPath.isEmpty() || buildPath.isBlank()) {
            llmErrorManager.errorProcess(TestSparkBundle.message("emptyBuildPath"), project)
            return
        }
        indicator.text = TestSparkBundle.message("searchMessage")

        log.info("Generated tests suite received")

        var generatedTestsArePassing = false

        val report = Report()

        var requestsCount = 0
        var warningMessage = ""
        var messageToPrompt = prompt
        var generatedTestSuite: TestSuiteGeneratedByLLM? = null

        // notify LLMChatService to restart the chat process.
        project.service<LLMChatService>().newSession()
        val dockerClient = DockerClientBuilder.getInstance().build()
        // Asking LLM to generate test. Here, we have a loop to make feedback cycle for LLm in case of wrong responses.
        while (!generatedTestsArePassing) {
            requestsCount++

            log.info("New iterations of requests")

            // Process stopped checking
            if (processStopped(project, indicator)) return

            // Ending loop checking
            if (isLastIteration(requestsCount) && project.service<Workspace>().testGenerationData.compilableTestCases.isEmpty()) {
                llmErrorManager.errorProcess(TestSparkBundle.message("invalidLLMResult"), project)
                break
            }

            // Send request to LLM
            if (warningMessage.isNotEmpty()) llmErrorManager.warningProcess(warningMessage, project)
            val requestResult: Pair<String, TestSuiteGeneratedByLLM?> =
                project.service<LLMChatService>().testGenerationRequest(messageToPrompt, indicator, packageName, project, llmErrorManager)
            generatedTestSuite = requestResult.second

            // Process stopped checking
            if (processStopped(project, indicator)) return

            // Bad response checking
            if (generatedTestSuite == null) {
                warningMessage = TestSparkBundle.message("emptyResponse")
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
                generatedTestSuite.updateTestCases(project.service<Workspace>().testGenerationData.compilableTestCases.toMutableList())
            } else {
                for (testCaseIndex in generatedTestSuite.testCases.indices) {
                    generatedTestCasesPaths.add(
                        project.service<TestStorageProcessingService>().saveGeneratedTest(
                            generatedTestSuite.packageString,
                            generatedTestSuite.toStringSingleTestCaseWithoutExpectedException(testCaseIndex),
                            project.service<Workspace>().resultPath!!,
                            "${project.service<JavaClassBuilderService>().getClassWithTestCaseName(generatedTestSuite.testCases[testCaseIndex].name)}.java",
                        ),
                    )
                }
            }

            val generatedTestPath: String = project.service<TestStorageProcessingService>().saveGeneratedTest(
                generatedTestSuite.packageString,
                generatedTestSuite.toStringWithoutExpectedException(),
                project.service<Workspace>().resultPath!!,
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
                    project.service<Workspace>().testGenerationData.compilableTestCases.toMutableList()
                }

            // Compile the test file
            indicator.text = TestSparkBundle.message("compilationTestsChecking")
            val separateCompilationResult = project.service<TestStorageProcessingService>().compileTestCases(generatedTestCasesPaths, buildPath, testCases)
            val commonCompilationResult = project.service<TestStorageProcessingService>().compileCode(File(generatedTestPath).absolutePath, buildPath)

            if (!separateCompilationResult && !isLastIteration(requestsCount)) {
                log.info("Incorrect result: \n$generatedTestSuite")
                warningMessage = TestSparkBundle.message("compilationError")
                messageToPrompt = "I cannot compile the tests that you provided. The error is:\n${commonCompilationResult.second}\n Fix this issue in the provided tests.\n return the fixed tests between ```"
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

        saveData(
            project,
            report,
            getPackageFromTestSuiteCode(generatedTestSuite.toString()),
            getImportsCodeFromTestSuiteCode(generatedTestSuite.toString(), project.service<Workspace>().classFQN!!),
            indicator,
        )
    }

    private fun isLastIteration(requestsCount: Int) = requestsCount > maxRequests
}
