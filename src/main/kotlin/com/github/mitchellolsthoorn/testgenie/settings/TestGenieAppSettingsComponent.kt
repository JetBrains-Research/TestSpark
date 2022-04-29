package com.github.mitchellolsthoorn.testgenie.settings

import javax.swing.JPanel

import com.intellij.ui.components.JBLabel

import com.intellij.util.ui.FormBuilder

import com.intellij.ui.components.JBCheckBox

import com.intellij.ui.components.JBTextField

class TestGenieAppSettingsComponent {
    var panel: JPanel? = null
    val myUserNameText = JBTextField()
    val myIdeaUserStatus = JBCheckBox("Is amogus bad? ")

    init {
        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Enter user name: "), myUserNameText, 1, false)
            .addComponent(myIdeaUserStatus, 1)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

//    var userNameText: String?
//        get() = myUserNameText.text
//        set(newText) {
//            myUserNameText.text = newText
//        }
//    var ideaUserStatus: Boolean
//        get() = myIdeaUserStatus.isSelected
//        set(newStatus) {
//            myIdeaUserStatus.isSelected = newStatus
//        }
}