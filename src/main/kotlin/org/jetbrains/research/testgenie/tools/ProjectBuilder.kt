package org.jetbrains.research.testgenie.tools

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import org.jetbrains.research.testgenie.TestGenieBundle
import org.jetbrains.research.testgenie.services.SettingsProjectService
import java.util.concurrent.CountDownLatch
import com.intellij.util.concurrency.Semaphore
import com.intellij.task.ProjectTaskManager
import org.jetbrains.research.testgenie.editor.Workspace

/**
 * This class builds the project before running EvoSuite and before validating the tests.
 */
class ProjectBuilder(private val project: Project) {
    private val log = Logger.getInstance(this::class.java)

    private val builderTimeout: Long = 12000000 // TODO: Source from config

    private val projectPath: String = ProjectRootManager.getInstance(project).contentRoots.first().path
    private val settingsState = project.service<SettingsProjectService>().state

    init {
        ApplicationManager.getApplication().saveAll()
    }

    fun runBuild(indicator: ProgressIndicator): Boolean {
        val handle = CountDownLatch(1)
        log.info("Starting build!")
        var isSuccessful = true

        try {
            indicator.isIndeterminate = true
            indicator.text = TestGenieBundle.message("buildMessage")
            if (settingsState.buildCommand.isEmpty()) {
                // User did not put own command line
                val promise = ProjectTaskManager.getInstance(project).buildAllModules()
                val finished = Semaphore()
                finished.down()
                promise.onSuccess {
                    if (it.isAborted || it.hasErrors()) {
                        errorProcess("Build process error")
                        isSuccessful = false
                    }
                    finished.up()
                }
                promise.onError {
                    errorProcess("Build process error")
                    isSuccessful = false
                    finished.up()
                }
                finished.waitFor()
            } else {
                // User put own command line
                // Save all open editors
                val cmd = ArrayList<String>()

                val operatingSystem = System.getProperty("os.name")

                if (operatingSystem.lowercase().contains("windows")) {
                    cmd.add("cmd.exe")
                    cmd.add("/c")
                } else {
                    cmd.add("sh")
                    cmd.add("-c")
                }

                cmd.add(settingsState.buildCommand)

                val cmdString = cmd.fold(String()) { acc, e -> acc.plus(e).plus(" ") }
                log.info("Starting build process with arguments: $cmdString")

                val buildProcess = GeneralCommandLine(cmd)
                buildProcess.setWorkDirectory(projectPath)
                val handler = OSProcessHandler(buildProcess)
                handler.startNotify()

                if (!handler.waitFor(builderTimeout)) {
                    errorProcess("Build process exceeded timeout - ${builderTimeout}ms")
                    isSuccessful = false
                }

                if (indicator.isCanceled) {
                    return false
                }

                val exitCode = handler.exitCode

                if (exitCode != 0) {
                    errorProcess("exit code $exitCode", "Build failed")
                    isSuccessful = false
                }
                handle.countDown()
            }
        } catch (e: Exception) {
            errorProcess(TestGenieBundle.message("evosuiteErrorMessage").format(e.message))
            e.printStackTrace()
            isSuccessful = false
        }
        log.info("Build finished!")
        return isSuccessful
    }

    private fun errorProcess(msg: String, title: String = TestGenieBundle.message("buildErrorTitle")) {
        project.service<Workspace>().errorOccurred()
        NotificationGroupManager.getInstance().getNotificationGroup("Build Execution Error").createNotification(
            title,
            msg,
            NotificationType.ERROR,
        ).notify(project)
    }
}
