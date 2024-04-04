package org.jetbrains.research.testspark.settings

import com.intellij.util.xmlb.annotations.OptionTag
import org.jetbrains.research.testspark.bundles.TestSparkDefaultsBundle
import org.jetbrains.research.testspark.core.data.JUnitVersion
import org.jetbrains.research.testspark.data.ContentDigestAlgorithm
import org.jetbrains.research.testspark.data.JUnitVersionConverter

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
        var javaPath: String = TestSparkDefaultsBundle.defaultValue("javaPath")
        val sandbox: Boolean = TestSparkDefaultsBundle.defaultValue("sandbox").toBoolean()
        val assertions: Boolean = TestSparkDefaultsBundle.defaultValue("assertions").toBoolean()
        val seed: String = TestSparkDefaultsBundle.defaultValue("seed")
        val junitCheck: Boolean = TestSparkDefaultsBundle.defaultValue("junitCheck").toBoolean()
        val minimize: Boolean = TestSparkDefaultsBundle.defaultValue("minimize").toBoolean()
        val algorithm: ContentDigestAlgorithm = ContentDigestAlgorithm.DYNAMOSA
        val configurationId: String = TestSparkDefaultsBundle.defaultValue("configurationId")
        val clientOnThread: Boolean = TestSparkDefaultsBundle.defaultValue("clientOnThread").toBoolean()
        val criterionLine: Boolean = TestSparkDefaultsBundle.defaultValue("criterionLine").toBoolean()
        val criterionBranch: Boolean = TestSparkDefaultsBundle.defaultValue("criterionBranch").toBoolean()
        val criterionException: Boolean = TestSparkDefaultsBundle.defaultValue("criterionException").toBoolean()
        val criterionWeakMutation: Boolean = TestSparkDefaultsBundle.defaultValue("criterionWeakMutation").toBoolean()
        val criterionOutput: Boolean = TestSparkDefaultsBundle.defaultValue("criterionOutput").toBoolean()
        val criterionMethod: Boolean = TestSparkDefaultsBundle.defaultValue("criterionMethod").toBoolean()
        val criterionMethodNoException: Boolean = TestSparkDefaultsBundle.defaultValue("criterionMethodNoException").toBoolean()
        val criterionCBranch: Boolean = TestSparkDefaultsBundle.defaultValue("criterionCBranch").toBoolean()
        val openAIName: String = TestSparkDefaultsBundle.defaultValue("openAIName")
        val openAIToken: String = TestSparkDefaultsBundle.defaultValue("openAIToken")
        val openAIModel: String = TestSparkDefaultsBundle.defaultValue("openAIModel")
        val grazieName: String = TestSparkDefaultsBundle.defaultValue("grazieName")
        val grazieToken: String = TestSparkDefaultsBundle.defaultValue("grazieToken")
        val grazieModel: String = TestSparkDefaultsBundle.defaultValue("grazieModel")
        var currentLLMPlatformName: String = TestSparkDefaultsBundle.defaultValue("openAIName")
        val maxLLMRequest: Int = TestSparkDefaultsBundle.defaultValue("maxLLMRequest").toInt()
        val maxInputParamsDepth: Int = TestSparkDefaultsBundle.defaultValue("maxInputParamsDepth").toInt()
        val maxPolyDepth: Int = TestSparkDefaultsBundle.defaultValue("maxPolyDepth").toInt()
        val classPrompts: String = TestSparkDefaultsBundle.defaultValue("classPrompt")
        val methodPrompts: String = TestSparkDefaultsBundle.defaultValue("methodPrompt")
        val linePrompts: String = TestSparkDefaultsBundle.defaultValue("linePrompt")
        var classPromptNames = TestSparkDefaultsBundle.defaultValue("classPromptName")
        var methodPromptNames = TestSparkDefaultsBundle.defaultValue("methodPromptName")
        var linePromptNames = TestSparkDefaultsBundle.defaultValue("linePromptName")
        var classCurrentDefaultPromptIndex = TestSparkDefaultsBundle.defaultValue("classCurrentDefaultPromptIndex").toInt()
        var methodCurrentDefaultPromptIndex = TestSparkDefaultsBundle.defaultValue("methodCurrentDefaultPromptIndex").toInt()
        var lineCurrentDefaultPromptIndex = TestSparkDefaultsBundle.defaultValue("lineCurrentDefaultPromptIndex").toInt()
        val defaultLLMRequests: String = TestSparkDefaultsBundle.defaultValue("defaultLLMRequests")
        val junitVersion: JUnitVersion = JUnitVersion.JUnit5
        val junitVersionPriorityCheckBoxSelected: Boolean = TestSparkDefaultsBundle.defaultValue("junitVersionPriority").toBoolean()
        val provideTestSamplesCheckBoxSelected: Boolean = TestSparkDefaultsBundle.defaultValue("provideTestSamples").toBoolean()
        val llmSetupCheckBoxSelected: Boolean = TestSparkDefaultsBundle.defaultValue("provideTestSamples").toBoolean()
        val evosuiteSetupCheckBoxSelected: Boolean = TestSparkDefaultsBundle.defaultValue("provideTestSamples").toBoolean()
        val evosuitePort: String = TestSparkDefaultsBundle.defaultValue("evosuitePort")
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
