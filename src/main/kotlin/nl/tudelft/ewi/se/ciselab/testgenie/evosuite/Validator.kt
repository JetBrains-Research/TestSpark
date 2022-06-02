package nl.tudelft.ewi.se.ciselab.testgenie.evosuite

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.io.FileUtilRt
import nl.tudelft.ewi.se.ciselab.testgenie.TestGenieBundle
import nl.tudelft.ewi.se.ciselab.testgenie.editor.Workspace
import nl.tudelft.ewi.se.ciselab.testgenie.services.TestGenieSettingsService
import java.io.File
import java.nio.charset.Charset
import javax.tools.JavaCompiler
import javax.tools.ToolProvider

class Validator(
    private val project: Project,
    private val testJob: Workspace.TestJob
) {
    private val logger: Logger = Logger.getInstance(this.javaClass)
    private val settingsState = TestGenieSettingsService.getInstance().state
    private val junitTimeout: Long = 12000000 // TODO: Source from config

    fun validateSuite() {
        val sep = File.separatorChar
        val pathSep = File.pathSeparator

        val jobName = testJob.info.jobId
        val report = testJob.report

        logger.info("Validating test suite $jobName")

        val targetFqn = "${testJob.info.targetUnit}_ESTest"

        val targetProjectCP = testJob.info.targetClassPath

        val pluginsPath = System.getProperty("idea.plugins.path")

        val junitPath = "$pluginsPath/TestGenie/lib/junit-4.12.jar"
        val standaloneRuntimePath = "$pluginsPath/TestGenie/lib/evosuite-standalone-runtime-1.2.1-SNAPSHOT.jar"
        val hamcrestPath = "$pluginsPath/TestGenie/lib/hamcrest-core-1.3.jar"

        val testValidationDirectory =
            "${FileUtilRt.getTempDirectory()}${sep}testGenieResults$sep$jobName-validation${sep}evosuite-tests"
        val validationDir = File(testValidationDirectory)

        // TODO: Implement classpath builder
        val dependenciesClasspath = "${junitPath}$pathSep$standaloneRuntimePath$pathSep$hamcrestPath"
        val classpath = "${targetProjectCP}${pathSep}$dependenciesClasspath${pathSep}$testValidationDirectory"

        if (!validationDir.exists()) {
            logger.error("Validation dir does not exist! - $testValidationDirectory")
            return
        }

        val testsPath = "$testValidationDirectory$sep${targetFqn.replace('.', sep)}.java"
        val testsFile = File(testsPath)

        val scaffoldPath = "$testValidationDirectory$sep${targetFqn.replace('.', sep)}_scaffolding.java"
        val scaffoldFile = File(scaffoldPath)

        val compilationFiles = listOf(testsFile, scaffoldFile)

        logger.info("Compiling tests...")
        compileTests(classpath, compilationFiles)

        logger.info("Executing tests...")
        ProgressManager.getInstance()
            .run(object : Task.Backgroundable(project, TestGenieBundle.message("evosuiteTestValidationRunMessage")) {
                override fun run(indicator: ProgressIndicator) {
                    try {
                        runTests(indicator, classpath, targetFqn)
                        indicator.stop()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
    }

    /**
     * Compiles the test files with the provided classpath
     *
     * @param indicator the progress indicator
     */
    private fun compileTests(classpath: String, files: List<File>) {
        logger.info("Compiling with classpath $classpath")

        val optionList: List<String> = listOf("-classpath", classpath)

        val compiler: JavaCompiler = ToolProvider.getSystemJavaCompiler()

        val fileManager = compiler.getStandardFileManager(null, null, null)

        val compilationUnits = fileManager.getJavaFileObjectsFromFiles(files)

        val task = compiler.getTask(
            null, fileManager, null,
            optionList, null, compilationUnits
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
    private fun runTests(indicator: ProgressIndicator, classpath: String, testFqn: String) {

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

        indicator.isIndeterminate = false
        val junitProcess = GeneralCommandLine(cmd)
        junitProcess.charset = Charset.forName("UTF-8")
        val handler = OSProcessHandler(junitProcess)

        // attach process listener for output
        handler.addProcessListener(object : ProcessAdapter() {
            override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                if (indicator.isCanceled) {
                    logger.info("Cancelling tests")
                    handler.destroyProcess()
                }
                val text = event.text
                logger.info(text) // kept for debugging purposes
            }
        })

        handler.startNotify()

        // treat this as a join handle
        handler.waitFor(junitTimeout)
    }
}
