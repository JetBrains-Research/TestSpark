package org.jetbrains.research.testspark.settings

import com.intellij.util.xmlb.annotations.OptionTag
import org.jetbrains.research.testspark.bundles.evosuite.EvoSuiteDefaultsBundle
import org.jetbrains.research.testspark.bundles.llm.LLMDefaultsBundle
import org.jetbrains.research.testspark.core.data.JUnitVersion
import org.jetbrains.research.testspark.data.JUnitVersionConverter
import org.jetbrains.research.testspark.data.evosuite.ContentDigestAlgorithm

/**
 * This class is the actual data class that stores the values of the EvoSuite Settings entries.
 */
data class SettingsApplicationState(
    var javaPath: String = DefaultSettingsApplicationState.javaPath,
    var sandbox: Boolean = DefaultSettingsApplicationState.sandbox,
    var assertions: Boolean = DefaultSettingsApplicationState.assertions,
    var seed: String = DefaultSettingsApplicationState.seed,
    var algorithm: ContentDigestAlgorithm = DefaultSettingsApplicationState.algorithm,
    var configurationId: String = DefaultSettingsApplicationState.configurationId,
    var clientOnThread: Boolean = DefaultSettingsApplicationState.clientOnThread,
    var junitCheck: Boolean = DefaultSettingsApplicationState.junitCheck,
    var criterionLine: Boolean = DefaultSettingsApplicationState.criterionLine,
    var criterionBranch: Boolean = DefaultSettingsApplicationState.criterionBranch,
    var criterionException: Boolean = DefaultSettingsApplicationState.criterionException,
    var criterionWeakMutation: Boolean = DefaultSettingsApplicationState.criterionWeakMutation,
    var criterionOutput: Boolean = DefaultSettingsApplicationState.criterionOutput,
    var criterionMethod: Boolean = DefaultSettingsApplicationState.criterionMethod,
    var criterionMethodNoException: Boolean = DefaultSettingsApplicationState.criterionMethodNoException,
    var criterionCBranch: Boolean = DefaultSettingsApplicationState.criterionCBranch,
    var minimize: Boolean = DefaultSettingsApplicationState.minimize,
    var openAIName: String = DefaultSettingsApplicationState.openAIName,
    var openAIToken: String = DefaultSettingsApplicationState.openAIToken,
    var openAIModel: String = DefaultSettingsApplicationState.openAIModel,
    var grazieName: String = DefaultSettingsApplicationState.grazieName,
    var grazieToken: String = DefaultSettingsApplicationState.grazieToken,
    var grazieModel: String = DefaultSettingsApplicationState.grazieModel,
    var currentLLMPlatformName: String = DefaultSettingsApplicationState.currentLLMPlatformName,
    var maxLLMRequest: Int = DefaultSettingsApplicationState.maxLLMRequest,
    var maxInputParamsDepth: Int = DefaultSettingsApplicationState.maxInputParamsDepth,
    var maxPolyDepth: Int = DefaultSettingsApplicationState.maxPolyDepth,
    var classPrompts: String = DefaultSettingsApplicationState.classPrompts,
    var methodPrompts: String = DefaultSettingsApplicationState.methodPrompts,
    var linePrompts: String = DefaultSettingsApplicationState.linePrompts,
    var classPromptNames: String = DefaultSettingsApplicationState.classPromptNames,
    var methodPromptNames: String = DefaultSettingsApplicationState.methodPromptNames,
    var linePromptNames: String = DefaultSettingsApplicationState.linePromptNames,
    var classCurrentDefaultPromptIndex: Int = DefaultSettingsApplicationState.classCurrentDefaultPromptIndex,
    var methodCurrentDefaultPromptIndex: Int = DefaultSettingsApplicationState.methodCurrentDefaultPromptIndex,
    var lineCurrentDefaultPromptIndex: Int = DefaultSettingsApplicationState.lineCurrentDefaultPromptIndex,
    var defaultLLMRequests: String = DefaultSettingsApplicationState.defaultLLMRequests,
    @OptionTag(converter = JUnitVersionConverter::class) var junitVersion: JUnitVersion = DefaultSettingsApplicationState.junitVersion,
    var junitVersionPriorityCheckBoxSelected: Boolean = DefaultSettingsApplicationState.junitVersionPriorityCheckBoxSelected,
    var provideTestSamplesCheckBoxSelected: Boolean = DefaultSettingsApplicationState.provideTestSamplesCheckBoxSelected,
    var llmSetupCheckBoxSelected: Boolean = DefaultSettingsApplicationState.llmSetupCheckBoxSelected,
    var evosuiteSetupCheckBoxSelected: Boolean = DefaultSettingsApplicationState.evosuiteSetupCheckBoxSelected,
    var evosuitePort: String = DefaultSettingsApplicationState.evosuitePort,
) {

    /**
     * Default values of SettingsApplicationState.
     */
    object DefaultSettingsApplicationState {
        var javaPath: String = EvoSuiteDefaultsBundle.get("javaPath")
        val sandbox: Boolean = EvoSuiteDefaultsBundle.get("sandbox").toBoolean()
        val assertions: Boolean = EvoSuiteDefaultsBundle.get("assertions").toBoolean()
        val seed: String = EvoSuiteDefaultsBundle.get("seed")
        val junitCheck: Boolean = EvoSuiteDefaultsBundle.get("junitCheck").toBoolean()
        val minimize: Boolean = EvoSuiteDefaultsBundle.get("minimize").toBoolean()
        val algorithm: ContentDigestAlgorithm = ContentDigestAlgorithm.DYNAMOSA
        val configurationId: String = EvoSuiteDefaultsBundle.get("configurationId")
        val clientOnThread: Boolean = EvoSuiteDefaultsBundle.get("clientOnThread").toBoolean()
        val criterionLine: Boolean = EvoSuiteDefaultsBundle.get("criterionLine").toBoolean()
        val criterionBranch: Boolean = EvoSuiteDefaultsBundle.get("criterionBranch").toBoolean()
        val criterionException: Boolean = EvoSuiteDefaultsBundle.get("criterionException").toBoolean()
        val criterionWeakMutation: Boolean = EvoSuiteDefaultsBundle.get("criterionWeakMutation").toBoolean()
        val criterionOutput: Boolean = EvoSuiteDefaultsBundle.get("criterionOutput").toBoolean()
        val criterionMethod: Boolean = EvoSuiteDefaultsBundle.get("criterionMethod").toBoolean()
        val criterionMethodNoException: Boolean = EvoSuiteDefaultsBundle.get("criterionMethodNoException").toBoolean()
        val criterionCBranch: Boolean = EvoSuiteDefaultsBundle.get("criterionCBranch").toBoolean()
        val openAIName: String = LLMDefaultsBundle.get("openAIName")
        val openAIToken: String = LLMDefaultsBundle.get("openAIToken")
        val openAIModel: String = LLMDefaultsBundle.get("openAIModel")
        val grazieName: String = LLMDefaultsBundle.get("grazieName")
        val grazieToken: String = LLMDefaultsBundle.get("grazieToken")
        val grazieModel: String = LLMDefaultsBundle.get("grazieModel")
        var currentLLMPlatformName: String = LLMDefaultsBundle.get("openAIName")
        val maxLLMRequest: Int = LLMDefaultsBundle.get("maxLLMRequest").toInt()
        val maxInputParamsDepth: Int = LLMDefaultsBundle.get("maxInputParamsDepth").toInt()
        val maxPolyDepth: Int = LLMDefaultsBundle.get("maxPolyDepth").toInt()
        val classPrompts: String = LLMDefaultsBundle.get("classPrompt")
        val methodPrompts: String = LLMDefaultsBundle.get("methodPrompt")
        val linePrompts: String = LLMDefaultsBundle.get("linePrompt")
        var classPromptNames = LLMDefaultsBundle.get("classPromptName")
        var methodPromptNames = LLMDefaultsBundle.get("methodPromptName")
        var linePromptNames = LLMDefaultsBundle.get("linePromptName")
        var classCurrentDefaultPromptIndex = LLMDefaultsBundle.get("classCurrentDefaultPromptIndex").toInt()
        var methodCurrentDefaultPromptIndex = LLMDefaultsBundle.get("methodCurrentDefaultPromptIndex").toInt()
        var lineCurrentDefaultPromptIndex = LLMDefaultsBundle.get("lineCurrentDefaultPromptIndex").toInt()
        val defaultLLMRequests: String = LLMDefaultsBundle.get("defaultLLMRequests")
        val junitVersion: JUnitVersion = JUnitVersion.JUnit5
        val junitVersionPriorityCheckBoxSelected: Boolean = LLMDefaultsBundle.get("junitVersionPriority").toBoolean()
        val provideTestSamplesCheckBoxSelected: Boolean = LLMDefaultsBundle.get("provideTestSamples").toBoolean()
        val llmSetupCheckBoxSelected: Boolean = LLMDefaultsBundle.get("provideTestSamples").toBoolean()
        val evosuiteSetupCheckBoxSelected: Boolean = LLMDefaultsBundle.get("provideTestSamples").toBoolean()
        val evosuitePort: String = EvoSuiteDefaultsBundle.get("evosuitePort")
    }

    // TODO remove from here
    fun serializeChangesFromDefault(): List<String> {
        val params = mutableListOf<String>()
        // Parameters from settings menu
        if (this.sandbox != DefaultSettingsApplicationState.sandbox) {
            params.add("-Dsandbox=${this.sandbox}")
        }
        if (this.assertions != DefaultSettingsApplicationState.assertions) {
            params.add("-Dassertions=${this.assertions}")
        }
        params.add("-Dalgorithm=${this.algorithm}")
        if (this.junitCheck != DefaultSettingsApplicationState.junitCheck) {
            params.add("-Djunit_check=${this.junitCheck}")
        }
        if (this.minimize != DefaultSettingsApplicationState.minimize) {
            params.add("-Dminimize=${this.minimize}")
        }
        return params
    }
}
