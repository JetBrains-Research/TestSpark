package org.jetbrains.research.testspark.settings.llm

import com.intellij.util.xmlb.annotations.OptionTag
import org.jetbrains.research.testspark.bundles.DefaultsBundle
import org.jetbrains.research.testspark.core.data.JUnitVersion
import org.jetbrains.research.testspark.data.JUnitVersionConverter

/**
 * This class is the actual data class that stores the values of the LLM Settings entries.
 */
data class LLMSettingsState(
    var openAIName: String = DefaultLLMSettingsState.openAIName,
    var openAIToken: String = DefaultLLMSettingsState.openAIToken,
    var openAIModel: String = DefaultLLMSettingsState.openAIModel,
    var grazieName: String = DefaultLLMSettingsState.grazieName,
    var grazieToken: String = DefaultLLMSettingsState.grazieToken,
    var grazieModel: String = DefaultLLMSettingsState.grazieModel,
    var currentLLMPlatformName: String = DefaultLLMSettingsState.currentLLMPlatformName,
    var maxLLMRequest: Int = DefaultLLMSettingsState.maxLLMRequest,
    var maxInputParamsDepth: Int = DefaultLLMSettingsState.maxInputParamsDepth,
    var maxPolyDepth: Int = DefaultLLMSettingsState.maxPolyDepth,
    var classPrompts: String = DefaultLLMSettingsState.classPrompts,
    var methodPrompts: String = DefaultLLMSettingsState.methodPrompts,
    var linePrompts: String = DefaultLLMSettingsState.linePrompts,
    var classPromptNames: String = DefaultLLMSettingsState.classPromptNames,
    var methodPromptNames: String = DefaultLLMSettingsState.methodPromptNames,
    var linePromptNames: String = DefaultLLMSettingsState.linePromptNames,
    var classCurrentDefaultPromptIndex: Int = DefaultLLMSettingsState.classCurrentDefaultPromptIndex,
    var methodCurrentDefaultPromptIndex: Int = DefaultLLMSettingsState.methodCurrentDefaultPromptIndex,
    var lineCurrentDefaultPromptIndex: Int = DefaultLLMSettingsState.lineCurrentDefaultPromptIndex,
    var defaultLLMRequests: String = DefaultLLMSettingsState.defaultLLMRequests,
    @OptionTag(converter = JUnitVersionConverter::class) var junitVersion: JUnitVersion = DefaultLLMSettingsState.junitVersion,
    var junitVersionPriorityCheckBoxSelected: Boolean = DefaultLLMSettingsState.junitVersionPriorityCheckBoxSelected,
    var provideTestSamplesCheckBoxSelected: Boolean = DefaultLLMSettingsState.provideTestSamplesCheckBoxSelected,
    var llmSetupCheckBoxSelected: Boolean = DefaultLLMSettingsState.llmSetupCheckBoxSelected,
) {

    /**
     * Default values of SettingsLLMState.
     */
    object DefaultLLMSettingsState {
        val openAIName: String = DefaultsBundle.defaultValue("openAIName")
        val openAIToken: String = DefaultsBundle.defaultValue("openAIToken")
        val openAIModel: String = DefaultsBundle.defaultValue("openAIModel")
        val grazieName: String = DefaultsBundle.defaultValue("grazieName")
        val grazieToken: String = DefaultsBundle.defaultValue("grazieToken")
        val grazieModel: String = DefaultsBundle.defaultValue("grazieModel")
        var currentLLMPlatformName: String = DefaultsBundle.defaultValue("openAIName")
        val maxLLMRequest: Int = DefaultsBundle.defaultValue("maxLLMRequest").toInt()
        val maxInputParamsDepth: Int = DefaultsBundle.defaultValue("maxInputParamsDepth").toInt()
        val maxPolyDepth: Int = DefaultsBundle.defaultValue("maxPolyDepth").toInt()
        val classPrompts: String = DefaultsBundle.defaultValue("classPrompt")
        val methodPrompts: String = DefaultsBundle.defaultValue("methodPrompt")
        val linePrompts: String = DefaultsBundle.defaultValue("linePrompt")
        var classPromptNames = DefaultsBundle.defaultValue("classPromptName")
        var methodPromptNames = DefaultsBundle.defaultValue("methodPromptName")
        var linePromptNames = DefaultsBundle.defaultValue("linePromptName")
        var classCurrentDefaultPromptIndex = DefaultsBundle.defaultValue("classCurrentDefaultPromptIndex").toInt()
        var methodCurrentDefaultPromptIndex = DefaultsBundle.defaultValue("methodCurrentDefaultPromptIndex").toInt()
        var lineCurrentDefaultPromptIndex = DefaultsBundle.defaultValue("lineCurrentDefaultPromptIndex").toInt()
        val defaultLLMRequests: String = DefaultsBundle.defaultValue("defaultLLMRequests")
        val junitVersion: JUnitVersion = JUnitVersion.JUnit5
        val junitVersionPriorityCheckBoxSelected: Boolean = DefaultsBundle.defaultValue("junitVersionPriority").toBoolean()
        val provideTestSamplesCheckBoxSelected: Boolean = DefaultsBundle.defaultValue("provideTestSamples").toBoolean()
        val llmSetupCheckBoxSelected: Boolean = DefaultsBundle.defaultValue("llmSetup").toBoolean()
    }
}
