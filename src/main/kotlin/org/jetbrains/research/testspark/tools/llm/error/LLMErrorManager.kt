package org.jetbrains.research.testspark.tools.llm.error

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.bundles.llm.LLMMessagesBundle
import org.jetbrains.research.testspark.bundles.plugin.PluginMessagesBundle
import org.jetbrains.research.testspark.core.monitor.ErrorMonitor
import org.jetbrains.research.testspark.tools.template.error.ErrorManager

/**
 * LLMErrorManager is a class that handles error and warning messages for LLM (Live Logic Monitor).
 *
 * It implements the ErrorManager interface and provides functions to create and process error and warning messages.
 *
 * Error messages can be created using the createRequestErrorMessage function, which takes an integer code and returns a formatted error message.
 * The errorProcess function can be used to process error messages, displaying them as notifications in the specified project.
 * The warningProcess function is used to process warning messages, displaying them as notifications in the specified project.
 *
 * This class is part of the LLMErrorManager module.
 */
class LLMErrorManager : ErrorManager {
    /**
     * Represents a private logger instance for logging messages within the class.
     *
     * @property log The instance of the logger.
     */
    private val log = Logger.getInstance(this::class.java)

    /**
     * Creates an error message for a request.
     *
     * @param code The error code associated with the request.
     * @return The error message for the request.
     */
    fun createRequestErrorMessage(code: Int): String = LLMMessagesBundle.get("requestError") + " " + code.toString()

    /**
     * Processes an error message and displays a notification if an error has occurred.
     *
     * @param message The error message to be displayed in the notification.
     * @param project The project in which the error occurred.
     */
    override fun errorProcess(message: String, project: Project, errorMonitor: ErrorMonitor) {
        if (errorMonitor.notifyErrorOccurrence()) {
            log.warn("Error in Test Generation: $message")
            NotificationGroupManager.getInstance()
                .getNotificationGroup("LLM Execution Error")
                .createNotification(
                    PluginMessagesBundle.get("llmErrorTitle"),
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
        log.warn("Error in Test Generation: $message")
        NotificationGroupManager.getInstance()
            .getNotificationGroup("LLM Execution Error")
            .createNotification(
                PluginMessagesBundle.get("llmWarningTitle"),
                message,
                NotificationType.WARNING,
            )
            .notify(project)
    }
}
