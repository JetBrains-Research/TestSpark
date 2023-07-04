package org.jetbrains.research.testgenie.tools.llm.error

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import org.jetbrains.research.testgenie.TestGenieBundle

class LLMErrorManager {
    fun display(message: String, project: Project) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("LLM Execution Error")
            .createNotification(
                TestGenieBundle.message("llmErrorTitle"),
                message,
                NotificationType.ERROR,
            )
            .notify(project)
    }

    fun displayWarning(message: String, project: Project) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("LLM Execution Error")
            .createNotification(
                TestGenieBundle.message("llmWarningTitle"),
                message,
                NotificationType.WARNING,
            )
            .notify(project)
    }
}
