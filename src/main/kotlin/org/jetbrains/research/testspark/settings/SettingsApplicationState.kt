package org.jetbrains.research.testspark.settings

import org.jetbrains.research.testspark.TestSparkDefaultsBundle

/**
 * This class is the actual data class that stores the values of the EvoSuite Settings entries.
 */
data class SettingsApplicationState(
    var javaPath: String = SettingsProjectState.DefaultSettingsPluginState.javaPath,
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
    var llmUserToken: String = DefaultSettingsApplicationState.llmUserToken,
    var model: String = DefaultSettingsApplicationState.model,
    var maxLLMRequest: Int = DefaultSettingsApplicationState.maxLLMRequest,
    var maxInputParamsDepth: Int = DefaultSettingsApplicationState.maxInputParamsDepth,
    var maxPolyDepth: Int = DefaultSettingsApplicationState.maxPolyDepth,
) {

    /**
     * Default values of SettingsApplicationState.
     */
    object DefaultSettingsApplicationState {
        val sandbox: Boolean = org.jetbrains.research.testspark.TestSparkDefaultsBundle.defaultValue("sandbox").toBoolean()
        val assertions: Boolean = org.jetbrains.research.testspark.TestSparkDefaultsBundle.defaultValue("assertions").toBoolean()
        val seed: String = org.jetbrains.research.testspark.TestSparkDefaultsBundle.defaultValue("seed")
        val junitCheck: Boolean = org.jetbrains.research.testspark.TestSparkDefaultsBundle.defaultValue("junitCheck").toBoolean()
        val minimize: Boolean = org.jetbrains.research.testspark.TestSparkDefaultsBundle.defaultValue("minimize").toBoolean()
        val algorithm: ContentDigestAlgorithm = ContentDigestAlgorithm.DYNAMOSA
        val configurationId: String = org.jetbrains.research.testspark.TestSparkDefaultsBundle.defaultValue("configurationId")
        val clientOnThread: Boolean = org.jetbrains.research.testspark.TestSparkDefaultsBundle.defaultValue("clientOnThread").toBoolean()
        val criterionLine: Boolean = org.jetbrains.research.testspark.TestSparkDefaultsBundle.defaultValue("criterionLine").toBoolean()
        val criterionBranch: Boolean = org.jetbrains.research.testspark.TestSparkDefaultsBundle.defaultValue("criterionBranch").toBoolean()
        val criterionException: Boolean = org.jetbrains.research.testspark.TestSparkDefaultsBundle.defaultValue("criterionException").toBoolean()
        val criterionWeakMutation: Boolean = org.jetbrains.research.testspark.TestSparkDefaultsBundle.defaultValue("criterionWeakMutation").toBoolean()
        val criterionOutput: Boolean = org.jetbrains.research.testspark.TestSparkDefaultsBundle.defaultValue("criterionOutput").toBoolean()
        val criterionMethod: Boolean = org.jetbrains.research.testspark.TestSparkDefaultsBundle.defaultValue("criterionMethod").toBoolean()
        val criterionMethodNoException: Boolean = org.jetbrains.research.testspark.TestSparkDefaultsBundle.defaultValue("criterionMethodNoException").toBoolean()
        val criterionCBranch: Boolean = org.jetbrains.research.testspark.TestSparkDefaultsBundle.defaultValue("criterionCBranch").toBoolean()
        val llmUserToken: String = org.jetbrains.research.testspark.TestSparkDefaultsBundle.defaultValue("llmToken")
        var model: String = org.jetbrains.research.testspark.TestSparkDefaultsBundle.defaultValue("model")
        val maxLLMRequest: Int = org.jetbrains.research.testspark.TestSparkDefaultsBundle.defaultValue("maxLLMRequest").toInt()
        val maxInputParamsDepth: Int = org.jetbrains.research.testspark.TestSparkDefaultsBundle.defaultValue("maxInputParamsDepth").toInt()
        val maxPolyDepth: Int = org.jetbrains.research.testspark.TestSparkDefaultsBundle.defaultValue("maxPolyDepth").toInt()
    }

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

enum class ContentDigestAlgorithm {
    // random
    RANDOM_SEARCH,

    // GAs
    STANDARD_GA, MONOTONIC_GA, STEADY_STATE_GA, BREEDER_GA, CELLULAR_GA, STANDARD_CHEMICAL_REACTION, MAP_ELITES,

    // mu-lambda
    ONE_PLUS_LAMBDA_LAMBDA_GA, ONE_PLUS_ONE_EA, MU_PLUS_LAMBDA_EA, MU_LAMBDA_EA,

    // many-objective algorithms
    MOSA, DYNAMOSA, LIPS, MIO,

    // multiple-objective optimisation algorithms
    NSGAII, SPEA2
}
