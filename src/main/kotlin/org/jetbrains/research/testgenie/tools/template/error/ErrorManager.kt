package org.jetbrains.research.testgenie.tools.template.error

import com.intellij.openapi.project.Project

interface ErrorManager {
    fun errorProcess(message: String, project: Project)
    fun warningProcess(message: String, project: Project)
}
