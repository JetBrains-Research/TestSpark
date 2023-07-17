package org.jetbrains.research.testgenie.tools.llm.generation

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import org.jetbrains.research.testgenie.TestGenieBundle
import org.jetbrains.research.testgenie.data.CodeTypeAndAdditionData
import org.jetbrains.research.testgenie.data.Report
import org.jetbrains.research.testgenie.services.SettingsProjectService
import org.jetbrains.research.testgenie.tools.getBuildPath
import org.jetbrains.research.testgenie.tools.llm.error.LLMErrorManager
import org.jetbrains.research.testgenie.tools.llm.test.TestSuiteGeneratedByLLM
import org.jetbrains.research.testgenie.tools.getImportsCodeFromTestSuiteCode
import org.jetbrains.research.testgenie.tools.getPackageFromTestSuiteCode
import org.jetbrains.research.testgenie.tools.llm.SettingsArguments
import org.jetbrains.research.testgenie.tools.saveData
import org.jetbrains.research.testgenie.tools.template.generation.ProcessManager
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.createDirectories

class LLMProcessManager(
    private val project: Project,
    private val projectClassPath: String,
    classesToTest: MutableList<PsiClass>,
    interestingPsiClasses: MutableSet<PsiClass>,
    polymorphismRelations: MutableMap<PsiClass, MutableList<PsiClass>>,
) : ProcessManager {
    private val settingsProjectState = project.service<SettingsProjectService>().state
    private var testFileName: String = "GeneratedTest.java"
    private val log = Logger.getInstance(this::class.java)
    private val llmErrorManager: LLMErrorManager = LLMErrorManager()
    private val llmRequestManager = LLMRequestManager()
    private val maxRequests = SettingsArguments.maxLLMRequest()
    private val prompt = PromptManager(classesToTest[0], classesToTest, interestingPsiClasses, polymorphismRelations).generatePrompt()

    override fun runTestGenerator(
        indicator: ProgressIndicator,
        codeType: CodeTypeAndAdditionData,
        resultPath: String,
        serializeResultPath: String,
        packageName: String,
        cutModule: Module,
        classFQN: String,
        fileUrl: String,
        testResultName: String,
        baseDir: String,
        log: Logger,
    ) {
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
        // Asking LLM to generate test. Here, we have a loop to make feedback cycle for LLm in case of wrong responses.

        // Send the first request to LLM
        var generatedTestSuite: TestSuiteGeneratedByLLM? = llmRequestManager.request(prompt, indicator, packageName, project, llmErrorManager)
        var generatedTestsArePassing = false

        var report: Report? = null
        var requestsCount = 0

        while (!generatedTestsArePassing) {
            if (requestsCount >= maxRequests) {
                llmErrorManager.errorProcess(TestGenieBundle.message("invalidGrazieResult"), project)
                break
            }
            // Check if response is not empty
            if (generatedTestSuite == null) {
                llmErrorManager.errorProcess(TestGenieBundle.message("emptyResponse"), project)
                requestsCount++
                generatedTestSuite = llmRequestManager.request("You have provided an empty answer! please answer my previous question with the same formats", indicator, packageName, project, llmErrorManager)
                continue
            }

            // Save the generated TestSuite into a temp file
            val generatedTestPath: String = saveGeneratedTests(generatedTestSuite, resultPath)
            if (!File(generatedTestPath).exists()) {
                llmErrorManager.errorProcess(TestGenieBundle.message("savingTestFileIssue"), project)
            }

            // Collect coverage information for each generated test method and display it
            val coverageCollector = TestCoverageCollector(
                indicator,
                project,
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
                llmErrorManager.warningProcess(TestGenieBundle.message("compilationError"), project)
                requestsCount++
                generatedTestSuite = llmRequestManager.request("I cannot compile the tests that you provided. The error is:\n${compilationResult.second}\n Fix this issue in the provided tests.\n return the fixed etsts between ```", indicator, packageName, project, llmErrorManager)
                continue
            }

            generatedTestsArePassing = true
            report = coverageCollector.collect()
        }

        // Error during the collecting
        report ?: return

        saveData(project, report, testResultName, fileUrl, getPackageFromTestSuiteCode(generatedTestSuite.toString()), getImportsCodeFromTestSuiteCode(generatedTestSuite.toString(), classFQN))
    }

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
        testFile.writeText(generatedTestSuite.toString())

        return generatedTestPath
    }
}
