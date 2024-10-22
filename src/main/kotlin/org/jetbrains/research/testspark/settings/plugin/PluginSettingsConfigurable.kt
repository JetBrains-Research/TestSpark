package org.jetbrains.research.testspark.settings.plugin

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.services.PluginSettingsService
import org.jetbrains.research.testspark.settings.template.SettingsConfigurable
import javax.swing.JComponent

/**
 * This class allows to configure some Plugin settings via the Plugin page in the Settings dialog, observes the changes and manages the UI and state
 * It interacts with the SettingsPluginComponent, TestSparkSettingsService and TestSparkSettingsState.
 * It provides controller functionality for the TestSparkSettingsState.
 */
class PluginSettingsConfigurable(val project: Project) : SettingsConfigurable {

    var settingsComponent: PluginSettingsComponent? = null

    /**
     * Creates a settings component that holds the panel with the settings entries, and returns this panel
     *
     * @return the panel used for displaying settings
     */
    override fun createComponent(): JComponent? {
        settingsComponent = PluginSettingsComponent()
        return settingsComponent!!.panel
    }

    /**
     * Sets the stored state values to the corresponding UI components. This method is called immediately after `createComponent` method.
     */
    override fun reset() {
        val settingsState: PluginSettingsState = project.service<PluginSettingsService>().state
        settingsComponent!!.showCoverageCheckboxSelected = settingsState.showCoverageCheckboxSelected
        settingsComponent!!.buildPath = settingsState.buildPath
        settingsComponent!!.colorRed = settingsState.colorRed
        settingsComponent!!.colorGreen = settingsState.colorGreen
        settingsComponent!!.colorBlue = settingsState.colorBlue
        settingsComponent!!.buildCommand = settingsState.buildCommand
    }

    /**
     * Checks if the values of the entries in the settings state are different from the persisted values of these entries.
     *
     * @return whether any setting has been modified
     */
    override fun isModified(): Boolean {
        val settingsState: PluginSettingsState = project.service<PluginSettingsService>().state
        var modified: Boolean = settingsComponent!!.buildPath != settingsState.buildPath
        modified = modified or (settingsComponent!!.showCoverageCheckboxSelected != settingsState.showCoverageCheckboxSelected)
        modified = modified or (settingsComponent!!.colorRed != settingsState.colorRed)
        modified = modified or (settingsComponent!!.colorGreen != settingsState.colorGreen)
        modified = modified or (settingsComponent!!.colorBlue != settingsState.colorBlue)
        modified = modified or (settingsComponent!!.buildCommand != settingsState.buildCommand)
        return modified
    }

    /**
     * Persists the modified state after a user hit Apply button.
     */
    override fun apply() {
        val settingsState: PluginSettingsState = project.service<PluginSettingsService>().state
        settingsState.showCoverageCheckboxSelected = settingsComponent!!.showCoverageCheckboxSelected
        settingsState.colorRed = settingsComponent!!.colorRed
        settingsState.colorGreen = settingsComponent!!.colorGreen
        settingsState.colorBlue = settingsComponent!!.colorBlue
        settingsState.buildPath = settingsComponent!!.buildPath
        settingsState.buildCommand = settingsComponent!!.buildCommand
    }

    /**
     * Returns the displayed name of the Settings tab.
     *
     * @return the name displayed in the menu (settings)
     */
    override fun getDisplayName(): String {
        return "TestSpark"
    }

    /**
     * Disposes the UI resources. It is called when a user closes the Settings dialog.
     */
    override fun disposeUIResources() {
        settingsComponent = null
    }
}
