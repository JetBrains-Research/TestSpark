package com.github.mitchellolsthoorn.testgenie.settings

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

class TestGenieSettingsConfigurable : Configurable {

    private var settingsComponent: TestGenieAppSettingsComponent = TestGenieAppSettingsComponent()

    override fun createComponent(): JComponent? {
        val settings: TestGenieSettingsState = TestGenieSettingsService.getInstance().state!!
        settingsComponent.userNameText = settings.userId
        settingsComponent.ideaUserStatus = settings.ideaStatus
        return settingsComponent.panel
    }

    override fun isModified(): Boolean {
        val settings: TestGenieSettingsState = TestGenieSettingsService.getInstance().state!!
        var modified: Boolean = settingsComponent.userNameText == settings.userId
        modified = modified or (settingsComponent.ideaUserStatus != settings.ideaStatus)
        return modified
    }

    override fun apply() {
        val settings: TestGenieSettingsState = TestGenieSettingsService.getInstance().state!!
        settings.userId = settingsComponent.userNameText!!
        settings.ideaStatus = settingsComponent.ideaUserStatus
    }

    override fun getDisplayName(): String {
        return "AMOGUS"
    }

//    override fun getPreferredFocusedComponent(): JComponent {
//        return settingsComponent.myUserNameText
//    }
}