package org.jetbrains.research.testspark.tools.template.error

import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.core.monitor.ErrorMonitor

/**
 * Represents an error manager interface for handling errors and warnings.
 */
interface ErrorManager {
    /**
     * Processes an error message for a specific project.
     *
     * @param message the error message to be processed
     * @param project the project for which the error occurred
     */
    fun errorProcess(
        message: String,
        project: Project,
        errorMonitor: ErrorMonitor,
    )

    /**
     * Processes a warning message for a specific project.
     *
     * @param message the warning message to be processed
     * @param project the project for which the warning is being processed
     */
    fun warningProcess(
        message: String,
        project: Project,
    )
}
