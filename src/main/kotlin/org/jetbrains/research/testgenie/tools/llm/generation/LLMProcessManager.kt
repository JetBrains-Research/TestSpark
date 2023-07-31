package org.jetbrains.research.testgenie.tools.llm.generation

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import org.jetbrains.research.testgenie.TestGenieBundle
import org.jetbrains.research.testgenie.data.CodeType
import org.jetbrains.research.testgenie.data.FragmentToTestDada
import org.jetbrains.research.testgenie.data.Report
import org.jetbrains.research.testgenie.editor.Workspace
import org.jetbrains.research.testgenie.services.ErrorService
import org.jetbrains.research.testgenie.services.SettingsProjectService
import org.jetbrains.research.testgenie.tools.getBuildPath
import org.jetbrains.research.testgenie.tools.getImportsCodeFromTestSuiteCode
import org.jetbrains.research.testgenie.tools.getKey
import org.jetbrains.research.testgenie.tools.getPackageFromTestSuiteCode
import org.jetbrains.research.testgenie.tools.isPromptLengthWithinLimit
import org.jetbrains.research.testgenie.tools.llm.SettingsArguments
import org.jetbrains.research.testgenie.tools.llm.error.LLMErrorManager
import org.jetbrains.research.testgenie.tools.llm.test.TestSuiteGeneratedByLLM
import org.jetbrains.research.testgenie.tools.processStopped
import org.jetbrains.research.testgenie.tools.saveData
import org.jetbrains.research.testgenie.tools.template.generation.ProcessManager
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.createDirectories

/**
 * LLMProcessManager is a class that implements the ProcessManager interface
 * and is responsible for generating tests using the LLM tool.
 *
 * @property project The project in which the test generation is being performed.
 * @property prompt The prompt to be sent to the LLM tool.
 * @property testFileName The name of the generated test file.
 * @property log An instance of the logger class for logging purposes.
 * @property llmErrorManager An instance of the LLMErrorManager class.
 * @property llmRequestManager An instance of the LLMRequestManager class.
 * @property maxRequests The maximum number of requests to be sent to LLM.
 */
