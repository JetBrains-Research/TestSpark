package com.github.mitchellolsthoorn.testgenie.toolwindow

import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindow
import java.awt.Dimension
import javax.swing.JButton
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
    private var saveButton: JButton? = null

    private var toolWindowPanel: JPanel? = null

    init {
        saveButton?.addActionListener {
            Messages.showInfoMessage(
                "Settings saved (not actually, still WIP)" +
                        "\nmax_size: ${maxSizeTextField?.text}" +
                        "\nglobal_timeout: ${globalTimeOutTextField?.text}" +
                        "\ncoverage: ${coverageCombobox?.selectedItem}",
                "Saved"
            )
        }
    }

    fun getContent(): JComponent? {
        return toolWindowPanel!!
    }
}