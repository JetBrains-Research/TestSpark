package org.jetbrains.research.testspark.tools.error

import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.bundles.plugin.PluginMessagesBundle
import org.jetbrains.research.testspark.core.data.TestSparkModule
import org.jetbrains.research.testspark.core.error.EvoSuiteError
import org.jetbrains.research.testspark.core.error.HttpError
import org.jetbrains.research.testspark.core.error.KexError
import org.jetbrains.research.testspark.core.error.LlmError
import org.jetbrains.research.testspark.core.error.TestSparkError
import org.jetbrains.research.testspark.tools.evosuite.error.evoSuiteErrorDisplayMessage
import org.jetbrains.research.testspark.tools.kex.error.kexErrorDisplayMessage
import org.jetbrains.research.testspark.tools.llm.error.httpErrorDisplayMessage
import org.jetbrains.research.testspark.tools.llm.error.llmErrorDisplayMessage

fun Project.createNotification(
    error: TestSparkError, notificationType: NotificationType
) = showNotification(
    module = error.module,
    message = error.displayMessage ?: PluginMessagesBundle.get("unknownErrorMessage"),
    notificationType = notificationType,
    logMessage = error.toString(),
)

val TestSparkError.displayMessage: String?
    get() = when (this) {
        is LlmError -> llmErrorDisplayMessage
        is HttpError -> httpErrorDisplayMessage
        is EvoSuiteError -> evoSuiteErrorDisplayMessage
        is KexError -> kexErrorDisplayMessage
        else -> null
    }

private fun Project.showNotification(
    module: TestSparkModule,
    message: String,
    notificationType: NotificationType,
    logMessage: String
) {
    val log = Logger.getInstance(this::class.java)
    if (notificationType == NotificationType.ERROR) {
        log.error("Error in $module module: $logMessage")
    } else {
        log.warn("Error in $module module: $logMessage")
    }

    NotificationGroupManager.getInstance()
        .getNotificationGroupFor(module)
        .createNotification(
            getNotificationTitleFor(module),
            message,
            NotificationType.ERROR,
        )
        .notify(this)
}

private fun NotificationGroupManager.getNotificationGroupFor(
    module: TestSparkModule
): NotificationGroup = when (module) {
    is TestSparkModule.LLM -> getNotificationGroup("LLM Execution Error")
    is TestSparkModule.EvoSuite -> getNotificationGroup("EvoSuite Execution Error")
    is TestSparkModule.Kex -> getNotificationGroup("Kex Execution Error")
    is TestSparkModule.UI -> getNotificationGroup("UserInterface")
    is TestSparkModule.ProjectBuilder -> getNotificationGroup("Build Execution Error")
    is TestSparkModule.Common -> getNotificationGroup("Execution Error")
}

private fun getNotificationTitleFor(
    module: TestSparkModule
): String = when (module) {
    is TestSparkModule.LLM -> PluginMessagesBundle.get("llmErrorTitle")
    is TestSparkModule.EvoSuite -> PluginMessagesBundle.get("evosuiteErrorTitle")
    is TestSparkModule.Kex -> PluginMessagesBundle.get("kexErrorTitle")
    is TestSparkModule.UI -> PluginMessagesBundle.get("generationWindowWarningTitle")
    is TestSparkModule.ProjectBuilder -> PluginMessagesBundle.get("buildErrorTitle")
    is TestSparkModule.Common -> PluginMessagesBundle.get("commonErrorTitle")
}
