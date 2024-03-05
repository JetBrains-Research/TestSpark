package org.jetbrains.research.testspark.settings.llm

import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import org.jetbrains.research.testspark.services.PromptParserService
import org.jetbrains.research.testspark.tools.llm.SettingsArguments
import javax.swing.JComponent

/**
 * This class allows to configure some LLM-related settings via the Large Language Model page in the Settings dialog,
 *   observes the changes and manages the UI and state.
 */
class SettingsLLMConfigurable : Configurable {

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
            if (settingsComponent!!.llmPlatforms[index].name == SettingsArguments.settingsState!!.openAIName) {
                settingsComponent!!.llmPlatforms[index].token = SettingsArguments.settingsState!!.openAIToken
                settingsComponent!!.llmPlatforms[index].model = SettingsArguments.settingsState!!.openAIModel
            }
            if (settingsComponent!!.llmPlatforms[index].name == SettingsArguments.settingsState!!.grazieName) {
                settingsComponent!!.llmPlatforms[index].token = SettingsArguments.settingsState!!.grazieToken
                settingsComponent!!.llmPlatforms[index].model = SettingsArguments.settingsState!!.grazieModel
            }
        }
        settingsComponent!!.currentLLMPlatformName = SettingsArguments.settingsState!!.currentLLMPlatformName
        settingsComponent!!.maxLLMRequest = SettingsArguments.settingsState!!.maxLLMRequest
        settingsComponent!!.maxPolyDepth = SettingsArguments.settingsState!!.maxPolyDepth
        settingsComponent!!.maxInputParamsDepth = SettingsArguments.settingsState!!.maxInputParamsDepth
        settingsComponent!!.classPrompt = SettingsArguments.settingsState!!.classPrompt
        settingsComponent!!.methodPrompt = SettingsArguments.settingsState!!.methodPrompt
        settingsComponent!!.linePrompt = SettingsArguments.settingsState!!.linePrompt
        settingsComponent!!.llmSetupCheckBoxSelected = SettingsArguments.settingsState!!.llmSetupCheckBoxSelected
        settingsComponent!!.provideTestSamplesCheckBoxSelected = SettingsArguments.settingsState!!.provideTestSamplesCheckBoxSelected

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
            if (settingsComponent!!.llmPlatforms[index].name == SettingsArguments.settingsState!!.openAIName) {
                modified = modified or (settingsComponent!!.llmPlatforms[index].token != SettingsArguments.settingsState!!.openAIToken)
                modified = modified or (settingsComponent!!.llmPlatforms[index].model != SettingsArguments.settingsState!!.openAIModel)
            }
            if (settingsComponent!!.llmPlatforms[index].name == SettingsArguments.settingsState!!.grazieName) {
                modified = modified or (settingsComponent!!.llmPlatforms[index].token != SettingsArguments.settingsState!!.grazieToken)
                modified = modified or (settingsComponent!!.llmPlatforms[index].model != SettingsArguments.settingsState!!.grazieModel)
            }
        }
        modified = modified or (settingsComponent!!.currentLLMPlatformName != SettingsArguments.settingsState!!.currentLLMPlatformName)
        modified = modified or (settingsComponent!!.maxLLMRequest != SettingsArguments.settingsState!!.maxLLMRequest)
        modified = modified or (settingsComponent!!.maxPolyDepth != SettingsArguments.settingsState!!.maxPolyDepth)
        modified = modified or (settingsComponent!!.maxInputParamsDepth != SettingsArguments.settingsState!!.maxInputParamsDepth)
        // class prompt
        modified = modified or (settingsComponent!!.classPrompt != SettingsArguments.settingsState!!.classPrompt)
        modified = modified and service<PromptParserService>().isPromptValid(settingsComponent!!.classPrompt)
        // method prompt
        modified = modified or (settingsComponent!!.methodPrompt != SettingsArguments.settingsState!!.methodPrompt)
        modified = modified and service<PromptParserService>().isPromptValid(settingsComponent!!.methodPrompt)
        // line prompt
        modified = modified or (settingsComponent!!.linePrompt != SettingsArguments.settingsState!!.linePrompt)
        modified = modified and service<PromptParserService>().isPromptValid(settingsComponent!!.linePrompt)

        modified = modified or (settingsComponent!!.llmSetupCheckBoxSelected != SettingsArguments.settingsState!!.llmSetupCheckBoxSelected)
        modified = modified or (settingsComponent!!.provideTestSamplesCheckBoxSelected != SettingsArguments.settingsState!!.provideTestSamplesCheckBoxSelected)

        return modified
    }

    /**
     * Persists the modified state after a user hit Apply button.
     */
    override fun apply() {
        for (index in settingsComponent!!.llmPlatforms.indices) {
            if (settingsComponent!!.llmPlatforms[index].name == SettingsArguments.settingsState!!.openAIName) {
                SettingsArguments.settingsState!!.openAIToken = settingsComponent!!.llmPlatforms[index].token
                SettingsArguments.settingsState!!.openAIModel = settingsComponent!!.llmPlatforms[index].model
            }
            if (settingsComponent!!.llmPlatforms[index].name == SettingsArguments.settingsState!!.grazieName) {
                SettingsArguments.settingsState!!.grazieToken = settingsComponent!!.llmPlatforms[index].token
                SettingsArguments.settingsState!!.grazieModel = settingsComponent!!.llmPlatforms[index].model
            }
        }
        SettingsArguments.settingsState!!.currentLLMPlatformName = settingsComponent!!.currentLLMPlatformName
        SettingsArguments.settingsState!!.maxLLMRequest = settingsComponent!!.maxLLMRequest
        SettingsArguments.settingsState!!.maxPolyDepth = settingsComponent!!.maxPolyDepth
        SettingsArguments.settingsState!!.maxInputParamsDepth = settingsComponent!!.maxInputParamsDepth
        SettingsArguments.settingsState!!.classPrompt = settingsComponent!!.classPrompt
        SettingsArguments.settingsState!!.methodPrompt = settingsComponent!!.methodPrompt
        SettingsArguments.settingsState!!.linePrompt = settingsComponent!!.linePrompt
        SettingsArguments.settingsState!!.llmSetupCheckBoxSelected = settingsComponent!!.llmSetupCheckBoxSelected
        SettingsArguments.settingsState!!.provideTestSamplesCheckBoxSelected = settingsComponent!!.provideTestSamplesCheckBoxSelected
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
