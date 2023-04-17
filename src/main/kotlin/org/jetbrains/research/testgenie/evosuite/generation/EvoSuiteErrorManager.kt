package org.jetbrains.research.testgenie.evosuite.generation

import com.intellij.execution.process.OSProcessHandler
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import org.jetbrains.research.testgenie.TestGenieBundle
import java.util.Locale

class EvoSuiteErrorManager {
    private var output = ""

    fun addLineToEvoSuiteOutput(line: String) {
        output += line + "\n"
    }

    private fun getCommonErrorMessage(message: String) =
        TestGenieBundle.message("evosuiteErrorCommon") + " " + message

    private fun getExceededTimeoutMessage(evoSuiteProcessTimeout: Long) =
        TestGenieBundle.message("exceededTimeoutMessage") + " " + evoSuiteProcessTimeout + " ms"

    // add the message of the error or exception from the evosuite output, or the non-zero exit code message otherwise
    private fun getEvoSuiteNonZeroExitCodeMessage(evosuiteOutput: String) =
        "Error: (.*)\n".toRegex().find(evosuiteOutput)?.groupValues?.get(1)
            ?: "Exception: (.*)\n".toRegex().find(evosuiteOutput)?.groupValues?.get(1)
            ?: TestGenieBundle.message("nonZeroCodeMessage")

    fun isProcessCorrect(
        handler: OSProcessHandler,
        project: Project,
        evoSuiteProcessTimeout: Long,
    ): Boolean {
        // exceeded timeout error
        if (!handler.waitFor(evoSuiteProcessTimeout)) {
            display(
                getExceededTimeoutMessage(evoSuiteProcessTimeout),
                project
            )
            return false
        }

        // non zero exit code error
        if (handler.exitCode != 0) {
            display(getEvoSuiteNonZeroExitCodeMessage(output), project)
            return false
        }

        // unknown class error
        if (output.contains(TestGenieBundle.message("unknownClassError"))) {
            display(TestGenieBundle.message("unknownClassMessage"), project)
            return false
        }

        // error while initializing target class
        if (output.contains(TestGenieBundle.message("errorWhileInitializingTargetClass"))) {
            display(
                TestGenieBundle.message("errorWhileInitializingTargetClass").lowercase(Locale.getDefault()),
                project
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
    fun display(message: String, project: Project) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("EvoSuite Execution Error")
            .createNotification(
                TestGenieBundle.message("evosuiteErrorTitle"),
                getCommonErrorMessage(message),
                NotificationType.ERROR
            )
            .notify(project)
    }
}
