package org.jetbrains.research.testspark.toolwindow

import org.jetbrains.research.testspark.TestSparkDefaultsBundle

/**
 * This class is responsible for persisting the values of the parameters in the "Parameters" tool window tab.
 */
data class QuickAccessParametersState(
    var showCoverage: Boolean = DefaultState.showCoverage,
    var stoppingCondition: StoppingCondition = DefaultState.stoppingCondition,
    var searchBudget: Int = DefaultState.searchBudget,
    var initializationTimeout: Int = DefaultState.initializationTimeout,
    var minimizationTimeout: Int = DefaultState.minimizationTimeout,
    var assertionTimeout: Int = DefaultState.assertionTimeout,
    var junitCheckTimeout: Int = DefaultState.junitCheckTimeout,
    var populationLimit: PopulationLimit = DefaultState.populationLimit,
    var population: Int = DefaultState.population,
) {

    object DefaultState {
        val showCoverage: Boolean = TestSparkDefaultsBundle.defaultValue("showCoverage").toBoolean()
        val stoppingCondition: StoppingCondition = StoppingCondition.valueOf(TestSparkDefaultsBundle.defaultValue("stoppingCondition"))
        val searchBudget: Int = TestSparkDefaultsBundle.defaultValue("searchBudget").toInt()
        val initializationTimeout: Int = TestSparkDefaultsBundle.defaultValue("initializationTimeout").toInt()
        val minimizationTimeout: Int = TestSparkDefaultsBundle.defaultValue("minimizationTimeout").toInt()
        val assertionTimeout: Int = TestSparkDefaultsBundle.defaultValue("assertionTimeout").toInt()
        val junitCheckTimeout: Int = TestSparkDefaultsBundle.defaultValue("junitCheckTimeout").toInt()
        val populationLimit: PopulationLimit = PopulationLimit.valueOf(TestSparkDefaultsBundle.defaultValue("populationLimit"))
        val population: Int = TestSparkDefaultsBundle.defaultValue("population").toInt()
    }

    fun serializeChangesFromDefault(): List<String> {
        val params = mutableListOf<String>()
        if (this.stoppingCondition != DefaultState.stoppingCondition) {
            params.add("-Dstopping_condition=${this.stoppingCondition.name}")
        }
        if (this.searchBudget != DefaultState.searchBudget) {
            params.add("-Dsearch_budget=${this.searchBudget}")
        }
        if (this.initializationTimeout != DefaultState.initializationTimeout) {
            params.add("-Dinitialization_timeout=${this.initializationTimeout}")
        }
        if (this.minimizationTimeout != DefaultState.minimizationTimeout) {
            params.add("-Dminimization_timeout=${this.minimizationTimeout}")
        }
        if (this.assertionTimeout != DefaultState.assertionTimeout) {
            params.add("-Dassertion_timeout=${this.assertionTimeout}")
        }
        if (this.junitCheckTimeout != DefaultState.junitCheckTimeout) {
            params.add("-Djunit_check_timeout=${this.junitCheckTimeout}")
        }
        if (this.populationLimit != DefaultState.populationLimit) {
            params.add("-Dpopulation_limit=${this.populationLimit.name}")
        }
        if (this.population != DefaultState.population) {
            params.add("-Dpopulation=${this.population}")
        }
        return params
    }
}

/**
 * This enum contains the type for the stopping condition of the algorithm.
 *
 * @param display string representation of the enum value that is used in UI elements
 * @param units the units of measurement (e.g. seconds, tests etc.) that are used when displaying the tooltip with the default value
 */
enum class StoppingCondition(private val display: String, private val units: String) {
    MAXTIME("Max time", "seconds"), MAXSTATEMENTS("Max statements", "statements"), MAXTESTS("Max tests", "tests"),
    MAXGENERATIONS("Max generations", "generations"), MAXFITNESSEVALUATIONS("Max fitness evaluations", "evaluations"),
    TIMEDELTA("Time delta", ""),
    ;

    /**
     * Returns the display name of the given enum value that is shown in the UI elements.
     *
     * @return the display name of the given enum value
     */
    override fun toString(): String {
        return display
    }

    /**
     * Gets the units of measurement (e.g. seconds, tests etc.).
     *
     * @return the units of measurement
     */
    fun units(): String = units
}

/**
 * This enum contains the type for the limit for the population size.
 *
 * @param display string representation of the enum value that is used in UI elements
 */
enum class PopulationLimit(private val display: String) {
    INDIVIDUALS("Individuals"), TESTS("Tests"), STATEMENTS("Statements");

    /**
     * Returns the display name of the given enum value that is shown in the UI elements.
     *
     * @return the display name of the given enum value
     */
    override fun toString(): String {
        return display
    }
}
