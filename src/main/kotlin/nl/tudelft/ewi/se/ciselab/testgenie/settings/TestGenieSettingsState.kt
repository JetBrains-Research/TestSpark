package nl.tudelft.ewi.se.ciselab.testgenie.settings

/**
 * This class is the actual data class that stores the values of the Settings entries.
 */
data class TestGenieSettingsState(
        var showCoverage: Boolean = false,

        var sandbox: Boolean = true,
        var assertions: Boolean = true,
        var seed: String = "",
        var algorithm: ContentDigestAlgorithm = ContentDigestAlgorithm.RANDOM_SEARCH,
        var configurationId: String = "",
        var clientOnThread: Boolean = false,
        var junitCheck: Boolean = false,
        var criterionLine: Boolean = true,
        var criterionBranch: Boolean = true,
        var criterionException: Boolean = true,
        var criterionWeakMutation: Boolean = true,
        var criterionOutput: Boolean = true,
        var criterionMethod: Boolean = true,
        var criterionMethodNoException: Boolean = true,
        var criterionCBranch: Boolean = true,
        var minimize: Boolean = true
) {
    private object DefaultSettingsState {
        const val sandbox: Boolean = true
        const val assertions: Boolean = true
        const val junitCheck: Boolean = false
        const val minimize: Boolean = true
        val algorithm: ContentDigestAlgorithm = ContentDigestAlgorithm.DYNAMOSA
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
        if (this.algorithm != DefaultSettingsState.algorithm) {
            params.add("-Dalgorithm=${this.algorithm}")
        }
        if (this.junitCheck != DefaultSettingsState.junitCheck) {
            params.add("-Djunit_check=${this.junitCheck}")
        }
        if (this.minimize != DefaultSettingsState.minimize) {
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