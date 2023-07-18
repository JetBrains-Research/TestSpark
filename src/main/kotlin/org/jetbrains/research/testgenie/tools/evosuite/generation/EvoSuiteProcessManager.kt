package org.jetbrains.research.testgenie.tools.evosuite.generation

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.util.concurrency.AppExecutorUtil
import org.jetbrains.research.testgenie.TestGenieBundle
import org.jetbrains.research.testgenie.data.CodeType
import org.jetbrains.research.testgenie.data.CodeTypeAndAdditionData
import org.jetbrains.research.testgenie.editor.Workspace
import org.jetbrains.research.testgenie.services.SettingsApplicationService
import org.jetbrains.research.testgenie.services.SettingsProjectService
import org.jetbrains.research.testgenie.tools.cancelPendingResult
import org.jetbrains.research.testgenie.tools.evosuite.SettingsArguments
import org.jetbrains.research.testgenie.tools.evosuite.error.EvoSuiteErrorManager
import org.jetbrains.research.testgenie.tools.getBuildPath
import org.jetbrains.research.testgenie.tools.getKey
import org.jetbrains.research.testgenie.tools.template.generation.ProcessManager
import java.io.File
import java.nio.charset.Charset
import java.util.regex.Pattern

class EvoSuiteProcessManager(
    private val project: Project,
    private val projectPath: String,
) : ProcessManager {
    private val evoSuiteProcessTimeout: Long = 12000000 // TODO: Source from config
    private val evosuiteVersion = "1.0.5" // TODO: Figure out a better way to source this

    private val sep = File.separatorChar
    private val pluginsPath = com.intellij.openapi.application.PathManager.getPluginsPath()
    private var evoSuitePath = "$pluginsPath${sep}TestGenie${sep}lib${sep}evosuite-$evosuiteVersion.jar"

    private val settingsApplicationState = SettingsApplicationService.getInstance().state
    private val settingsProjectState = project.service<SettingsProjectService>().state

    private val evoSuiteErrorManager: EvoSuiteErrorManager = EvoSuiteErrorManager()

    /**
     * Executes EvoSuite.
     *
     * @param indicator the progress indicator
     */
    override fun runTestGenerator(
        indicator: ProgressIndicator,
        codeType: CodeTypeAndAdditionData,
        projectClassPath: String,
        resultPath: String,
        serializeResultPath: String,
        packageName: String,
        cutModule: Module,
        classFQN: String,
        fileUrl: String,
        testResultName: String,
        baseDir: String,
        log: Logger,
        modificationStamp: Long,
    ) {
        try {
            // get command
            val command = when (codeType.type!!) {
                CodeType.CLASS -> SettingsArguments(projectClassPath, projectPath, serializeResultPath, classFQN, baseDir).build()
                CodeType.METHOD -> {
                    project.service<Workspace>().key = getKey(fileUrl, "$classFQN#${codeType.objectDescription}", modificationStamp, testResultName, projectClassPath)
                    SettingsArguments(projectClassPath, projectPath, serializeResultPath, classFQN, baseDir).forMethod(codeType.objectDescription).build()
                }

                CodeType.LINE -> SettingsArguments(projectClassPath, projectPath, serializeResultPath, classFQN, baseDir).forLine(codeType.objectIndex).build(true)
            }

            if (!settingsApplicationState?.seed.isNullOrBlank()) command.add("-seed=${settingsApplicationState?.seed}")
            if (!settingsApplicationState?.configurationId.isNullOrBlank()) command.add("-Dconfiguration_id=${settingsApplicationState?.configurationId}")

            // update build path
            var buildPath = projectClassPath
            if (settingsProjectState.buildPath.isEmpty()) {
                // User did not set own path
                buildPath = getBuildPath(project)
            }
            command[command.indexOf(projectClassPath)] = buildPath
            log.info("Generating tests for project $projectPath with classpath $buildPath inside the project")

            // construct command
            val cmd = ArrayList<String>()
            cmd.add(settingsApplicationState!!.javaPath)
            cmd.add("-Djdk.attach.allowAttachSelf=true")
            cmd.add("-jar")
            cmd.add(evoSuitePath)
            cmd.addAll(command)

            val cmdString = cmd.fold(String()) { acc, e -> acc.plus(e).plus(" ") }
            log.info("Starting EvoSuite with arguments: $cmdString")

            indicator.isIndeterminate = false
            indicator.text = TestGenieBundle.message("searchMessage")
            val evoSuiteProcess = GeneralCommandLine(cmd)
            evoSuiteProcess.charset = Charset.forName("UTF-8")
            evoSuiteProcess.setWorkDirectory(projectPath)
            val handler = OSProcessHandler(evoSuiteProcess)

            // attach process listener for output
            handler.addProcessListener(object : ProcessAdapter() {
                override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                    if (indicator.isCanceled) {
                        log.info("Cancelling search")

                        cancelPendingResult(project, testResultName)

                        handler.destroyProcess()
                    }

                    val text = event.text

                    evoSuiteErrorManager.addLineToEvoSuiteOutput(text)

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

            // evosuite errors check
            if (!evoSuiteErrorManager.isProcessCorrect(handler, project, evoSuiteProcessTimeout)) return

            // start result watcher
            AppExecutorUtil.getAppScheduledExecutorService()
                .execute(ResultWatcher(project, testResultName, fileUrl, classFQN))
        } catch (e: Exception) {
            evoSuiteErrorManager.errorProcess(TestGenieBundle.message("evosuiteErrorMessage").format(e.message), project)
            e.printStackTrace()
        }
    }
}
