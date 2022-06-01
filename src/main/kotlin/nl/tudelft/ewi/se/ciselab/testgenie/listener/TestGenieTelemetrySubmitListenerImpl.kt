package nl.tudelft.ewi.se.ciselab.testgenie.listener

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import nl.tudelft.ewi.se.ciselab.testgenie.services.TestGenieTelemetryService
import java.util.Timer
import java.util.TimerTask

/**
 * This class is responsible for scheduling potential submissions of telemetry into a file, which is done every 5 minutes,
 *   as well as attempting to do it when the project is closed.
 */
class TestGenieTelemetrySubmitListenerImpl : ProjectManagerListener {
    private val log = Logger.getInstance(this.javaClass)

    /**
     * Schedules attempts to submit the telemetry into a file.
     * The attempts are done every 5 minutes and is first done 5 minutes after opening a project.
     *
     * @param project the current project
     */
    override fun projectOpened(project: Project) {
        Timer().scheduleAtFixedRate(
            object : TimerTask() {
                override fun run() {
                    log.info("Checking generated telemetry...")
                    ApplicationManager.getApplication()
                        .getService(TestGenieTelemetryService::class.java).submitTelemetry()
                }
            },
            300000, 300000
        )
    }

    /**
     * Attempts to submit the telemetry into a file when the project is closed.
     *
     * @param project the current project
     */
    override fun projectClosing(project: Project) {
        log.info("Checking generated telemetry...")

        ApplicationManager.getApplication()
            .getService(TestGenieTelemetryService::class.java).submitTelemetry()
    }
}
