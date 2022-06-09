package nl.tudelft.ewi.se.ciselab.testgenie.evosuite.validation

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.CapturingProcessAdapter
import com.intellij.execution.process.OSProcessHandler
import com.intellij.openapi.application.ApplicationManager
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
import nl.tudelft.ewi.se.ciselab.testgenie.editor.Workspace
import nl.tudelft.ewi.se.ciselab.testgenie.services.TestGenieSettingsService
import java.io.File
import java.io.FileWriter
import java.nio.charset.Charset
import java.util.regex.Pattern
import javax.tools.JavaCompiler
import javax.tools.ToolProvider

class Validator(
    private val project: Project,
    private val testJob: Workspace.TestJob,
    private val edits: HashMap<String, String> // test name, test code
) {
    private val logger: Logger = Logger.getInstance(this.javaClass)
    private val settingsState = TestGenieSettingsService.getInstance().state
    private val junitTimeout: Long = 12000000 // TODO: Source from config

    fun validateSuite() {
        val sep = File.separatorChar
        val pathSep = File.pathSeparator

        val jobName = testJob.info.jobId

        logger.info("Validating test suite $jobName")

        val targetFqn = "${testJob.info.targetUnit}_ESTest"

        val targetProjectCP = testJob.info.targetClassPath

        val pluginsPath = System.getProperty("idea.plugins.path")

        val junitPath = "$pluginsPath/TestGenie/lib/junit-4.13.jar"
        val standaloneRuntimePath = "$pluginsPath/TestGenie/lib/standalone-runtime.jar"
        val hamcrestPath = "$pluginsPath/TestGenie/lib/hamcrest-core-1.3.jar"

        val testValidationRoot = "${FileUtilRt.getTempDirectory()}${sep}testGenieResults$sep$jobName-validation"
        val testValidationDirectory =
            "$testValidationRoot${sep}evosuite-tests"
        val validationDir = File(testValidationDirectory)

        // TODO: Implement classpath builder
        val dependenciesClasspath = "${junitPath}$pathSep$standaloneRuntimePath$pathSep$hamcrestPath"
        val classpath = "${targetProjectCP}${pathSep}$dependenciesClasspath${pathSep}$testValidationDirectory"

        if (!validationDir.exists()) {
            logger.error("Validation dir does not exist! - $testValidationDirectory")
            return
        }

        val compilationFiles = setupCompilationFiles(testValidationDirectory, targetFqn)

        logger.info("Compiling tests...")
        compileTests(classpath, compilationFiles)

        logger.info("Executing tests...")
        ProgressManager.getInstance()
            .run(object : Task.Backgroundable(project, TestGenieBundle.message("evosuiteTestValidationRunMessage")) {
                override fun run(indicator: ProgressIndicator) {
                    try {
                        runTests(indicator, classpath, targetFqn)
                        runTestsWithCoverage(indicator, classpath, targetFqn, testValidationRoot)
                        indicator.stop()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
    }

    private fun setupCompilationFiles(testValidationDirectory: String, targetFqn: String): List<File> {
        val sep = File.separatorChar

        val baseClassName = "$testValidationDirectory$sep${targetFqn.replace('.', sep)}"
        // flush test edits to file
        val testsPath = "$baseClassName.java"
        val testsFile = File(testsPath)

        val editor = TestCaseEditor(testsFile.readText(), edits)

        if (edits.size == 0) {
            logger.trace("No changes found, resetting files to old state")
            val testsFileWriter = FileWriter(testsPath, false)
            testsFileWriter.write(testJob.report.testSuiteCode)
            testsFileWriter.close()
            logger.trace("Flushed original tests to $testsPath")
        } else {
            val editedTests = editor.edit()
            val testsFileWriter = FileWriter(testsFile, false)
            testsFileWriter.write(editedTests)
            testsFileWriter.close()
            logger.trace("Flushed edited tests to $testsPath")
        }

        val testsCovPath = "${baseClassName}_Cov.java"
        val testsCov = File(testsCovPath)
        val testsCovWriter = FileWriter(testsCov, false)
        val testsNoScaffold = editor.editRemoveScaffold()
        testsCovWriter.write(testsNoScaffold)
        testsCovWriter.close()

        val scaffoldPath = "${baseClassName}_scaffolding.java"
        val scaffoldFile = File(scaffoldPath)

        return listOf(scaffoldFile, testsFile, testsCov)
    }

    /**
     * Compiles the test files with the provided classpath
     *
     * @param indicator the progress indicator
     */
    private fun compileTests(classpath: String, files: List<File>) {
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

        if (compiled) {
            logger.info("Compilation successful!")
        } else {
            logger.error("Compilation failed!")
            return
        }
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
            val content: Content = contentManager.factory.createContent(console.component, "Running tests", false)
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
        testValidationRoot: String
    ) {
        settingsState ?: return

        indicator.text = "Calculating coverage"

        val pluginsPath = System.getProperty("idea.plugins.path")
        val jacocoPath = "$pluginsPath/TestGenie/lib/jacocoagent.jar"
        // construct command
        val cmd = ArrayList<String>()
        cmd.add(settingsState.javaPath)
        cmd.add("-javaagent:$jacocoPath=destfile=$testValidationRoot/jacoco.exec")
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

        val output = cap.output.stdout

        // TOOD: jacoco result
//        val junitResult = parseJunitResult(output)

//        project.messageBus.syncPublisher(VALIDATION_RESULT_TOPIC).validationResult(junitResult)
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
