package org.jetbrains.research.testspark.error

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.bundles.TestSparkBundle
import org.jetbrains.research.testspark.services.ErrorService
import org.jetbrains.research.testspark.tools.template.error.ErrorManager

class TestExecutionErrorManager : ErrorManager {

    /**
     * Processes an error message and displays a notification if an error has occurred.
     *
     * @param message The error message to be displayed in the notification.
     * @param project The project in which the error occurred.
     */
    override fun errorProcess(message: String, project: Project) {
        if (project.service<ErrorService>().errorOccurred()) {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("Test Execution Error")
                .createNotification(
                    TestSparkBundle.message("testExeErrorTitle"),
                    message,
                    NotificationType.ERROR,
                )
                .notify(project)
        }
    }

    /**
     * Displays a warning notification with the given message in the specified project.
     *
     * @param message The content of the warning notification.
     * @param project The project in which to display the notification.
     */
    override fun warningProcess(message: String, project: Project) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Test Execution Error")
            .createNotification(
                TestSparkBundle.message("testExeWarningTitle"),
                message,
                NotificationType.WARNING,
            )
            .notify(project)
    }

}