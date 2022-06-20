package nl.tudelft.ewi.se.ciselab.testgenie.evosuite

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.util.concurrency.AppExecutorUtil
import nl.tudelft.ewi.se.ciselab.testgenie.TestGenieBundle
import nl.tudelft.ewi.se.ciselab.testgenie.Util
import nl.tudelft.ewi.se.ciselab.testgenie.editor.Workspace
import nl.tudelft.ewi.se.ciselab.testgenie.services.RunnerService
import nl.tudelft.ewi.se.ciselab.testgenie.services.SettingsApplicationService
import nl.tudelft.ewi.se.ciselab.testgenie.services.SettingsProjectService
import nl.tudelft.ewi.se.ciselab.testgenie.services.StaticInvalidationService
import nl.tudelft.ewi.se.ciselab.testgenie.services.TestCaseCachingService
import nl.tudelft.ewi.se.ciselab.testgenie.services.TestCaseDisplayService
import org.evosuite.result.TestGenerationResultImpl
import org.evosuite.utils.CompactReport
import org.evosuite.utils.CompactTestCase
import java.io.File
import java.nio.charset.Charset
import java.util.UUID
import java.util.regex.Pattern

/**
 * A utility class that runs evosuite as a separate process in its various
 * modes of operation.
 *
 * @param projectPath The root of the project we're testing, this sets the working dir of the evosuite process
 * @param projectClassPath Path to the class path containing the compiled classes. This will change according to
 * build system (e.g. Maven target/classes or Gradle build/classes)
 * @param classFQN Fully qualified name of the class under test
 */
