package com.github.mitchellolsthoorn.testgenie.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory

class TestGenieToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val testGeniePanelWrapper = TestGeniePanelWrapper()
        val contentFactory : ContentFactory = ContentFactory.SERVICE.getInstance()
        val content : Content = contentFactory.createContent(testGeniePanelWrapper.getContent(), "TestGenie", false)
        toolWindow.contentManager.addContent(content)
    }
}