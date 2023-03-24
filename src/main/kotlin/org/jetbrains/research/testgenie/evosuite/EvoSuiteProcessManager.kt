package org.jetbrains.research.testgenie.evosuite

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.CompilerModuleExtension
import com.intellij.openapi.util.Key
import com.intellij.util.concurrency.AppExecutorUtil
import org.jetbrains.research.testgenie.TestGenieBundle
import org.jetbrains.research.testgenie.editor.Workspace
import org.jetbrains.research.testgenie.services.SettingsApplicationService
import org.jetbrains.research.testgenie.services.SettingsProjectService
import java.io.File
import java.nio.charset.Charset
import java.util.regex.Pattern

class EvoSuiteProcessManager(
    private val project: Project,
    private val projectPath: String,
    private val projectClassPath: String,
    private val fileUrl: String,
) {
    private val evoSuiteProcessTimeout: Long = 12000000 // TODO: Source from config
    private val evosuiteVersion = "1.0.5" // TODO: Figure out a better way to source this

    private val sep = File.separatorChar
    private val pluginsPath = com.intellij.openapi.application.PathManager.getPluginsPath()
    private var evoSuitePath = "$pluginsPath${sep}TestGenie${sep}lib${sep}evosuite-$evosuiteVersion.jar"

    private val settingsApplicationState = SettingsApplicationService.getInstance().state
    private val settingsProjectState = project.service<SettingsProjectService>().state

    /**
     * Executes EvoSuite.
     *
     * @param indicator the progress indicator
     */
    fun runEvoSuite(
        indicator: ProgressIndicator,
        command: MutableList<String>,
        log: Logger,
        testResultName: String,
    ) {
        try {
            if (!settingsApplicationState?.seed.isNullOrBlank()) command.add("-seed=${settingsApplicationState?.seed}")
            if (!settingsApplicationState?.configurationId.isNullOrBlank()) command.add("-Dconfiguration_id=${settingsApplicationState?.configurationId}")

            // update build path
            var buildPath = projectClassPath
            if (settingsProjectState.buildPath.isEmpty()) {
                // User did not set own path
                buildPath = ""
                for (module in ModuleManager.getInstance(project).modules) {
                    val compilerOutputPath = CompilerModuleExtension.getInstance(module)?.compilerOutputPath
                    compilerOutputPath?.let { buildPath += compilerOutputPath.path.plus(":") }
                }
            }
            command[command.indexOf(projectClassPath)] = buildPath
            log.info("Generating tests for project $projectPath with classpath $buildPath inside the project")

            // construct command
            val cmd = ArrayList<String>()
            cmd.add(settingsProjectState.javaPath)
            cmd.add("-Djdk.attach.allowAttachSelf=true")
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
            val errorsList = mutableListOf<EvosuiteError>()

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

                    // Unknown class error
                    errorsList.add(
                        EvosuiteError(
                            text.contains("Unknown class"),
                            "unknown class, be sure its compilation path is correct"
                        )
                    )

                    val progressMatcher =
                        Pattern.compile("Progress:[>= ]*(\\d+(?:\\.\\d+)?)%").matcher(text)
                    val coverageMatcher = Pattern.compile("Cov:[>= ]*(\\d+(?:\\.\\d+)?)%").matcher(text)

                    log.info(text) // kept for debugging purposes

                    val progress =
                        if (progressMatcher.find()) {
                            progressMatcher.group(1)?.toDouble()?.div(100)
                        } else {
                            null
                        }
                    val coverage =
                        if (coverageMatcher.find()) {
                            coverageMatcher.group(1)?.toDouble()?.div(100)
                        } else {
                            null
                        }

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

            if (indicator.isCanceled) return

            // Timeout error
            // TODO add timeout to message
            errorsList.add(EvosuiteError(!handler.waitFor(evoSuiteProcessTimeout), "exceeded timeout"))

            // Non-zero exit code error
            // TODO add code to message
            errorsList.add(EvosuiteError(handler.exitCode != 0, "exited with non-zero exit code"))

            // check all errors
            for (error in errorsList) {
                if (error.isFailed) {
                    evosuiteErrorDisplay(error.message)
                    return
                }
            }

            // start result watcher
            AppExecutorUtil.getAppScheduledExecutorService()
                .execute(ResultWatcher(project, testResultName, fileUrl))
        } catch (e: Exception) {
            evosuiteErrorDisplay(TestGenieBundle.message("evosuiteErrorMessage").format(e.message))
            e.printStackTrace()
        }
    }

    /**
     * Show an EvoSuite execution error balloon.
     *
     * @param msg the balloon content to display
     */
    private fun evosuiteErrorDisplay(msg: String, title: String = TestGenieBundle.message("evosuiteErrorTitle")) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("EvoSuite Execution Error")
            .createNotification(
                title,
                TestGenieBundle.message("evosuiteErrorCommon") + " " + msg,
                NotificationType.ERROR
            )
            .notify(project)
    }

    // evosuite errors data
    data class EvosuiteError(val isFailed: Boolean, val message: String)
}
