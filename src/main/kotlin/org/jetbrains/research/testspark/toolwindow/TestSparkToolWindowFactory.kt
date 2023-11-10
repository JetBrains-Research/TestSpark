package org.jetbrains.research.testspark.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import org.jetbrains.research.testspark.TestSparkLabelsBundle

/**
 * This class is responsible for creating the tabs and the UI of the TestSpark tool window.
 */
class TestSparkToolWindowFactory : ToolWindowFactory {
    /**
     * Initialises the UI of the tool window.
     */
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val descriptionTab = DescriptionTab(project)
        val contentFactory: ContentFactory = ContentFactory.getInstance()
        val content: Content = contentFactory.createContent(descriptionTab.getContent(), TestSparkLabelsBundle.defaultValue("descriptionWindow"), false)
        toolWindow.contentManager.addContent(content)
    }
}
