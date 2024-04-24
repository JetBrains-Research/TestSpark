package org.jetbrains.research.testspark.settings.llm

import com.intellij.util.xmlb.annotations.OptionTag
import org.jetbrains.research.testspark.bundles.llm.LLMDefaultsBundle
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
        val openAIName: String = LLMDefaultsBundle.defaultValue("openAIName")
        val openAIToken: String = LLMDefaultsBundle.defaultValue("openAIToken")
        val openAIModel: String = LLMDefaultsBundle.defaultValue("openAIModel")
        val grazieName: String = LLMDefaultsBundle.defaultValue("grazieName")
        val grazieToken: String = LLMDefaultsBundle.defaultValue("grazieToken")
        val grazieModel: String = LLMDefaultsBundle.defaultValue("grazieModel")
        var currentLLMPlatformName: String = LLMDefaultsBundle.defaultValue("openAIName")
        val maxLLMRequest: Int = LLMDefaultsBundle.defaultValue("maxLLMRequest").toInt()
        val maxInputParamsDepth: Int = LLMDefaultsBundle.defaultValue("maxInputParamsDepth").toInt()
        val maxPolyDepth: Int = LLMDefaultsBundle.defaultValue("maxPolyDepth").toInt()
        val classPrompts: String = LLMDefaultsBundle.defaultValue("classPrompt")
        val methodPrompts: String = LLMDefaultsBundle.defaultValue("methodPrompt")
        val linePrompts: String = LLMDefaultsBundle.defaultValue("linePrompt")
        var classPromptNames = LLMDefaultsBundle.defaultValue("classPromptName")
        var methodPromptNames = LLMDefaultsBundle.defaultValue("methodPromptName")
        var linePromptNames = LLMDefaultsBundle.defaultValue("linePromptName")
        var classCurrentDefaultPromptIndex = LLMDefaultsBundle.defaultValue("classCurrentDefaultPromptIndex").toInt()
        var methodCurrentDefaultPromptIndex = LLMDefaultsBundle.defaultValue("methodCurrentDefaultPromptIndex").toInt()
        var lineCurrentDefaultPromptIndex = LLMDefaultsBundle.defaultValue("lineCurrentDefaultPromptIndex").toInt()
        val defaultLLMRequests: String = LLMDefaultsBundle.defaultValue("defaultLLMRequests")
        val junitVersion: JUnitVersion = JUnitVersion.JUnit5
        val junitVersionPriorityCheckBoxSelected: Boolean = LLMDefaultsBundle.defaultValue("junitVersionPriority").toBoolean()
        val provideTestSamplesCheckBoxSelected: Boolean = LLMDefaultsBundle.defaultValue("provideTestSamples").toBoolean()
        val llmSetupCheckBoxSelected: Boolean = LLMDefaultsBundle.defaultValue("llmSetup").toBoolean()
    }
}
