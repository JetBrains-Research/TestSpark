package com.github.mitchellolsthoorn.testgenie.toolwindow

import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import javax.swing.JComponent

class TestGenieToolWindow(toolWindow: ToolWindow) {
    private val testGenieLabel : JComponent = createLabel("TestGenie is amogus!")

    // A helper method to create a label
    private fun createLabel(text: String): JComponent {
        val label = JBLabel(text)
        label.componentStyle = UIUtil.ComponentStyle.SMALL
        label.fontColor = UIUtil.FontColor.BRIGHTER
        label.border = JBUI.Borders.empty(0, 5, 2, 0)
        return label
    }

    fun getContent(): JComponent {
        return testGenieLabel
    }
}