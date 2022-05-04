package com.github.mitchellolsthoorn.testgenie.toolwindow

import com.github.mitchellolsthoorn.testgenie.services.TestCaseDisplayService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.JTextField

/**
 * This class is responsible for creating the UI of the TestGenie tool window.
 */
class TestGenieToolWindowFactory : ToolWindowFactory {
    /**
     * Initialises the UI of the tool window.
     */
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val testGeniePanelWrapper = TestGenieToolWindow(toolWindow)
        val contentFactory : ContentFactory = ContentFactory.SERVICE.getInstance()

        val testCaseDisplayService = project.service<TestCaseDisplayService>()
        toolWindow.contentManager.addContent(
            contentFactory.createContent(JScrollPane(testCaseDisplayService.panel),
                "Generated Tests",
                true)
        )

        val content : Content = contentFactory.createContent(testGeniePanelWrapper.getContent(), "Parameters", false)

        toolWindow.contentManager.addContent(content)
        toolWindow.contentManager.addContent(contentFactory.createContent(JTextArea("Here is where the coverage visualisation will appear"), "Coverage Visualisation", false))
    }
}