package nl.tudelft.ewi.se.ciselab.testgenie.evosuite.validation

import com.github.javaparser.ParseProblemException
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
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentManager
import nl.tudelft.ewi.se.ciselab.testgenie.TestGenieBundle
import nl.tudelft.ewi.se.ciselab.testgenie.TestGenieLabelsBundle
import nl.tudelft.ewi.se.ciselab.testgenie.editor.Workspace
import nl.tudelft.ewi.se.ciselab.testgenie.evosuite.ProjectBuilder
import nl.tudelft.ewi.se.ciselab.testgenie.services.SettingsProjectService
import org.jacoco.core.analysis.Analyzer
import org.jacoco.core.analysis.CoverageBuilder
import org.jacoco.core.analysis.IClassCoverage
import org.jacoco.core.tools.ExecFileLoader
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
 * @param edits a map of test names and their edits
 */
class Validator(
    private val project: Project,
    private val testJob: Workspace.TestJob,
    private val activeTests: Set<String>,
    private val edits: HashMap<String, String> // test name, test code
) {
    private val logger: Logger = Logger.getInstance(this.javaClass)
    private val settingsState = project.service<SettingsProjectService>().state
    private val junitTimeout: Long = 12000000 // TODO: Source from config

    private val sep = File.separatorChar
    private val pathSep = File.pathSeparatorChar

    fun validateSuite() {
        val jobName = testJob.info.jobId

        logger.info("Validating test suite $jobName")

        val fqn = testJob.info.targetUnit.split('#').first()
        val targetFqn = "${fqn}_ESTest"

        val targetProjectCP = testJob.info.targetClassPath

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

                        val originalTestSuite = testJob.report.testSuiteCode
                        val compilationFiles =
                            setupCompilationFiles(testValidationDirectory, targetFqn, originalTestSuite)
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
                        runTestsWithCoverage(indicator, classpath, targetFqn, testValidationRoot, targetProjectCP)
                        indicator.stop()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
    }

    private fun setupCompilationFiles(
        testValidationDirectory: String,
        targetFqn: String,
        originalTestSuite: String
    ): List<File>? {

        val baseClassName = "$testValidationDirectory$sep${targetFqn.replace('.', sep)}"
        // flush test edits to file
        val testsPath = "$baseClassName.java"
        val testsFile = File(testsPath)

        val editor = TestCaseEditor(originalTestSuite, edits, activeTests)
        val editedTests: String?

        try {
            editedTests = editor.edit()

            if (edits.size == 0 && activeTests.size == testJob.report.testCaseList.size) {
                logger.trace("No changes found, resetting files to old state")
                val testsFileWriter = FileWriter(testsPath, false)
                testsFileWriter.write(testJob.report.testSuiteCode)
                testsFileWriter.close()
                logger.trace("Flushed original tests to $testsPath")
            } else {
                val testsFileWriter = FileWriter(testsFile, false)
                testsFileWriter.write(editedTests)
                testsFileWriter.close()
                logger.trace("Flushed edited tests to $testsPath")
            }
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
        settingsState ?: return

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
        projectClasses: String
    ) {
        indicator.text = "Calculating coverage"

        val pluginsPath = System.getProperty("idea.plugins.path")
        val jacocoPath = "$pluginsPath${sep}TestGenie${sep}lib${sep}jacocoagent.jar"
        // construct command
        val jacocoReportPath = "$testValidationRoot${sep}jacoco.exec"
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

        val loader = ExecFileLoader()
        loader.load(File(jacocoReportPath))
        val executionData = loader.executionDataStore

        val coverageBuilder = CoverageBuilder()
        val analyzer = Analyzer(executionData, coverageBuilder)
        val count = analyzer.analyzeAll(File(projectClasses))
        logger.info("$count classes analyzed")

        val coverages = coverageBuilder.classes
        logJacoco(coverages)

        val cov = getCoverageLineByLine(coverages)

        project.messageBus.syncPublisher(JACOCO_REPORT_TOPIC).receiveJacocoReport(cov)
    }

    private fun logJacoco(coverages: Collection<IClassCoverage>) {
        val totalCoveredLines = coverages.stream().mapToInt { coverage: IClassCoverage ->
            coverage.lineCounter.coveredCount
        }.sum()
        val totalLines = coverages.stream().mapToInt { coverage: IClassCoverage ->
            coverage.lineCounter.totalCount
        }.sum()
        val totalCoveredInstructions = coverages.stream().mapToInt { coverage: IClassCoverage ->
            coverage.instructionCounter.coveredCount
        }.sum()
        val totalInstructions = coverages.stream().mapToInt { coverage: IClassCoverage ->
            coverage.instructionCounter.totalCount
        }.sum()
        val totalCoveredBranches = coverages.stream().mapToInt { coverage: IClassCoverage ->
            coverage.branchCounter.coveredCount
        }.sum()
        val totalBranches = coverages.stream().mapToInt { coverage: IClassCoverage ->
            coverage.branchCounter.totalCount
        }.sum()
        logger.info("Lines: $totalCoveredLines/$totalLines | Branches: $totalCoveredBranches/$totalBranches | Instructions: $totalCoveredInstructions/$totalInstructions")
    }

    data class CoverageLineByLine(
        val fullyCoveredLines: MutableList<Int>,
        val partiallyCoveredLines: MutableList<Int>,
        val notCoveredLines: MutableList<Int>,
    )

    private fun getCoverageLineByLine(coverages: Collection<IClassCoverage>): CoverageLineByLine {
        val fullyCoveredLines: MutableList<Int> = ArrayList()
        val partiallyCoveredLines: MutableList<Int> = ArrayList()
        val notCoveredLines: MutableList<Int> = ArrayList()
        for (coverage in coverages) {
            for (method in coverage.methods) {
                for (line in method.firstLine..method.lastLine) {
                    val totalBranches = method.getLine(line).branchCounter.totalCount
                    val missedBranches = method.getLine(line).branchCounter.missedCount
                    val lineTouched =
                        method.getLine(line).instructionCounter.totalCount == 0 || method.getLine(line).instructionCounter.coveredCount > 0
                    val fullCoverage = lineTouched && missedBranches == 0
                    val partialCoverage = lineTouched && missedBranches > 0 && totalBranches > 0
                    if (fullCoverage) {
                        fullyCoveredLines.add(line)
                    } else if (partialCoverage) {
                        partiallyCoveredLines.add(line)
                    } else {
                        notCoveredLines.add(line)
                    }
                }
            }
        }
        return CoverageLineByLine(fullyCoveredLines, partiallyCoveredLines, notCoveredLines)
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
