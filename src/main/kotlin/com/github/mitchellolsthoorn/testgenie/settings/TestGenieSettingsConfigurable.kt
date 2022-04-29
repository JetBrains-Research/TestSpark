package com.github.mitchellolsthoorn.testgenie.settings

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

class TestGenieSettingsConfigurable : Configurable {

    private var settingsComponent: TestGenieAppSettingsComponent? = null

    override fun createComponent(): JComponent? {
        settingsComponent = TestGenieAppSettingsComponent()
        return settingsComponent!!.panel
    }

    override fun isModified(): Boolean {
        val settings: TestGenieSettingsState = TestGenieSettingsService.instance.state!!
        var modified: Boolean = !settingsComponent?.myUserNameText?.equals(settings.userId)!!
        modified = modified or (settingsComponent?.myIdeaUserStatus?.isSelected != settings.ideaStatus)
        return modified
    }

    override fun apply() {
        val settings: TestGenieSettingsState = TestGenieSettingsService.instance.state!!
        settings.userId = settingsComponent?.myUserNameText?.text!!
        settings.ideaStatus = settingsComponent?.myIdeaUserStatus?.isSelected!!
    }

    override fun getDisplayName(): String {
        return "AMOGUS"
    }

    override fun disposeUIResources() {
        settingsComponent = null
    }
}