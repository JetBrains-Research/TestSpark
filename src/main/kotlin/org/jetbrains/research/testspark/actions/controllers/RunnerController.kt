package org.jetbrains.research.testspark.actions.controllers

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.bundles.plugin.PluginMessagesBundle
import org.jetbrains.research.testspark.services.ErrorService

/**
 * Manager used for the sole purpose to limit TestSpark to generate tests only once at a time.
 */
class RunnerController {
    private var isRunning: Boolean = false

    /**
     * Method to show notification that test generation is already running.
     */
    private fun showGenerationRunningNotification(project: Project) {
        val terminateButton: AnAction = object : AnAction("Terminate") {
            override fun actionPerformed(e: AnActionEvent) {
                project.service<ErrorService>().errorOccurred()
            }
        }

        val notification = NotificationGroupManager.getInstance()
            .getNotificationGroup("Execution Error")
            .createNotification(
                PluginMessagesBundle.get("alreadyRunningNotificationTitle"),
                PluginMessagesBundle.get("alreadyRunningTextNotificationText"),
                NotificationType.WARNING,
            )

        notification.addAction(terminateButton)

        notification.notify(project)
    }

    fun finished() {
        isRunning = false
    }

    /**
     * Check if generator is running.
     *
     * @return true if it is already running
     */
    fun isGeneratorRunning(project: Project): Boolean {
        if (isRunning) {
            showGenerationRunningNotification(project)
            return true
        }
        isRunning = true
        return false
    }
}
