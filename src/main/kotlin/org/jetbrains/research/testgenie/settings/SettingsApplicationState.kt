package org.jetbrains.research.testgenie.settings

import org.jetbrains.research.testgenie.TestGenieDefaultsBundle

/**
 * This class is the actual data class that stores the values of the EvoSuite Settings entries.
 */
data class SettingsApplicationState(
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
    var grazieUserToken: String = DefaultSettingsApplicationState.grazieUserToken
) {

    /**
     * Default values of SettingsApplicationState.
     */
    object DefaultSettingsApplicationState {
        val sandbox: Boolean = TestGenieDefaultsBundle.defaultValue("sandbox").toBoolean()
        val assertions: Boolean = TestGenieDefaultsBundle.defaultValue("assertions").toBoolean()
        val seed: String = TestGenieDefaultsBundle.defaultValue("seed")
        val junitCheck: Boolean = TestGenieDefaultsBundle.defaultValue("junitCheck").toBoolean()
        val minimize: Boolean = TestGenieDefaultsBundle.defaultValue("minimize").toBoolean()
        val algorithm: ContentDigestAlgorithm = ContentDigestAlgorithm.DYNAMOSA
        val configurationId: String = TestGenieDefaultsBundle.defaultValue("configurationId")
        val clientOnThread: Boolean = TestGenieDefaultsBundle.defaultValue("clientOnThread").toBoolean()
        val criterionLine: Boolean = TestGenieDefaultsBundle.defaultValue("criterionLine").toBoolean()
        val criterionBranch: Boolean = TestGenieDefaultsBundle.defaultValue("criterionBranch").toBoolean()
        val criterionException: Boolean = TestGenieDefaultsBundle.defaultValue("criterionException").toBoolean()
        val criterionWeakMutation: Boolean = TestGenieDefaultsBundle.defaultValue("criterionWeakMutation").toBoolean()
        val criterionOutput: Boolean = TestGenieDefaultsBundle.defaultValue("criterionOutput").toBoolean()
        val criterionMethod: Boolean = TestGenieDefaultsBundle.defaultValue("criterionMethod").toBoolean()
        val criterionMethodNoException: Boolean = TestGenieDefaultsBundle.defaultValue("criterionMethodNoException").toBoolean()
        val criterionCBranch: Boolean = TestGenieDefaultsBundle.defaultValue("criterionCBranch").toBoolean()
        val grazieUserToken: String = TestGenieDefaultsBundle.defaultValue("grazieToken")
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
