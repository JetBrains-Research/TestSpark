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

/**
 * This class stores the UI of the TestGenie tool window.
 */
class TestGenieToolWindow(_toolWindow: ToolWindow) {

    private val toolWindow = _toolWindow

    private var maxSizeTextField: JTextField? = null
    private var globalTimeOutTextField: JTextField? = null
    private var coverageCombobox: JComboBox<Boolean>? = null
    private var saveButton: JButton? = null

    private var toolWindowPanel: JPanel? = null

    init {
        saveButton?.addActionListener { addListenerForSaveButton(it) }
    }

    /**
     * Returns the panel that is the main wrapper component of the tool window.
     */
    fun getContent(): JComponent? {
        return toolWindowPanel!!
    }


    /**
     * Adds a listener to the `Save` button to parse, validate and extract the entered values.
     */
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
                            "\nMax size: $maxSize" +
                            "\nGlobal timeout: $globalTimeout" +
                            "\nCoverage: $coverage",
                    "Saved"
                )
            }
        }
    }

    /**
     * Convert a string to an integer, or return null in case of an exception
     */
    private fun toInt(str: String): Int? {
        return try {
            str.toInt()
        } catch (e : java.lang.NumberFormatException) {
            null
        }
    }
}