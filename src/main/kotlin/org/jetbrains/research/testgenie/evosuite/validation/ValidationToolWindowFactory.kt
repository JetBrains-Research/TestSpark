package org.jetbrains.research.testgenie.evosuite.validation

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

/**
 * This class is responsible for creating the tabs and the UI of the tool window corresponding to dynamic test validation.
 */
class ValidationToolWindowFactory : ToolWindowFactory {
    /**
     * Initialises the UI of the tool window.
     */
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    }
}
