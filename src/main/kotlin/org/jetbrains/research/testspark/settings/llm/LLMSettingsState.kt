package org.jetbrains.research.testspark.settings.llm

import com.intellij.util.xmlb.annotations.OptionTag
import org.jetbrains.research.testspark.bundles.DefaultsBundle
import org.jetbrains.research.testspark.core.data.JUnitVersion
import org.jetbrains.research.testspark.data.JUnitVersionConverter

/**
 * This class is the actual data class that stores the values of the LLM Settings entries.
 */
data class LLMSettingsState(
    var openAIName: String = DefaultSettingsLLMState.openAIName,
    var openAIToken: String = DefaultSettingsLLMState.openAIToken,
    var openAIModel: String = DefaultSettingsLLMState.openAIModel,
    var grazieName: String = DefaultSettingsLLMState.grazieName,
    var grazieToken: String = DefaultSettingsLLMState.grazieToken,
    var grazieModel: String = DefaultSettingsLLMState.grazieModel,
    var currentLLMPlatformName: String = DefaultSettingsLLMState.currentLLMPlatformName,
    var maxLLMRequest: Int = DefaultSettingsLLMState.maxLLMRequest,
    var maxInputParamsDepth: Int = DefaultSettingsLLMState.maxInputParamsDepth,
    var maxPolyDepth: Int = DefaultSettingsLLMState.maxPolyDepth,
    var classPrompts: String = DefaultSettingsLLMState.classPrompts,
    var methodPrompts: String = DefaultSettingsLLMState.methodPrompts,
    var linePrompts: String = DefaultSettingsLLMState.linePrompts,
    var classPromptNames: String = DefaultSettingsLLMState.classPromptNames,
    var methodPromptNames: String = DefaultSettingsLLMState.methodPromptNames,
    var linePromptNames: String = DefaultSettingsLLMState.linePromptNames,
    var classCurrentDefaultPromptIndex: Int = DefaultSettingsLLMState.classCurrentDefaultPromptIndex,
    var methodCurrentDefaultPromptIndex: Int = DefaultSettingsLLMState.methodCurrentDefaultPromptIndex,
    var lineCurrentDefaultPromptIndex: Int = DefaultSettingsLLMState.lineCurrentDefaultPromptIndex,
    var defaultLLMRequests: String = DefaultSettingsLLMState.defaultLLMRequests,
    @OptionTag(converter = JUnitVersionConverter::class) var junitVersion: JUnitVersion = DefaultSettingsLLMState.junitVersion,
    var junitVersionPriorityCheckBoxSelected: Boolean = DefaultSettingsLLMState.junitVersionPriorityCheckBoxSelected,
    var provideTestSamplesCheckBoxSelected: Boolean = DefaultSettingsLLMState.provideTestSamplesCheckBoxSelected,
    var llmSetupCheckBoxSelected: Boolean = DefaultSettingsLLMState.llmSetupCheckBoxSelected,
) {

    /**
     * Default values of SettingsLLMState.
     */
    object DefaultSettingsLLMState {
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
