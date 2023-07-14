package org.jetbrains.research.testgenie.services

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import org.jetbrains.research.testgenie.TestGenieBundle

/**
 * Service used for the sole purpose to limit TestGenie to generate tests only once at a time.
 */
class RunnerService(private val project: Project) {
    var isRunning: Boolean = false

    /**
     * Method to show notification that test generation is already running.
     */
    private fun showGenerationRunningNotification() {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Execution Error")
            .createNotification(
                TestGenieBundle.message("alreadyRunningNotificationTitle"),
                TestGenieBundle.message("alreadyRunningTextNotificationText"),
                NotificationType.WARNING,
            )
            .notify(project)
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
