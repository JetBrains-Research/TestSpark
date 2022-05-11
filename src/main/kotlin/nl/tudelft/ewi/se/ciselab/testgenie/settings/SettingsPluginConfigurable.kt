package nl.tudelft.ewi.se.ciselab.testgenie.settings

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

/**
 * This class interacts with the other two Settings classes. It provides controller functionality for the settingsState.
 */
class SettingsPluginConfigurable : Configurable {

    private var settingsComponent: SettingsPluginComponent? = null

    /**
     * Creates a settings component that holds the panel with the settings entries, and returns this panel
     *
     * @return the panel used for displaying settings
     */
    override fun createComponent(): JComponent? {
        settingsComponent = SettingsPluginComponent()
        return settingsComponent!!.panel
    }

    /**
     * Sets the stored state values to the corresponding UI components. This method is called immediately after `createComponent` method.
     */
    override fun reset() {
        val settingsState: TestGenieSettingsState = TestGenieSettingsService.getInstance().state!!
        settingsComponent!!.showCoverage = settingsState.showCoverage
    }

    /**
     * Checks if the values of the entries in the settings state are different from the persisted values of these entries.
     *
     * @return whether any setting has been modified
     */
    override fun isModified(): Boolean {
        val settingsState: TestGenieSettingsState = TestGenieSettingsService.getInstance().state!!
        return settingsComponent!!.showCoverage != settingsState.showCoverage
    }

    /**
     * Persists the modified state after a user hit Apply button.
     */
    override fun apply() {
        val settingsState: TestGenieSettingsState = TestGenieSettingsService.getInstance().state!!
        settingsState.showCoverage = settingsComponent!!.showCoverage
    }

    /**
     * Returns the displayed name of the Settings tab.
     *
     * @return the name displayed in the menu (settings)
     */
    override fun getDisplayName(): String {
        return "TestGenie"
    }

    /**
     * Returns the UI component that should be focused when the TestGenie Settings page is opened.
     *
     *  @return preferred UI component
     */
    override fun getPreferredFocusedComponent(): JComponent {
        return settingsComponent!!.getPreferredFocusedComponent()
    }

    /**
     * Disposes the UI resources. It is called when a user closes the Settings dialog.
     */
    override fun disposeUIResources() {
        settingsComponent = null
    }
}