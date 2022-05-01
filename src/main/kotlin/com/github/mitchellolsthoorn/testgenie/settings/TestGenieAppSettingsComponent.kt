package com.github.mitchellolsthoorn.testgenie.settings

import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * This class displays and captures changes to the values of the Settings entries.
 */
class TestGenieAppSettingsComponent {
    var panel: JPanel? = null
    var userNameTextField = JTextField()
    var ideaUserStatusCheckBox = JCheckBox("Is amogus bad? ")

    init {
        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Enter user name: "), userNameTextField, 1, false)
            .addComponent(ideaUserStatusCheckBox, 1)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    /**
     * Returns the UI component that should be focused when a user opens the TestGenie Settings page.
     */
    fun getPreferredFocusedComponent(): JComponent {
        return userNameTextField
    }


    var userNameText: String?
        get() = userNameTextField.text
        set(newText) {
            userNameTextField.text = newText
        }

    var ideaUserStatus: Boolean
        get() = ideaUserStatusCheckBox.isSelected
        set(newStatus) {
            ideaUserStatusCheckBox.isSelected = newStatus
        }
}