package org.jetbrains.research.testspark.settings

import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import org.jetbrains.research.testspark.services.PromptParserService
import org.jetbrains.research.testspark.services.SettingsApplicationService
import javax.swing.JComponent

/**
 * This class allows to configure some LLM-related settings via the Large Language Model page in the Settings dialog,
 *   observes the changes and manages the UI and state.
 */

class SettingsLLMConfigurable : Configurable {

    var settingsComponent: SettingsLLMComponent? = null

    /**
     * Creates a settings component that holds the panel with the settings entries, and returns this panel
     *
     * @return the panel used for displaying settings
     */
    override fun createComponent(): JComponent? {
        settingsComponent = SettingsLLMComponent()
        return settingsComponent!!.panel
    }

    /**
     * Sets the stored state values to the corresponding UI components. This method is called immediately after `createComponent` method.
     */
    override fun reset() {
        val settingsState: SettingsApplicationState = SettingsApplicationService.getInstance().state!!
        settingsComponent!!.llmUserToken = settingsState.llmUserToken
        settingsComponent!!.model = settingsState.model
        settingsComponent!!.llmPlatform = settingsState.llmPlatform
        settingsComponent!!.maxLLMRequest = settingsState.maxLLMRequest
        settingsComponent!!.maxPolyDepth = settingsState.maxPolyDepth
        settingsComponent!!.maxInputParamsDepth = settingsState.maxInputParamsDepth
        settingsComponent!!.classPrompt = settingsState.classPrompt
        settingsComponent!!.methodPrompt = settingsState.methodPrompt
        settingsComponent!!.linePrompt = settingsState.linePrompt
    }

    /**
     * Checks if the values of the entries in the settings state are different from the persisted values of these entries.
     *
     * @return whether any setting has been modified
     */
    override fun isModified(): Boolean {
        val settingsState: SettingsApplicationState = SettingsApplicationService.getInstance().state!!
        var modified: Boolean = settingsComponent!!.llmUserToken != settingsState.llmUserToken
        modified = modified or (settingsComponent!!.model != settingsState.model)
        modified = modified or (settingsComponent!!.llmPlatform != settingsState.llmPlatform)
        modified = modified or (settingsComponent!!.maxLLMRequest != settingsState.maxLLMRequest)
        modified = modified or (settingsComponent!!.maxPolyDepth != settingsState.maxPolyDepth)
        modified = modified or (settingsComponent!!.maxInputParamsDepth != settingsState.maxInputParamsDepth)
        // class prompt
        modified = modified or (settingsComponent!!.classPrompt != settingsState.classPrompt)
        modified = modified and service<PromptParserService>().isPromptValid(settingsComponent!!.classPrompt)
        // method prompt
        modified = modified or (settingsComponent!!.methodPrompt != settingsState.methodPrompt)
        modified = modified and service<PromptParserService>().isPromptValid(settingsComponent!!.methodPrompt)
        // line prompt
        modified = modified or (settingsComponent!!.linePrompt != settingsState.linePrompt)
        modified = modified and service<PromptParserService>().isPromptValid(settingsComponent!!.linePrompt)

        return modified
    }

    /**
     * Persists the modified state after a user hit Apply button.
     */
    override fun apply() {
        val settingsState: SettingsApplicationState = SettingsApplicationService.getInstance().state!!
        settingsState.llmUserToken = settingsComponent!!.llmUserToken
        settingsState.model = settingsComponent!!.model
        settingsState.llmPlatform = settingsComponent!!.llmPlatform
        settingsState.maxLLMRequest = settingsComponent!!.maxLLMRequest
        settingsState.maxPolyDepth = settingsComponent!!.maxPolyDepth
        settingsState.maxInputParamsDepth = settingsComponent!!.maxInputParamsDepth
        settingsState.classPrompt = settingsComponent!!.classPrompt
        settingsState.methodPrompt = settingsComponent!!.methodPrompt
        settingsState.linePrompt = settingsComponent!!.linePrompt
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
