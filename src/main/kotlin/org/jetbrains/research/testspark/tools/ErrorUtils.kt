package org.jetbrains.research.testspark.tools

import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.bundles.plugin.PluginMessagesBundle
import org.jetbrains.research.testspark.core.exception.GeneralException
import org.jetbrains.research.testspark.core.exception.KexException
import org.jetbrains.research.testspark.core.exception.TestSparkException
import org.jetbrains.research.testspark.tools.kex.error.kexDisplayMessage

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
        is KexException -> kexDisplayMessage
        is GeneralException -> generalDisplayMessage
        else -> PluginMessagesBundle.get("unknownErrorOccurredMessage")
    }

private fun NotificationGroupManager.getNotificationGroupFor(
    exception: TestSparkException
): NotificationGroup = when (exception) {
    is KexException -> getNotificationGroup("Kex Execution Error")
    is GeneralException -> getNotificationGroup("Generation Error")
    else -> getNotificationGroup("Execution Error")
}

private fun getNotificationTitleFor(
    exception: TestSparkException
): String = when (exception) {
    is KexException -> PluginMessagesBundle.get("kexErrorTitle")
    is GeneralException -> PluginMessagesBundle.get("buildErrorTitle")
    else -> PluginMessagesBundle.get("unknownErrorOccurredMessage")
}

