package nl.tudelft.ewi.se.ciselab.testgenie.settings

import nl.tudelft.ewi.se.ciselab.testgenie.TestGenieDefaultsBundle

/**
 * This class is the actual data class that stores the values of the Settings entries.
 */
data class TestGenieSettingsState(
    var javaPath: String = DefaultSettingsState.javaPath,

    var sandbox: Boolean = DefaultSettingsState.sandbox,
    var assertions: Boolean = DefaultSettingsState.assertions,
    var seed: String = DefaultSettingsState.seed,
    var algorithm: ContentDigestAlgorithm = DefaultSettingsState.algorithm,
    var configurationId: String = DefaultSettingsState.configurationId,
    var clientOnThread: Boolean = DefaultSettingsState.clientOnThread,
    var junitCheck: Boolean = DefaultSettingsState.junitCheck,
    var criterionLine: Boolean = DefaultSettingsState.criterionLine,
    var criterionBranch: Boolean = DefaultSettingsState.criterionBranch,
    var criterionException: Boolean = DefaultSettingsState.criterionException,
    var criterionWeakMutation: Boolean = DefaultSettingsState.criterionWeakMutation,
    var criterionOutput: Boolean = DefaultSettingsState.criterionOutput,
    var criterionMethod: Boolean = DefaultSettingsState.criterionMethod,
    var criterionMethodNoException: Boolean = DefaultSettingsState.criterionMethodNoException,
    var criterionCBranch: Boolean = DefaultSettingsState.criterionCBranch,
    var minimize: Boolean = DefaultSettingsState.minimize,
    var colorRed: Int = DefaultSettingsState.colorRed,
    var colorGreen: Int = DefaultSettingsState.colorGreen,
    var colorBlue: Int = DefaultSettingsState.colorBlue,
    var buildPath: String = DefaultSettingsState.buildPath,
    var buildCommand: String = DefaultSettingsState.buildCommand,
    var telemetryEnabled: Boolean = DefaultSettingsState.telemetryEnabled,
    var telemetryPath: String = DefaultSettingsState.telemetryPath
) {

    object DefaultSettingsState {
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

        val javaPath: String = TestGenieDefaultsBundle.defaultValue("javaPath")
        val colorRed: Int = TestGenieDefaultsBundle.defaultValue("colorRed").toInt()
        val colorGreen: Int = TestGenieDefaultsBundle.defaultValue("colorGreen").toInt()
        val colorBlue: Int = TestGenieDefaultsBundle.defaultValue("colorBlue").toInt()
        val buildPath: String = TestGenieDefaultsBundle.defaultValue("buildPath")
        val buildCommand: String = TestGenieDefaultsBundle.defaultValue("buildCommand")
        val telemetryEnabled: Boolean = TestGenieDefaultsBundle.defaultValue("telemetryEnabled").toBoolean()
        val telemetryPath: String = System.getProperty("user.home")
    }

    fun serializeChangesFromDefault(): List<String> {
        val params = mutableListOf<String>()
        // Parameters from settings menu
        if (this.sandbox != DefaultSettingsState.sandbox) {
            params.add("-Dsandbox=${this.sandbox}")
        }
        if (this.assertions != DefaultSettingsState.assertions) {
            params.add("-Dassertions=${this.assertions}")
        }
        if (this.junitCheck != DefaultSettingsState.junitCheck) {
            params.add("-Djunit_check=${this.junitCheck}")
        }
        if (this.minimize != DefaultSettingsState.minimize) {
            params.add("-Dminimize=${this.minimize}")
        }
        params.add("-Dalgorithm=${this.algorithm}")
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
