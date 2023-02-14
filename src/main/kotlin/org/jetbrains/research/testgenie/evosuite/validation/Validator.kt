package org.jetbrains.research.testgenie.evosuite.validation

import com.github.javaparser.ParseProblemException
import com.intellij.coverage.CoverageDataManager
import com.intellij.coverage.CoverageRunner
import com.intellij.coverage.CoverageSuite
import com.intellij.coverage.CoverageSuitesBundle
import com.intellij.coverage.DefaultCoverageFileProvider
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.CapturingProcessAdapter
import com.intellij.execution.process.OSProcessHandler
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Comparing
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentManager
import org.jetbrains.research.testgenie.TestGenieBundle
import org.jetbrains.research.testgenie.TestGenieLabelsBundle
import org.jetbrains.research.testgenie.editor.Workspace
import org.jetbrains.research.testgenie.evosuite.ProjectBuilder
import org.jetbrains.research.testgenie.services.SettingsProjectService
import org.jetbrains.research.testgenie.services.TestCaseDisplayService
import java.io.File
import java.io.FileWriter
import java.nio.charset.Charset
import java.util.regex.Pattern
import javax.tools.JavaCompiler
import javax.tools.ToolProvider

/**
 * Class for validating and calculating the coverage of an optionally
 * edited set of test cases.
 *
 * @param tests a map of test names and their code
 */
