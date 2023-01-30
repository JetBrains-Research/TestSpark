package nl.tudelft.ewi.se.ciselab.testgenie.evosuite

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
import nl.tudelft.ewi.se.ciselab.testgenie.TestGenieBundle
import nl.tudelft.ewi.se.ciselab.testgenie.services.SettingsProjectService
import java.util.concurrent.CountDownLatch

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

    fun runBuild(indicator: ProgressIndicator) {
        val handle = CountDownLatch(1)
        log.info("Starting build!")

        try {
            indicator.isIndeterminate = true
            indicator.text = TestGenieBundle.message("evosuiteBuildMessage")
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
                buildError("Build process exceeded timeout - ${builderTimeout}ms")
            }

            if (indicator.isCanceled) {
                return
            }

            val exitCode = handler.exitCode

            if (exitCode != 0) {
                buildError("exit code $exitCode", "Build failed")
            }
            handle.countDown()
        } catch (e: Exception) {
            (TestGenieBundle.message("evosuiteErrorMessage").format(e.message))
            e.printStackTrace()
        }
        log.info("Build finished!")
    }

    private fun buildError(msg: String, title: String = TestGenieBundle.message("evosuiteErrorTitle")) {
        NotificationGroupManager.getInstance().getNotificationGroup("Build Execution Error").createNotification(
            title, msg, NotificationType.ERROR
        ).notify(project)
    }
}
