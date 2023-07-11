package org.jetbrains.research.testgenie.tools.llm.error

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.jetbrains.research.testgenie.TestGenieBundle
import org.jetbrains.research.testgenie.editor.Workspace

class LLMErrorManager {
    fun createRequestErrorMessage(code: Int): String = TestGenieBundle.message("requestError") + " " + code.toString()

    fun errorProcess(message: String, project: Project) {
        project.service<Workspace>().errorOccurred()
        NotificationGroupManager.getInstance()
            .getNotificationGroup("LLM Execution Error")
            .createNotification(
                TestGenieBundle.message("llmErrorTitle"),
                message,
                NotificationType.ERROR,
            )
            .notify(project)
    }

    fun warningProcess(message: String, project: Project) {
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
