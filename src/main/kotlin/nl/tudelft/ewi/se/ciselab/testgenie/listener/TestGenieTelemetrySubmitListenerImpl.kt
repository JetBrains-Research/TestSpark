package nl.tudelft.ewi.se.ciselab.testgenie.listener

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import nl.tudelft.ewi.se.ciselab.testgenie.services.TestGenieTelemetryService

class TestGenieTelemetrySubmitListenerImpl : ProjectManagerListener {
    private val log = Logger.getInstance(this.javaClass)

    override fun projectClosing(project: Project) {
        log.info("Saving generated telemetry...")

        ApplicationManager.getApplication().getService(TestGenieTelemetryService::class.java).submitTelemetry()
    }
}
