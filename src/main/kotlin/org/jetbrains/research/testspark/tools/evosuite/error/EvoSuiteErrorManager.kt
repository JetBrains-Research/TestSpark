package org.jetbrains.research.testspark.tools.evosuite.error

import com.intellij.execution.process.OSProcessHandler
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.bundles.evosuite.EvoSuiteMessagesBundle
import org.jetbrains.research.testspark.bundles.plugin.PluginMessagesBundle
import org.jetbrains.research.testspark.core.monitor.ErrorMonitor
import org.jetbrains.research.testspark.core.progress.CustomProgressIndicator
import org.jetbrains.research.testspark.tools.ToolUtils
import org.jetbrains.research.testspark.tools.template.error.ErrorManager
import java.util.Locale

/**
 * This class represents the error manager for EvoSuite. It provides methods for handling and displaying errors and warnings
 * encountered during EvoSuite execution.
 */
class EvoSuiteErrorManager : ErrorManager {
    private var output = ""

    /**
     * Appends a line to the EvoSuite output.
     *
     * @param line the line to be added to the EvoSuite output
     */
    fun addLineToEvoSuiteOutput(line: String) {
        output += line + "\n"
    }

    /**
     * Retrieves the common error message with the provided message.
     *
     * @param message The additional message to include in the error message.
     * @return The common error message with the provided message.
     */
    private fun getCommonErrorMessage(message: String) =
        EvoSuiteMessagesBundle.get("evosuiteErrorCommon") + " " + message

    /**
     * Returns the exceeded timeout message with the specified EvoSuite process timeout.
     *
     * @param evoSuiteProcessTimeout The timeout value for the EvoSuite process in milliseconds.
     * @return The exceeded timeout message.
     */
    private fun getExceededTimeoutMessage(evoSuiteProcessTimeout: Long) =
        EvoSuiteMessagesBundle.get("exceededTimeoutMessage") + " " + evoSuiteProcessTimeout + " ms"

    /**
     * Retrieves the error message from the EvoSuite output or the non-zero exit code message if available.
     * If neither is found, a default message is returned.
     *
     * @param evosuiteOutput The EvoSuite output as a string.
     * @return The error message or non-zero exit code message, or a default message if neither is found.
     */
    private fun getEvoSuiteNonZeroExitCodeMessage(evosuiteOutput: String) =
        "Error: (.*)\n".toRegex().find(evosuiteOutput)?.groupValues?.get(1)
            ?: "Exception: (.*)\n".toRegex().find(evosuiteOutput)?.groupValues?.get(1)
            ?: EvoSuiteMessagesBundle.get("nonZeroCodeMessage")

    /**
     * Checks if the process is correct by analyzing the output and exit code of the process.
     *
     * @param handler the OS process handler for the running process
     * @param project the current project
     * @param evoSuiteProcessTimeout the timeout for the EvoSuite process
     * @param indicator the progress indicator
     * @return true if the process is correct, false otherwise
     */
    fun isProcessCorrect(
        handler: OSProcessHandler,
        project: Project,
        evoSuiteProcessTimeout: Long,
        indicator: CustomProgressIndicator,
        errorMonitor: ErrorMonitor,
    ): Boolean {
        if (ToolUtils.isProcessStopped(errorMonitor, indicator)) return false

        // exceeded timeout error
        if (!handler.waitFor(evoSuiteProcessTimeout)) {
            errorProcess(
                getExceededTimeoutMessage(evoSuiteProcessTimeout),
                project,
                errorMonitor,
            )
            return false
        }

        if (ToolUtils.isProcessStopped(errorMonitor, indicator)) return false

        // non zero exit code error
        if (handler.exitCode != 0) {
            errorProcess(getEvoSuiteNonZeroExitCodeMessage(output), project, errorMonitor)
            return false
        }

        // unknown class error
        if (output.contains(EvoSuiteMessagesBundle.get("unknownClassError"))) {
            errorProcess(EvoSuiteMessagesBundle.get("unknownClassMessage"), project, errorMonitor)
            return false
        }

        // error while initializing target class
        if (output.contains(EvoSuiteMessagesBundle.get("errorWhileInitializingTargetClass"))) {
            errorProcess(
                EvoSuiteMessagesBundle.get("errorWhileInitializingTargetClass").lowercase(Locale.getDefault()),
                project,
                errorMonitor,
            )
            return false
        }

        return true
    }

    /**
     * Show an EvoSuite execution error balloon.
     *
     * @param message the balloon content to display
     */
    override fun errorProcess(message: String, project: Project, errorMonitor: ErrorMonitor) {
        if (errorMonitor.notifyErrorOccurrence()) {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("EvoSuite Execution Error")
                .createNotification(
                    PluginMessagesBundle.get("evosuiteErrorTitle"),
                    getCommonErrorMessage(message),
                    NotificationType.ERROR,
                )
                .notify(project)
        }
    }

    /**
     * Show an EvoSuite execution warning balloon.
     *
     * @param message the balloon content to display
     */
    override fun warningProcess(message: String, project: Project) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("EvoSuite Execution Error")
            .createNotification(
                PluginMessagesBundle.get("evosuiteErrorTitle"),
                getCommonErrorMessage(message),
                NotificationType.WARNING,
            )
            .notify(project)
    }
}
