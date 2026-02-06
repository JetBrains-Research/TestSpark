package org.jetbrains.research.testspark.actions.controllers

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.bundles.plugin.PluginMessagesBundle
import org.jetbrains.research.testspark.core.progress.CustomProgressIndicator

/**
 * Manager used for monitoring the unit test generation process.
 * It also limits TestSpark to generate tests only once at a time.
 */
class IndicatorController {
    var activeIndicators: MutableList<CustomProgressIndicator?> = mutableListOf()

    /**
     * Method to show notification that test generation is already running.
     */
    private fun showGenerationRunningNotification(project: Project) {
        val notification =
            NotificationGroupManager
                .getInstance()
                .getNotificationGroup("Execution Error")
                .createNotification(
                    PluginMessagesBundle.get("alreadyRunningNotificationTitle"),
                    PluginMessagesBundle.get("alreadyRunningTextNotificationText"),
                    NotificationType.WARNING,
                )

        val terminateButton: AnAction =
            object : AnAction(PluginMessagesBundle.get("terminateButtonText")) {
                override fun actionPerformed(e: AnActionEvent) {
                    for (indicator in activeIndicators) {
                        indicator?.cancel()
                    }
                    notification.expire()
                }
            }

        notification.addAction(terminateButton)

        notification.notify(project)
    }

    fun finished() {
        for (indicator in activeIndicators) {
            if (indicator != null &&
                indicator.isRunning()
            ) {
                indicator.stop()
            }
        }
        activeIndicators.clear()
    }

    /**
     * Check if generator is running.
     *
     * @return true if it is already running
     */
    fun isGeneratorRunning(project: Project): Boolean {
        // If indicator is null, we have never initiated an indicator before and there is no running test generation
        for (indicator in activeIndicators) {
            if (indicator != null && indicator.isRunning()) {
                showGenerationRunningNotification(project)
                return true
            }
        }
        return false
    }
}
