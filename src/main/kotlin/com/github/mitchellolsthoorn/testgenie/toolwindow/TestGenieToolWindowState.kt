package com.github.mitchellolsthoorn.testgenie.toolwindow

/**
 * This class is responsible for persisting the values of the parameters in the Quick Access (tool window),
 *   which are also present in the settings menu.
 */
data class TestGenieToolWindowState
constructor(
    var searchBudget: Int = 60,
    var localSearchBudgetType: LocalSearchBudgetType = LocalSearchBudgetType.TIME,
    var localSearchBudgetValue: Int = 5,
    var stoppingCondition: StoppingCondition = StoppingCondition.MAXTIME,
    var initializationTimeout: Int = 120,
    var minimizationTimeout: Int = 60,
    var assertionTimeout: Int = 60,
    var junitCheckTimeout: Int = 60,
    var population: Int = 50,
    var populationLimit: PopulationLimit = PopulationLimit.INDIVIDUALS
) {
    private object DefaultToolWindowState {
        const val searchBudget: Int = 60
        const val localSearchBudgetValue: Int = 5
        const val initializationTimeout: Int = 120
        const val minimizationTimeout: Int = 60
        const val assertionTimeout: Int = 60
        const val junitCheckTimeout: Int = 60
        const val population: Int = 50
        val localSearchBudgetType: LocalSearchBudgetType = LocalSearchBudgetType.TIME
        val stoppingCondition: StoppingCondition = StoppingCondition.MAXTIME
        val populationLimit: PopulationLimit = PopulationLimit.INDIVIDUALS
    }

    fun serializeChangesFromDefault(): List<String> {

        val params = mutableListOf<String>()
        if (this.searchBudget != DefaultToolWindowState.searchBudget) {
            params.add("-Dsearch_budget=${this.searchBudget}")
        }
        if (this.localSearchBudgetValue != DefaultToolWindowState.localSearchBudgetValue) {
            params.add("-Dlocal_search_budget=${this.localSearchBudgetValue}")
        }
        if (this.localSearchBudgetType != DefaultToolWindowState.localSearchBudgetType) {
            params.add("-Dlocal_search_budget_type=${this.localSearchBudgetType.name}")
        }
        if (this.stoppingCondition != DefaultToolWindowState.stoppingCondition) {
            params.add("-Dstopping_condition=${this.stoppingCondition.name}")
        }
        if (this.initializationTimeout != DefaultToolWindowState.initializationTimeout) {
            params.add("-Dinitialization_timeout=${this.initializationTimeout}")
        }
        if (this.minimizationTimeout != DefaultToolWindowState.minimizationTimeout) {
            params.add("-Dminimization_timeout=${this.minimizationTimeout}")
        }
        if (this.assertionTimeout != DefaultToolWindowState.assertionTimeout) {
            params.add("-Dassertion_timeout=${this.assertionTimeout}")
        }
        if (this.junitCheckTimeout != DefaultToolWindowState.junitCheckTimeout) {
            params.add("-Djunit_check_timeout=${this.junitCheckTimeout}")
        }
        if (this.population != DefaultToolWindowState.population) {
            params.add("-Dpopulation=${this.population}")
        }
        if (this.populationLimit != DefaultToolWindowState.populationLimit) {
            params.add("-Dpopulation_limit=${this.populationLimit.name}")
        }
        return params
    }

}

/**
 * This enum contains the types for the search budget value parameter.
 *
 * @param display string representation of the enum value that is used in UI elements
 */
enum class LocalSearchBudgetType(private val display: String) {
    TIME("Time"), STATEMENTS("Statements"), TESTS("Tests"), SUITES("Suites"), FITNESS_EVALUATIONS("Fitness evaluations");

    /**
     * Returns the display name of the given enum value that is shown in the UI elements.
     *
     * @return the display name of the given enum value
     */
    override fun toString(): String {
        return display
    }
}

/**
 * This enum contains the type for the stopping condition of the algorithm.
 *
 * @param display string representation of the enum value that is used in UI elements
 */
enum class StoppingCondition(private val display: String) {
    MAXTIME("Max time"), MAXSTATEMENTS("Max statements"), MAXTESTS("Max tests"), MAXGENERATIONS("Max generations"), MAXFITNESSEVALUATIONS(
        "Max fitness evaluations"
    ),
    TIMEDELTA("Time delta");

    /**
     * Returns the display name of the given enum value that is shown in the UI elements.
     *
     * @return the display name of the given enum value
     */
    override fun toString(): String {
        return display
    }
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