class LLMProcessManager(
    private val project: Project,
    private val prompt: String,
) : ProcessManager {
    private val settingsProjectState = project.service<SettingsProjectService>().state
    private var testFileName: String = "GeneratedTest.java"
    private val log = Logger.getInstance(this::class.java)
    private val llmErrorManager: LLMErrorManager = LLMErrorManager()
    private val llmRequestManager = LLMRequestManager()
    private val maxRequests = SettingsArguments.maxLLMRequest()

    /**
     * Runs the test generator process.
     *
     * @param indicator The progress indicator for tracking the progress of the test generation process.
     * @param codeType The type of code to generate tests for.
     * @param projectClassPath The class path of the project.
     * @param resultPath The path to save the generated test results.
     * @param serializeResultPath The serialized result path.
     * @param packageName The package name of the code being tested.
     * @param cutModule The module to cut.
     * @param classFQN The fully qualified name of the class being tested.
     * @param fileUrl The URL of the file being tested.
     * @param testResultName The name of the test result.
     * @param baseDir The base directory of the project.
     * @param modificationStamp The modification stamp of the file being tested.
     */
    override fun runTestGenerator(
        indicator: ProgressIndicator,
        codeType: FragmentToTestDada,
        projectClassPath: String,
        resultPath: String,
        serializeResultPath: String,
        packageName: String,
        cutModule: Module,
        classFQN: String,
        fileUrl: String,
        testResultName: String,
        baseDir: String,
        modificationStamp: Long,
    ) {
        log.info("LLM test generation begins")

        if (processStopped(project, indicator)) return

        if (!isPromptLengthWithinLimit(prompt)) {
            llmErrorManager.errorProcess(TestGenieBundle.message("tooLongPrompt"), project)
            return
        }

        if (codeType.type == CodeType.METHOD) {
            project.service<Workspace>().key = getKey(
                fileUrl,
                "$classFQN#${codeType.objectDescription}",
                modificationStamp,
                testResultName,
                projectClassPath,
            )
        }

        // update build path
        var buildPath = projectClassPath
        if (settingsProjectState.buildPath.isEmpty()) {
            // User did not set own path
            buildPath = getBuildPath(project)
        }

        if (buildPath.isEmpty() || buildPath.isBlank()) {
            llmErrorManager.errorProcess(TestGenieBundle.message("emptyBuildPath"), project)
            return
        }
        indicator.text = TestGenieBundle.message("searchMessage")

        // Send the first request to LLM
        var generatedTestSuite: TestSuiteGeneratedByLLM? =
            llmRequestManager.request(prompt, indicator, packageName, project, llmErrorManager)

        log.info("Generated tests suite received")

        var generatedTestsArePassing = false

        var report: Report? = null
        var requestsCount = 0

        // Asking LLM to generate test. Here, we have a loop to make feedback cycle for LLm in case of wrong responses.
        while (!generatedTestsArePassing) {
            log.info("New iterations of requests")

            if (processStopped(project, indicator)) return

            if (requestsCount >= maxRequests) {
                llmErrorManager.errorProcess(TestGenieBundle.message("invalidGrazieResult"), project)
                break
            }

            // Check if response is not empty
            if (generatedTestSuite == null || generatedTestSuite.testCases.isEmpty()) {
                llmErrorManager.warningProcess(TestGenieBundle.message("emptyResponse"), project)
                requestsCount++
                generatedTestSuite = llmRequestManager.request(
                    "You have provided an empty answer! Please answer my previous question with the same formats",
                    indicator,
                    packageName,
                    project,
                    llmErrorManager,
                )
                continue
            }

            log.info("Result is not empty")

            // Save the generated TestSuite into a temp file
            val generatedTestPath: String = saveGeneratedTests(generatedTestSuite, resultPath)
            if (!File(generatedTestPath).exists()) {
                llmErrorManager.errorProcess(TestGenieBundle.message("savingTestFileIssue"), project)
                break
            }

            // Collect coverage information for each generated test method and display it
            val coverageCollector = TestCoverageCollector(
                indicator,
                project,
                classFQN,
                resultPath,
                File("$generatedTestPath$testFileName"),
                generatedTestSuite.getPrintablePackageString(),
                buildPath,
                generatedTestSuite.testCases,
                cutModule,
                fileUrl.split(File.separatorChar).last(),
            )

            // compile the test file
            val compilationResult = coverageCollector.compile()
            if (!compilationResult.first) {
                log.info("Incorrect result: \n$generatedTestSuite")
                llmErrorManager.warningProcess(TestGenieBundle.message("compilationError"), project)
                requestsCount++
                generatedTestSuite = llmRequestManager.request(
                    "I cannot compile the tests that you provided. The error is:\n${compilationResult.second}\n Fix this issue in the provided tests.\n return the fixed tests between ```",
                    indicator,
                    packageName,
                    project,
                    llmErrorManager,
                )
                continue
            }

            log.info("Result is compilable")

            generatedTestsArePassing = true
            report = coverageCollector.collect()
        }

        if (processStopped(project, indicator)) return

        // Error during the collecting
        if (project.service<ErrorService>().isErrorOccurred()) return

        log.info("Result is ready")

        saveData(
            project,
            report!!,
            testResultName,
            fileUrl,
            getPackageFromTestSuiteCode(generatedTestSuite.toString()),
            getImportsCodeFromTestSuiteCode(generatedTestSuite.toString(), classFQN),
        )
    }

    /**
     * Saves the generated test suite to a file at the specified result path.
     *
     * @param generatedTestSuite the test suite generated by LLM
     * @param resultPath the path where the generated tests should be saved
     * @return the path where the tests are saved
     */
    private fun saveGeneratedTests(generatedTestSuite: TestSuiteGeneratedByLLM, resultPath: String): String {
        // Generate the final path for the generated tests
        var generatedTestPath = "$resultPath${File.separatorChar}"
        generatedTestSuite.packageString.split(".").forEach { directory ->
            if (directory.isNotBlank()) generatedTestPath += "$directory${File.separatorChar}"
        }
        Path(generatedTestPath).createDirectories()

        // Save the generated test suite to the file
        val testFile = File("$generatedTestPath${File.separatorChar}$testFileName")
        testFile.createNewFile()
        log.info("Save test in file " + testFile.absolutePath)
        testFile.writeText(generatedTestSuite.toStringWithoutExpectedException())

        return generatedTestPath
    }
}
