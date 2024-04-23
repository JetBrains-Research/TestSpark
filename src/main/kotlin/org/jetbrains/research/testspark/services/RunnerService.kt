package org.jetbrains.research.testspark.services

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.bundles.MessagesBundle

/**
 * Service used for the sole purpose to limit TestSpark to generate tests only once at a time.
 */
@Service(Service.Level.PROJECT)
class RunnerService(private val project: Project) {
    private var isRunning: Boolean = false

    /**
     * Method to show notification that test generation is already running.
     */
    private fun showGenerationRunningNotification() {
        val terminateButton: AnAction = object : AnAction("Terminate") {
            override fun actionPerformed(e: AnActionEvent) {
                project.service<ErrorService>().errorOccurred()
            }
        }

        val notification = NotificationGroupManager.getInstance()
            .getNotificationGroup("Execution Error")
            .createNotification(
                MessagesBundle.message("alreadyRunningNotificationTitle"),
                MessagesBundle.message("alreadyRunningTextNotificationText"),
                NotificationType.WARNING,
            )

        notification.addAction(terminateButton)

        notification.notify(project)
    }

    fun clear() {
        isRunning = false
    }

    /**
     * Check if generator is running.
     *
     * @return true if it is already running
     */
    fun isGeneratorRunning(): Boolean {
        if (isRunning) {
            showGenerationRunningNotification()
            return true
        }
        isRunning = true
        return false
    }
}
