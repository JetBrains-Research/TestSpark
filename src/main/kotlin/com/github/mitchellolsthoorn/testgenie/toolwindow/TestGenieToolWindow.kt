package com.github.mitchellolsthoorn.testgenie.toolwindow

import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindow
import java.awt.Dimension
import java.awt.event.ActionEvent
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
        saveButton?.addActionListener { addListenerForSaveButton(it) }
    }

    fun getContent(): JComponent? {
        return toolWindowPanel!!
    }


    private val addListenerForSaveButton : (ActionEvent) -> Unit = {
        if (maxSizeTextField?.text == null || globalTimeOutTextField?.text == null) {
            Messages.showErrorDialog("Please specify the value", "Empty Value Field")
        } else {
            // Validate all the input values
            val maxSize: Int? = toInt(maxSizeTextField!!.text)
            val globalTimeout: Int? = toInt(globalTimeOutTextField!!.text)
            val coverage : Boolean = coverageCombobox?.selectedItem.toString().toBoolean()

            if (maxSize == null || globalTimeout == null) {
                Messages.showErrorDialog("Please specify a number", "Invalid Input Value")
            } else {
                Messages.showInfoMessage(
                    "Settings saved (not actually, still WIP)" +
                            "\nmax_size: $maxSize" +
                            "\nglobal_timeout: $globalTimeout" +
                            "\ncoverage: $coverage",
                    "Saved"
                )
            }
        }
    }

    private fun toInt(str: String): Int? {
        return try {
            str.toInt()
        } catch (e : java.lang.NumberFormatException) {
            null
        }
    }
}