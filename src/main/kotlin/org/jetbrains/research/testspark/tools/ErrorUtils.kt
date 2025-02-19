package org.jetbrains.research.testspark.tools

import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.bundles.plugin.PluginMessagesBundle
import org.jetbrains.research.testspark.core.exception.TestCompilerException
import org.jetbrains.research.testspark.core.exception.LlmException
import org.jetbrains.research.testspark.core.exception.TestSparkException
import org.jetbrains.research.testspark.tools.kex.error.llmDisplayMessage

fun Project.errorProcess(exception: TestSparkException) {
    val log = Logger.getInstance(this::class.java)
    log.warn("Error in Test Generation: ${exception.displayMessage}")

    NotificationGroupManager.getInstance()
        .getNotificationGroupFor(exception)
        .createNotification(
            getNotificationTitleFor(exception),
            exception.displayMessage,
            NotificationType.ERROR,
        )
        .notify(this)
}



val TestSparkException.displayMessage: String
    get() = when (this) {
        is LlmException -> llmDisplayMessage
        is TestCompilerException -> generalDisplayMessage
        else -> PluginMessagesBundle.get("unknownErrorOccurredMessage")
    }

private fun NotificationGroupManager.getNotificationGroupFor(
    exception: TestSparkException
): NotificationGroup = when (exception) {
    is LlmException -> getNotificationGroup("LLM Execution Error")
    is TestCompilerException -> getNotificationGroup("Generation Error")
    else -> getNotificationGroup("Execution Error")
}

private fun getNotificationTitleFor(
    exception: TestSparkException
): String = when (exception) {
    is LlmException -> PluginMessagesBundle.get("kexErrorTitle")
    is TestCompilerException -> PluginMessagesBundle.get("buildErrorTitle")
    else -> PluginMessagesBundle.get("unknownErrorOccurredMessage")
}

