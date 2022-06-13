package nl.tudelft.ewi.se.ciselab.testgenie.services

import com.github.javaparser.ParseProblemException
import com.github.javaparser.StaticJavaParser
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.tudelft.ewi.se.ciselab.testgenie.TestGenieBundle

/**
 * Service used for the sole purpose to limit TestGenie to generate tests only once at a time.
 */
class RunnerService(private val project: Project) {
    var isRunning: Boolean = false

    /**
     * Method to show notification that test generation is already running.
     */
    private fun showGenerationRunningNotification() {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("EvoSuite Execution Error")
            .createNotification(
                TestGenieBundle.message("alreadyRunningNotificationTitle"),
                TestGenieBundle.message("alreadyRunningTextNotificationText"),
                NotificationType.WARNING
            )
            .notify(project)
    }

    /**
     * Method to show notification that the class cannot be parsed.
     */
    private fun showParsingFailedNotification() {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("EvoSuite Execution Error")
            .createNotification(
                TestGenieBundle.message("parsingFailedNotificationTitle"),
                TestGenieBundle.message("parsingFailedNotificationText"),
                NotificationType.ERROR
            )
            .notify(project)
    }

    /**
     * Check if EvoSuite is running.
     *
     * @return true if it is already running
     */
    private fun isEvoSuiteRunning(): Boolean {
        if (isRunning) {
            showGenerationRunningNotification()
            return true
        }
        isRunning = true
        return false
    }

    /**
     * Check if class is parsable.
     *
     * @return true if it is parsable
     */
    private fun isParsing(psiFile: PsiFile): Boolean {
        return try {
            StaticJavaParser.parse(psiFile.text)
            true
        } catch (e: ParseProblemException) {
            showParsingFailedNotification()
            isRunning = false
            false
        }
    }

    /**
     * Method to verify if action can be executed.
     *
     * @return true if action can be executed
     */
    fun verify(psiFile: PsiFile): Boolean {
        if (isEvoSuiteRunning()) return true
        return isParsing(psiFile)
    }
}
