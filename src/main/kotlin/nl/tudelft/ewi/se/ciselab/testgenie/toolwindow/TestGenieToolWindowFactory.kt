package nl.tudelft.ewi.se.ciselab.testgenie.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory

/**
 * This class is responsible for creating the UI of the TestGenie tool window.
 */
class TestGenieToolWindowFactory : ToolWindowFactory {
    /**
     * Initialises the UI of the tool window.
     */
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val quickAccessParameters = QuickAccessParameters(project)
        val contentFactory: ContentFactory = ContentFactory.SERVICE.getInstance()
        val content: Content = contentFactory.createContent(quickAccessParameters.getContent(), "Parameters", false)
        toolWindow.contentManager.addContent(content)
    }
}
