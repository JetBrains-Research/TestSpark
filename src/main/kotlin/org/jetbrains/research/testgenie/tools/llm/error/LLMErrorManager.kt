package org.jetbrains.research.testgenie.tools.llm.error

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.jetbrains.research.testgenie.TestGenieBundle
import org.jetbrains.research.testgenie.services.ErrorService
import org.jetbrains.research.testgenie.tools.template.error.ErrorManager

class LLMErrorManager : ErrorManager {
    fun createRequestErrorMessage(code: Int): String = TestGenieBundle.message("requestError") + " " + code.toString()

    override fun errorProcess(message: String, project: Project) {
        if (project.service<ErrorService>().errorOccurred()) {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("LLM Execution Error")
                .createNotification(
                    TestGenieBundle.message("llmErrorTitle"),
                    message,
                    NotificationType.ERROR,
                )
                .notify(project)
        }
    }

    override fun warningProcess(message: String, project: Project) {
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
