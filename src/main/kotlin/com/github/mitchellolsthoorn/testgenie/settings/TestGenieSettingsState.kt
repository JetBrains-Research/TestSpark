package com.github.mitchellolsthoorn.testgenie.settings

/**
 * This class is the actual data class that stores the values of the Settings entries.
 */
data class TestGenieSettingsState(
        var globalTimeout: String = "60",
        var showCoverage: Boolean = false,

        var sandbox : Boolean = true,
        var assertions : Boolean = true,
        var seed : String = "",
        var algorithm: String = ContentDigestAlgorithm.RANDOM_SEARCH.toString(),
        var configurationId : String = "",
        var clientOnThread : Boolean = false,
        var junitCheck : Boolean = false,
        var criterionLine : Boolean = true,
        var criterionBranch : Boolean = true,
        var criterionException : Boolean = true,
        var criterionWeakMutation : Boolean = true,
        var criterionOutput : Boolean = true,
        var criterionMethod : Boolean = true,
        var criterionMethodNoException : Boolean = true,
        var criterionCBranch : Boolean = true

)

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