class Validator(
    private val project: Project,
    private val testJobInfo: Workspace.TestJobInfo,
    private val tests: HashMap<String, String> // test name, test code
) {
    private val logger: Logger = Logger.getInstance(this.javaClass)
    private val settingsState = project.service<SettingsProjectService>().state
    private val junitTimeout: Long = 12000000 // TODO: Source from config

    private val sep = File.separatorChar
    private val pathSep = File.pathSeparatorChar

    fun validateSuite() {
        val jobName = testJobInfo.jobId

        logger.info("Validating test suite $jobName")

        val fqn = testJobInfo.targetUnit.split('#').first()
        val targetFqn = "${fqn}_ESTest"

        val targetProjectCP = testJobInfo.targetClassPath

        val pluginsPath = System.getProperty("idea.plugins.path")

        val junitPath = "$pluginsPath${sep}TestGenie${sep}lib${sep}junit-4.13.jar"
        val standaloneRuntimePath = "$pluginsPath${sep}TestGenie${sep}lib${sep}standalone-runtime.jar"
        val hamcrestPath = "$pluginsPath${sep}TestGenie${sep}lib${sep}hamcrest-core-1.3.jar"

        val testValidationRoot = "${FileUtilRt.getTempDirectory()}${sep}testGenieResults$sep$jobName-validation"
        val testValidationDirectory = "$testValidationRoot${sep}evosuite-tests"
        val validationDir = File(testValidationDirectory)

        // TODO: Implement classpath builder
        val dependenciesClasspath = "${junitPath}$pathSep$standaloneRuntimePath$pathSep$hamcrestPath"
        val classpath = "${targetProjectCP}${pathSep}$dependenciesClasspath${pathSep}$testValidationDirectory"

        if (!validationDir.exists()) {
            logger.error("Validation dir does not exist! - $testValidationDirectory")
            return
        }
        logger.info("Rebuilding user project...")
        val projectBuilder = ProjectBuilder(project)

        ProgressManager.getInstance()
            .run(object : Task.Backgroundable(project, TestGenieBundle.message("validationCompilation")) {
                override fun run(indicator: ProgressIndicator) {
                    try {
                        projectBuilder.runBuild(indicator)

                        val compilationFiles =
                            setupCompilationFiles(testValidationDirectory, targetFqn)
                                ?: return

                        logger.info("Compiling tests...")
                        val successfulCompilation = compileTests(classpath, compilationFiles)

                        if (!successfulCompilation) {
                            logger.warn("Compilation failed")
                            showTestsCompilationFailed()
                            return
                        }
                        logger.info("Compilation successful!")
                        logger.info("Executing tests...")
                        indicator.text = TestGenieBundle.message("validationRunning")

                        runTests(indicator, classpath, targetFqn)
                        runTestsWithCoverage(indicator, classpath, targetFqn, testValidationRoot)
                        indicator.stop()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onFinished() {
                    super.onFinished()
                    project.service<TestCaseDisplayService>().makeValidatedButtonAvailable()
                }
            })
    }

    private fun setupCompilationFiles(
        testValidationDirectory: String,
        targetFqn: String
    ): List<File>? {

        val baseClassName = "$testValidationDirectory$sep${targetFqn.replace('.', sep)}"
        // flush test edits to file
        val testsPath = "$baseClassName.java"
        val testsFile = File(testsPath)

        val editor = TestCaseEditor(testsFile.readText(), tests)
        val editedTests: String?

        try {
            editedTests = editor.edit()
            val testsFileWriter = FileWriter(testsFile, false)
            testsFileWriter.write(editedTests)
            testsFileWriter.close()
            logger.trace("Flushed tests to $testsPath")
        } catch (e: ParseProblemException) {
            logger.warn("Parsing tests failed - $e")
            showTestsParsingFailed()
            return null
        }

        val testsCovPath = "${baseClassName}_Cov.java"
        val testsCov = File(testsCovPath)
        val testsCovWriter = FileWriter(testsCov, false)
        val testsNoScaffold = editor.editRemoveScaffold(editedTests)

        testsCovWriter.write(testsNoScaffold)
        testsCovWriter.close()

        val scaffoldPath = "${baseClassName}_scaffolding.java"
        val scaffoldFile = File(scaffoldPath)

        return listOf(scaffoldFile, testsFile, testsCov)
    }

    /**
     * Compiles the provided test files with the provided classpath
     */
    private fun compileTests(classpath: String, files: List<File>): Boolean {
        logger.trace("Compiling with classpath $classpath")

        val optionList: List<String> = listOf("-classpath", classpath)

        val compiler: JavaCompiler = ToolProvider.getSystemJavaCompiler()

        val fileManager = compiler.getStandardFileManager(null, null, null)

        val compilationUnits = fileManager.getJavaFileObjectsFromFiles(files)

        val task = compiler.getTask(
            null, fileManager, null, optionList, null, compilationUnits
        )

        val compiled = task.call()
        fileManager.close()

        return compiled
    }

    /**
     * Runs the compiled tests
     *
     * @param indicator the progress indicator
     */
    private fun runTests(
        indicator: ProgressIndicator,
        classpath: String,
        testFqn: String,
    ) {
        indicator.isIndeterminate = false
        settingsState

        // construct command
        val cmd = ArrayList<String>()
        cmd.add(settingsState.javaPath)
        cmd.add("-cp")
        cmd.add(classpath)
        cmd.add("org.junit.runner.JUnitCore")
        cmd.add(testFqn)

        val cmdString = cmd.fold(String()) { acc, e -> acc.plus(e).plus(" ") }
        logger.info("Running junit tests with: $cmdString")

        val junitProcess = GeneralCommandLine(cmd)
        junitProcess.charset = Charset.forName("UTF-8")
        val handler = OSProcessHandler(junitProcess)

        // attach console listener for displaying information
        val consoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(project)
        consoleBuilder.setViewer(true)
        val console = consoleBuilder.console
        console.attachToProcess(handler)

        val capturer = CapturingProcessAdapter()
        // attach another listener for parsing process results
        handler.addProcessListener(capturer)
        handler.startNotify()
        val manager: ToolWindowManager = ToolWindowManager.getInstance(project)

        ApplicationManager.getApplication().invokeLater {
            val window = manager.getToolWindow("TestGenie Validator")!!
            val contentManager: ContentManager = window.contentManager
            contentManager.removeAllContents(true)
            val content: Content = contentManager.factory.createContent(
                console.component, TestGenieLabelsBundle.defaultValue("junitRun"), false
            )
            contentManager.addContent(content)
        }

        // treat this as a join handle
        handler.waitFor(junitTimeout)

        val output = capturer.output.stdout

        val junitResult = parseJunitResult(output)
        showValidationResult(junitResult)

        project.messageBus.syncPublisher(VALIDATION_RESULT_TOPIC).validationResult(junitResult)
    }

    /**
     * Runs the compiled tests
     *
     * @param indicator the progress indicator
     */
    private fun runTestsWithCoverage(
        indicator: ProgressIndicator,
        classpath: String,
        testFqn: String,
        testValidationRoot: String,
    ) {
        indicator.text = "Calculating coverage"

        val pluginsPath = System.getProperty("idea.plugins.path")
        val jacocoPath = "$pluginsPath${sep}TestGenie${sep}lib${sep}jacocoagent.jar"
        // construct command
        val jacocoReportPath = "$testValidationRoot${sep}jacoco.exec"
        // delete old report
        File(jacocoReportPath).delete()
        val cmd = ArrayList<String>()
        cmd.add(settingsState.javaPath)
        cmd.add("-javaagent:$jacocoPath=destfile=$jacocoReportPath")
        cmd.add("-cp")
        cmd.add(classpath)
        cmd.add("org.junit.runner.JUnitCore")
        cmd.add("${testFqn}_Cov")

        val cmdString = cmd.fold(String()) { acc, e -> acc.plus(e).plus(" ") }
        logger.info("Running tests for coverage with: $cmdString")

        val junitProcess = GeneralCommandLine(cmd)
        junitProcess.charset = Charset.forName("UTF-8")
        val handler = OSProcessHandler(junitProcess)

        val cap = CapturingProcessAdapter()
        // attach another listener for parsing process results
        handler.addProcessListener(cap)
        handler.startNotify()

        // treat this as a join handle
        handler.waitFor(junitTimeout)

        val manager = CoverageDataManager.getInstance(project)
        val virtualFile = LocalFileSystem.getInstance().findFileByPath(jacocoReportPath)!!
        val coverageRunner = getCoverageRunner(virtualFile)

        val coverageSuite: CoverageSuite = manager
            .addExternalCoverageSuite(
                virtualFile.name, virtualFile.timeStamp, coverageRunner,
                DefaultCoverageFileProvider(virtualFile.path)
            )

        val testCaseDisplayService = project.service<TestCaseDisplayService>()
        testCaseDisplayService.setJacocoReport(CoverageSuitesBundle(coverageSuite))
        testCaseDisplayService.toggleJacocoButton.isEnabled = true
    }

    private fun getCoverageRunner(file: VirtualFile): CoverageRunner? {
        for (runner in CoverageRunner.EP_NAME.extensionList) {
            for (extension in runner.dataFileExtensions) {
                if (Comparing.strEqual(file.extension, extension)) return runner
            }
        }
        return null
    }

    /**
     * Method to show notification that the tests cannot be compiled.
     */
    private fun showTestsCompilationFailed() {
        NotificationGroupManager.getInstance().getNotificationGroup("Test Validation Error").createNotification(
            TestGenieBundle.message("compilationFailedNotificationTitle"),
            TestGenieBundle.message("compilationFailedNotificationText"),
            NotificationType.ERROR
        ).notify(project)
    }

    /**
     * Method to show notification that the tests cannot be parsed.
     */
    private fun showTestsParsingFailed() {
        NotificationGroupManager.getInstance().getNotificationGroup("Test Validation Error").createNotification(
            TestGenieBundle.message("compilationFailedNotificationTitle"),
            TestGenieBundle.message("compilationFailedNotificationText"),
            NotificationType.ERROR
        ).notify(project)
    }

    /**
     * Method to show validation results
     */
    private fun showValidationResult(junitResult: JUnitResult) {
        val passed = junitResult.totalTests - junitResult.failedTests
        NotificationGroupManager.getInstance().getNotificationGroup("Validation Result").createNotification(
            TestGenieBundle.message("validationResult"),
            "$passed/${junitResult.totalTests}",
            NotificationType.INFORMATION
        ).notify(project)
    }

    data class JUnitResult(val totalTests: Int, val failedTests: Int, val failedTestNames: Set<String>)

    companion object {
        fun parseJunitResult(cap: String): JUnitResult {

            val output = cap.trimEnd()
            val resultString = output.substring(output.lastIndexOf("\n")).trim()

            if (resultString.startsWith("OK")) {
                val successMatcher = Pattern.compile("(\\d+)").matcher(resultString)
                successMatcher.find()
                val total = successMatcher.group().toInt()
                return JUnitResult(total, 0, emptySet())
            } else {
                val failMatcher = Pattern.compile("\\d+").matcher(resultString)
                failMatcher.find()
                val total = failMatcher.group().toInt()
                failMatcher.find()
                val failed = failMatcher.group().toInt()
                val failedCaseMatcher = Pattern.compile("\\d\\) (\\w*)\\(.*\n").matcher(output)
                val cases = mutableSetOf<String>()
                while (failedCaseMatcher.find()) {
                    val testName = failedCaseMatcher.group(1).trim()
                    cases.add(testName)
                }

                return JUnitResult(total, failed, cases)
            }
        }
    }
}
