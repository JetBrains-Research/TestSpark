package org.jetbrains.research.testspark.tools.evosuite.error

import com.intellij.execution.process.OSProcessHandler
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.services.ErrorService
import org.jetbrains.research.testspark.tools.processStopped
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
        org.jetbrains.research.testspark.TestSparkBundle.message("evosuiteErrorCommon") + " " + message

    /**
     * Returns the exceeded timeout message with the specified EvoSuite process timeout.
     *
     * @param evoSuiteProcessTimeout The timeout value for the EvoSuite process in milliseconds.
     * @return The exceeded timeout message.
     */
    private fun getExceededTimeoutMessage(evoSuiteProcessTimeout: Long) =
        org.jetbrains.research.testspark.TestSparkBundle.message("exceededTimeoutMessage") + " " + evoSuiteProcessTimeout + " ms"

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
            ?: org.jetbrains.research.testspark.TestSparkBundle.message("nonZeroCodeMessage")

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
        indicator: ProgressIndicator,
    ): Boolean {
        if (processStopped(project, indicator)) return false

        // exceeded timeout error
        if (!handler.waitFor(evoSuiteProcessTimeout)) {
            errorProcess(
                getExceededTimeoutMessage(evoSuiteProcessTimeout),
                project,
            )
            return false
        }

        // non zero exit code error
        if (handler.exitCode != 0) {
            errorProcess(getEvoSuiteNonZeroExitCodeMessage(output), project)
            return false
        }

        // unknown class error
        if (output.contains(org.jetbrains.research.testspark.TestSparkBundle.message("unknownClassError"))) {
            errorProcess(org.jetbrains.research.testspark.TestSparkBundle.message("unknownClassMessage"), project)
            return false
        }

        // error while initializing target class
        if (output.contains(org.jetbrains.research.testspark.TestSparkBundle.message("errorWhileInitializingTargetClass"))) {
            errorProcess(
                org.jetbrains.research.testspark.TestSparkBundle.message("errorWhileInitializingTargetClass").lowercase(Locale.getDefault()),
                project,
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
    override fun errorProcess(message: String, project: Project) {
        if (project.service<ErrorService>().errorOccurred()) {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("EvoSuite Execution Error")
                .createNotification(
                    org.jetbrains.research.testspark.TestSparkBundle.message("evosuiteErrorTitle"),
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
                org.jetbrains.research.testspark.TestSparkBundle.message("evosuiteErrorTitle"),
                getCommonErrorMessage(message),
                NotificationType.WARNING,
            )
            .notify(project)
    }
}