class Pipeline(
    private val project: Project,
    private val projectPath: String,
    private val projectClassPath: String,
    private val classFQN: String,
    private val fileUrl: String,
    private val modTs: Long
) {
    private val log = Logger.getInstance(this::class.java)

    private val evoSuiteProcessTimeout: Long = 12000000 // TODO: Source from config
    private val evosuiteVersion = "1.0.4" // TODO: Figure out a better way to source this

    private val sep = File.separatorChar
    private val pluginsPath = System.getProperty("idea.plugins.path")
    private var evoSuitePath = "$pluginsPath${sep}TestGenie${sep}lib${sep}evosuite-$evosuiteVersion.jar"

    private val id = UUID.randomUUID().toString()
    private val testResultDirectory = "${FileUtilRt.getTempDirectory()}${sep}testGenieResults$sep"
    private val testResultName = "test_gen_result_$id"

    private var key = Workspace.TestJobInfo(fileUrl, classFQN, modTs, testResultName, projectClassPath)

    private val serializeResultPath = "\"$testResultDirectory$testResultName\""
    private var baseDir = "$testResultDirectory$testResultName-validation"

    private val settingsApplicationState = SettingsApplicationService.getInstance().state
    private val settingsProjectState = project.service<SettingsProjectService>().state

    private var command = mutableListOf<String>()
    private var cacheFromLine: Int? = null
    private var cacheToLine: Int? = null

    private var skipCache: Boolean = false

    init {
        Util.makeTmp()
        Util.makeDir(baseDir)
    }

    /**
     * Sets up evosuite to run for a target class. This is the simplest configuration.
     */
    fun forClass(): Pipeline {
        command = SettingsArguments(projectClassPath, projectPath, serializeResultPath, classFQN, baseDir).build()
        return this
    }

    /**
     * Sets up evosuite to run for a target method of the target class. This attaches a method descriptor argument
     * to the evosuite process.
     *
     * @param methodDescriptor The method descriptor of the method under test
     */
    fun forMethod(methodDescriptor: String): Pipeline {
        command =
            SettingsArguments(projectClassPath, projectPath, serializeResultPath, classFQN, baseDir).forMethod(
                methodDescriptor
            ).build()

        // attach method desc. to target unit key
        key = Workspace.TestJobInfo(fileUrl, "$classFQN#$methodDescriptor", modTs, testResultName, projectClassPath)

        return this
    }

    /**
     * Sets up evosuite to run for a target line of the target class. This attaches the selected line argument
     * to the evosuite process.
     */
    fun forLine(selectedLine: Int): Pipeline {
        command = SettingsArguments(projectClassPath, projectPath, serializeResultPath, classFQN, baseDir).forLine(
            selectedLine
        )
            .build()

        return this
    }

    /**
     * Configures lines for the cache (0-indexed)
     */
    fun withCacheLines(fromLine: Int, toLine: Int): Pipeline {
        this.cacheFromLine = fromLine + 1
        this.cacheToLine = toLine + 1
        return this
    }

    /**
     * Method to invalidate the cache.
     *
     * @param linesToInvalidate set of lines to invalidate
     */
    fun invalidateCache(linesToInvalidate: Set<Int>): Pipeline {
        val staticInvalidator = project.service<StaticInvalidationService>()
        staticInvalidator.invalidateCacheLines(fileUrl, linesToInvalidate)
        log.info("Going to invalidate $linesToInvalidate lines")
        return this
    }

    /**
     * Builds the project and launches the EvoSuite process,
     * tracking it from a separate thread.
     * Generate tests even if there is no cache miss.
     */
    fun withoutCache(): Pipeline {
        this.skipCache = true
        return this
    }

    /**
     * Builds the project and launches EvoSuite on a separate thread.
     *
     * @return the path to which results will be (eventually) saved
     */
    fun runTestGeneration(): String {
        log.info("Starting build and EvoSuite task")
        log.info("EvoSuite results will be saved to $serializeResultPath")

        val workspace = project.service<Workspace>()
        workspace.addPendingResult(testResultName, key)
        val projectBuilder = ProjectBuilder(project)

        ProgressManager.getInstance()
            .run(object : Task.Backgroundable(project, TestGenieBundle.message("evosuiteTestGenerationMessage")) {
                override fun run(indicator: ProgressIndicator) {
                    try {
                        if (!skipCache) {
                            // Check cache
                            val hasCachedTests = tryShowCachedTestCases()
                            if (hasCachedTests) {
                                log.info("Found cached tests")
                                indicator.stop()
                                return
                            }
                        }

                        if (indicator.isCanceled) {
                            indicator.stop()
                            return
                        }

                        projectBuilder.runBuild(indicator)

                        runEvoSuite(indicator)
                        indicator.stop()
                    } catch (e: Exception) {
                        evosuiteError(TestGenieBundle.message("evosuiteErrorMessage").format(e.message))
                        e.printStackTrace()
                    } finally {
                        // Revert to previous state
                        val runnerService = project.service<RunnerService>()
                        runnerService.isRunning = false
                        val testCaseDisplayService = project.service<TestCaseDisplayService>()
                        testCaseDisplayService.validateButton.isEnabled = true
                    }
                }
            })
        val testCaseDisplayService = project.service<TestCaseDisplayService>()
        testCaseDisplayService.fileUrl = fileUrl
        testCaseDisplayService.toggleJacocoButton.isEnabled = false

        return testResultName
    }

    /**
     * Attempts to retrieve and display cached test cases.
     *
     * @return true if cached tests were found, false otherwise
     */
    private fun tryShowCachedTestCases(): Boolean {
        val cache = project.service<TestCaseCachingService>()
        val testCases = cache.retrieveFromCache(fileUrl, cacheFromLine!!, cacheToLine!!)

        if (testCases.isEmpty()) {
            // no suitable cached tests found
            return false
        }

        // retrieve the job of an arbitrary valid test case
        val testJobInfo = cache.getTestJobInfo(fileUrl, testCases[0].testCode)

        val workspace = project.service<Workspace>()
        ApplicationManager.getApplication().invokeLater {
            val report = CompactReport(TestGenerationResultImpl())
            val testMap = hashMapOf<String, CompactTestCase>()
            testCases.forEach {
                testMap[it.testName] = it
            }

            report.testCaseList = testMap
            report.allCoveredLines = testCases.map { it.coveredLines }.flatten().toSet()

            workspace.receiveGenerationResult(testResultName, report, this, testJobInfo)
        }

        return true
    }

    /**
     * Executes EvoSuite.
     *
     * @param indicator the progress indicator
     */
    private fun runEvoSuite(indicator: ProgressIndicator) {
        if (!settingsApplicationState?.seed.isNullOrBlank()) command.add("-seed=${settingsApplicationState?.seed}")
        if (!settingsApplicationState?.configurationId.isNullOrBlank()) command.add("-Dconfiguration_id=${settingsApplicationState?.configurationId}")

        // construct command
        val cmd = ArrayList<String>()
        cmd.add(settingsProjectState.javaPath)
        cmd.add("-jar")
        cmd.add(evoSuitePath)
        cmd.addAll(command)

        val cmdString = cmd.fold(String()) { acc, e -> acc.plus(e).plus(" ") }
        log.info("Starting EvoSuite with arguments: $cmdString")

        indicator.isIndeterminate = false
        indicator.text = TestGenieBundle.message("evosuiteSearchMessage")
        val evoSuiteProcess = GeneralCommandLine(cmd)
        evoSuiteProcess.charset = Charset.forName("UTF-8")
        evoSuiteProcess.setWorkDirectory(projectPath)
        val handler = OSProcessHandler(evoSuiteProcess)

        // attach process listener for output
        handler.addProcessListener(object : ProcessAdapter() {
            override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                if (indicator.isCanceled) {
                    log.info("Cancelling search")

                    val workspace = project.service<Workspace>()
                    workspace.cancelPendingResult(testResultName)

                    handler.destroyProcess()
                }

                val text = event.text

                val progressMatcher =
                    Pattern.compile("Progress:[>= ]*(\\d+(?:\\.\\d+)?)%").matcher(text)
                val coverageMatcher = Pattern.compile("Cov:[>= ]*(\\d+(?:\\.\\d+)?)%").matcher(text)

                log.info(text) // kept for debugging purposes

                val progress =
                    if (progressMatcher.find()) progressMatcher.group(1)?.toDouble()?.div(100)
                    else null
                val coverage =
                    if (coverageMatcher.find()) coverageMatcher.group(1)?.toDouble()?.div(100)
                    else null
                if (progress != null && coverage != null) {
                    indicator.fraction = if (progress >= coverage) progress else coverage
                } else if (progress != null) {
                    indicator.fraction = progress
                } else if (coverage != null) {
                    indicator.fraction = coverage
                }

                if (indicator.fraction == 1.0 && indicator.text != TestGenieBundle.message("evosuitePostProcessMessage")) {
                    indicator.text = TestGenieBundle.message("evosuitePostProcessMessage")
                }
            }
        })

        handler.startNotify()

        // treat this as a join handle
        if (!handler.waitFor(evoSuiteProcessTimeout)) {
            evosuiteError("EvoSuite process exceeded timeout - ${evoSuiteProcessTimeout}ms")
        }

        if (!indicator.isCanceled) {
            if (handler.exitCode == 0) {
                // if process wasn't cancelled, start result watcher
                AppExecutorUtil.getAppScheduledExecutorService()
                    .execute(ResultWatcher(project, testResultName, fileUrl))
            } else {
                evosuiteError("EvoSuite process exited with non-zero exit code - ${handler.exitCode}")
            }
        }
    }

    /**
     * Show an EvoSuite execution error balloon.
     *
     * @param msg the balloon content to display
     */
    private fun evosuiteError(msg: String, title: String = TestGenieBundle.message("evosuiteErrorTitle")) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("EvoSuite Execution Error")
            .createNotification(
                title,
                msg,
                NotificationType.ERROR
            )
            .notify(project)
    }
}
