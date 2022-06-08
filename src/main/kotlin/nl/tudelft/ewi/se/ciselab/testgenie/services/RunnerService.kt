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
                "No new tests will be generated until this test generation run is complete.",
                NotificationType.WARNING
            )
            .notify(project)
    }

    /**
     * Method to verify whether EvoSuite is running or not.
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
