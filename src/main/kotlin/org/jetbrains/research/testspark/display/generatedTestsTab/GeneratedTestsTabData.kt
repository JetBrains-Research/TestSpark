package org.jetbrains.research.testspark.display.generatedTestsTab

import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentManager
import org.jetbrains.research.testspark.core.data.TestCase
import javax.swing.JPanel

class GeneratedTestsTabData {
    val testCasePanels: HashMap<String, JPanel> = HashMap()
    var testsSelected: Int = 0
    val unselectedTestCases: HashMap<Int, TestCase> = HashMap()
    val testCasePanelFactories: ArrayList<TestCasePanelFactory> = arrayListOf()
    var allTestCasePanel: JPanel = JPanel()
    var scrollPane: JBScrollPane = JBScrollPane(
        allTestCasePanel,
        JBScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
        JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER,
    )
    var topButtonsPanelFactory = TopButtonsPanelFactory()
    var contentManager: ContentManager? = null
    var content: Content? = null
}
