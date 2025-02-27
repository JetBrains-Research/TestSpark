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
import org.jetbrains.research.testspark.core.exception.CommonException
import org.jetbrains.research.testspark.core.exception.CompilerException
import org.jetbrains.research.testspark.core.exception.TestSparkException
import org.jetbrains.research.testspark.tools.error.message.commonExceptionMessage
import org.jetbrains.research.testspark.tools.error.message.compilerExceptionMessage
import org.jetbrains.research.testspark.tools.error.message.evoSuiteErrorDisplayMessage
import org.jetbrains.research.testspark.tools.error.message.httpErrorDisplayMessage
import org.jetbrains.research.testspark.tools.error.message.kexErrorDisplayMessage
import org.jetbrains.research.testspark.tools.error.message.llmErrorDisplayMessage

fun Project.createNotification(
    error: TestSparkError,
    notificationType: NotificationType
) = createNotification(
    module = error.module,
    message = error.displayMessage ?: PluginMessagesBundle.get("unknownErrorMessage"),
    notificationType = notificationType,
    logMessage = "$error ${error.displayMessage?.let { ": $it" } ?: ""}",
)

fun Project.createNotification(
    exception: TestSparkException,
    notificationType: NotificationType
) = createNotification(
    module = exception.module,
    message = exception.displayMessage ?: PluginMessagesBundle.get("unknownErrorMessage"),
    notificationType = notificationType,
    logMessage = "$exception ${exception.displayMessage?.let { ": $it" } ?: ""}",
)

val TestSparkException.displayMessage: String?
    get() = when (this) {
        is CommonException -> commonExceptionMessage
        is CompilerException -> compilerExceptionMessage
        else -> null
    }

val TestSparkError.displayMessage: String?
    get() = when (this) {
        is LlmError -> llmErrorDisplayMessage
        is HttpError -> httpErrorDisplayMessage
        is EvoSuiteError -> evoSuiteErrorDisplayMessage
        is KexError -> kexErrorDisplayMessage
        else -> null
    }

fun Project.createNotification(
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
    is TestSparkModule.Llm -> getNotificationGroup("LLM Execution Error")
    is TestSparkModule.EvoSuite -> getNotificationGroup("EvoSuite Execution Error")
    is TestSparkModule.Kex -> getNotificationGroup("Kex Execution Error")
    is TestSparkModule.UI -> getNotificationGroup("UserInterface")
    is TestSparkModule.ProjectBuilder -> getNotificationGroup("Build Execution Error")
    is TestSparkModule.Common -> getNotificationGroup("Execution Error")
    is TestSparkModule.Compiler -> getNotificationGroup("Compiler Error")
}

private fun getNotificationTitleFor(
    module: TestSparkModule
): String = when (module) {
    is TestSparkModule.Llm -> PluginMessagesBundle.get("llmErrorTitle")
    is TestSparkModule.EvoSuite -> PluginMessagesBundle.get("evosuiteErrorTitle")
    is TestSparkModule.Kex -> PluginMessagesBundle.get("kexErrorTitle")
    is TestSparkModule.UI -> PluginMessagesBundle.get("generationWindowWarningTitle")
    is TestSparkModule.ProjectBuilder -> PluginMessagesBundle.get("buildErrorTitle")
    is TestSparkModule.Common -> PluginMessagesBundle.get("commonErrorTitle")
    is TestSparkModule.Compiler -> PluginMessagesBundle.get("compilerErrorTitle")
}
