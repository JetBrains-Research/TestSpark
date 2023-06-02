package org.jetbrains.research.testgenie.tools.llm.error

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import org.jetbrains.research.testgenie.TestGenieBundle

class LLMErrorManager {

    fun displayMissingTokenNotification(project: Project) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("LLM Error")
            .createNotification(
                TestGenieBundle.message("missingToken"),
                NotificationType.WARNING
            )
            .notify(project)
    }

    companion object {
        fun displayEmptyTests(project: Project) {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("LLM Error")
                .createNotification(
                    TestGenieBundle.message("emptyResponse"),
                    NotificationType.ERROR
                )
                .notify(project)
        }
    }
}
