package com.github.mitchellolsthoorn.testgenie.toolwindow

import com.intellij.openapi.wm.ToolWindow
import java.awt.Dimension
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField


class TestGenieToolWindow(_toolWindow: ToolWindow) {

    private val toolWindow = _toolWindow

    // max_size, global_timeout, coverage - some TestGenie parameters
    private var maxSizeTextField: JTextField? = null
    private var globalTimeOutTextField: JTextField? = null
    private var coverageCombobox: JComboBox<Boolean>? = null

    private var toolWindowPanel: JPanel? = null

    fun getContent(): JComponent? {
        return toolWindowPanel!!
    }
}