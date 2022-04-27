package com.github.mitchellolsthoorn.testgenie.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import javax.swing.JTextArea
import javax.swing.JTextField

class TestGenieToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val testGeniePanelWrapper = TestGenieToolWindow(toolWindow)
        val contentFactory : ContentFactory = ContentFactory.SERVICE.getInstance()
        val content : Content = contentFactory.createContent(testGeniePanelWrapper.getContent(), "TestGenie1", false)

        toolWindow.contentManager.addContent(content)
        toolWindow.contentManager.addContent(contentFactory.createContent(JTextArea("Here is where the hyperparameters will appear"), "Hyper-Parameters", false))
    }
}