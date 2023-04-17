package org.jetbrains.research.testgenie.listener

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import org.jetbrains.research.testgenie.services.TestGenieTelemetryService

class ProjectCloseListenerImpl : ProjectManagerListener {
    private val log = Logger.getInstance(this.javaClass)

    /**
     * Attempts to submit the telemetry into a file when the project is closed.
     *
     * @param project the current project
     */
    override fun projectClosing(project: Project) {
        log.info("Checking generated telemetry for the project ${project.name} before closing...")

        project.service<TestGenieTelemetryService>().submitTelemetry()
    }
}
