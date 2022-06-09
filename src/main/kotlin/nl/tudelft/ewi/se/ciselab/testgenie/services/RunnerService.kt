package nl.tudelft.ewi.se.ciselab.testgenie.services

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

/**
 * Service used for the sole purpose to limit TestGenie to generate tests only once at a time.
 */
class RunnerService(private val project: Project) {
    var isRunning: Boolean = false

    /**
     * Method to show notification that test generation is already running.
     */
    private fun showNotification() {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("EvoSuite Execution Error")
            .createNotification(
                "EvoSuite is already running",
                "You can generate more tests after the current test generation is complete.",
                NotificationType.WARNING
            )
            .notify(project)
    }

    /**
     * Method to verify whether EvoSuite is running or not.
     *
     * @return true if EvoSuite is currently running
     */
    fun verifyIsRunning(): Boolean {
        if (isRunning) {
            showNotification()
            return true
        }
        isRunning = true
        return false
    }
}
