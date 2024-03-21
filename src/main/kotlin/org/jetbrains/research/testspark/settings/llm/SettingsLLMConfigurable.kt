package org.jetbrains.research.testspark.settings.llm

import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import org.jetbrains.research.testspark.services.PromptParserService
import org.jetbrains.research.testspark.services.SettingsApplicationService
import org.jetbrains.research.testspark.settings.SettingsApplicationState
import javax.swing.JComponent

/**
 * This class allows to configure some LLM-related settings via the Large Language Model page in the Settings dialog,
 *   observes the changes and manages the UI and state.
 */
class SettingsLLMConfigurable : Configurable {
    private val settingsState: SettingsApplicationState
        get() = SettingsApplicationService.getInstance().state!!

    private var settingsComponent: SettingsLLMComponent? = null

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
        for (index in settingsComponent!!.llmPlatforms.indices) {
            if (settingsComponent!!.llmPlatforms[index].name == settingsState.openAIName) {
                settingsComponent!!.llmPlatforms[index].token = settingsState.openAIToken
                settingsComponent!!.llmPlatforms[index].model = settingsState.openAIModel
            }
            if (settingsComponent!!.llmPlatforms[index].name == settingsState.grazieName) {
                settingsComponent!!.llmPlatforms[index].token = settingsState.grazieToken
                settingsComponent!!.llmPlatforms[index].model = settingsState.grazieModel
            }
        }
        settingsComponent!!.currentLLMPlatformName = settingsState.currentLLMPlatformName
        settingsComponent!!.maxLLMRequest = settingsState.maxLLMRequest
        settingsComponent!!.maxPolyDepth = settingsState.maxPolyDepth
        settingsComponent!!.maxInputParamsDepth = settingsState.maxInputParamsDepth
        settingsComponent!!.junitVersion = settingsState.junitVersion
        settingsComponent!!.junitVersionPriorityCheckBoxSelected = settingsState.junitVersionPriorityCheckBoxSelected
        settingsComponent!!.classPrompt = settingsState.classPrompt
        settingsComponent!!.methodPrompt = settingsState.methodPrompt
        settingsComponent!!.linePrompt = settingsState.linePrompt
        settingsComponent!!.llmSetupCheckBoxSelected = settingsState.llmSetupCheckBoxSelected
        settingsComponent!!.provideTestSamplesCheckBoxSelected = settingsState.provideTestSamplesCheckBoxSelected
        settingsComponent!!.defaultLLMRequests = settingsState.defaultLLMRequests

        settingsComponent!!.updateTokenAndModel()
    }

    /**
     * Checks if the values of the entries in the settings state are different from the persisted values of these entries.
     *
     * @return whether any setting has been modified
     */
    override fun isModified(): Boolean {
        var modified = false
        for (index in settingsComponent!!.llmPlatforms.indices) {
            if (settingsComponent!!.llmPlatforms[index].name == settingsState.openAIName) {
                modified = modified or (settingsComponent!!.llmPlatforms[index].token != settingsState.openAIToken)
                modified = modified or (settingsComponent!!.llmPlatforms[index].model != settingsState.openAIModel)
            }
            if (settingsComponent!!.llmPlatforms[index].name == settingsState.grazieName) {
                modified = modified or (settingsComponent!!.llmPlatforms[index].token != settingsState.grazieToken)
                modified = modified or (settingsComponent!!.llmPlatforms[index].model != settingsState.grazieModel)
            }
        }
        modified = modified or (settingsComponent!!.currentLLMPlatformName != settingsState.currentLLMPlatformName)
        modified = modified or (settingsComponent!!.maxLLMRequest != settingsState.maxLLMRequest)
        modified = modified or (settingsComponent!!.maxPolyDepth != settingsState.maxPolyDepth)
        modified = modified or (settingsComponent!!.maxInputParamsDepth != settingsState.maxInputParamsDepth)

        // junit version
        modified = modified or (settingsComponent!!.junitVersion != settingsState.junitVersion)
        modified = modified or (settingsComponent!!.junitVersionPriorityCheckBoxSelected != settingsState.junitVersionPriorityCheckBoxSelected)

        // class prompt
        modified = modified or (settingsComponent!!.classPrompt != settingsState.classPrompt)
        modified = modified and service<PromptParserService>().isPromptValid(settingsComponent!!.classPrompt)
        // method prompt
        modified = modified or (settingsComponent!!.methodPrompt != settingsState.methodPrompt)
        modified = modified and service<PromptParserService>().isPromptValid(settingsComponent!!.methodPrompt)
        // line prompt
        modified = modified or (settingsComponent!!.linePrompt != settingsState.linePrompt)
        modified = modified and service<PromptParserService>().isPromptValid(settingsComponent!!.linePrompt)

        modified = modified or (settingsComponent!!.llmSetupCheckBoxSelected != settingsState.llmSetupCheckBoxSelected)
        modified = modified or (settingsComponent!!.provideTestSamplesCheckBoxSelected != settingsState.provideTestSamplesCheckBoxSelected)

        modified = modified or (settingsComponent!!.defaultLLMRequests != settingsState.defaultLLMRequests)

        return modified
    }

    /**
     * Persists the modified state after a user hit Apply button.
     */
    override fun apply() {
        for (index in settingsComponent!!.llmPlatforms.indices) {
            if (settingsComponent!!.llmPlatforms[index].name == settingsState.openAIName) {
                settingsState.openAIToken = settingsComponent!!.llmPlatforms[index].token
                settingsState.openAIModel = settingsComponent!!.llmPlatforms[index].model
            }
            if (settingsComponent!!.llmPlatforms[index].name == settingsState.grazieName) {
                settingsState.grazieToken = settingsComponent!!.llmPlatforms[index].token
                settingsState.grazieModel = settingsComponent!!.llmPlatforms[index].model
            }
        }
        settingsState.currentLLMPlatformName = settingsComponent!!.currentLLMPlatformName
        settingsState.maxLLMRequest = settingsComponent!!.maxLLMRequest
        settingsState.maxPolyDepth = settingsComponent!!.maxPolyDepth
        settingsState.maxInputParamsDepth = settingsComponent!!.maxInputParamsDepth
        settingsState.junitVersion = settingsComponent!!.junitVersion
        settingsState.junitVersionPriorityCheckBoxSelected = settingsComponent!!.junitVersionPriorityCheckBoxSelected
        settingsState.classPrompt = settingsComponent!!.classPrompt
        settingsState.methodPrompt = settingsComponent!!.methodPrompt
        settingsState.linePrompt = settingsComponent!!.linePrompt
        settingsState.defaultLLMRequests = settingsComponent!!.defaultLLMRequests
        settingsState.llmSetupCheckBoxSelected = settingsComponent!!.llmSetupCheckBoxSelected
        settingsState.provideTestSamplesCheckBoxSelected = settingsComponent!!.provideTestSamplesCheckBoxSelected
        settingsState.defaultLLMRequests = settingsComponent!!.defaultLLMRequests
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
