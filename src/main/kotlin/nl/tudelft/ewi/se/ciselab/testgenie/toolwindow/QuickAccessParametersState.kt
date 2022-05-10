package nl.tudelft.ewi.se.ciselab.testgenie.toolwindow

/**
 * This class is responsible for persisting the values of the parameters in the Quick Access (tool window),
 *   which are also present in the settings menu.
 */
data class TestGenieToolWindowState
constructor(
    var stoppingCondition: StoppingCondition = StoppingCondition.MAXTIME,
    var searchBudget: Int = 60,
    var initializationTimeout: Int = 120,
    var minimizationTimeout: Int = 60,
    var assertionTimeout: Int = 60,
    var junitCheckTimeout: Int = 60,
    var populationLimit: PopulationLimit = PopulationLimit.INDIVIDUALS,
    var population: Int = 50
) {
    private object DefaultToolWindowState {
        val stoppingCondition: StoppingCondition = StoppingCondition.MAXTIME
        const val searchBudget: Int = 60
        const val initializationTimeout: Int = 120
        const val minimizationTimeout: Int = 60
        const val assertionTimeout: Int = 60
        const val junitCheckTimeout: Int = 60
        val populationLimit: PopulationLimit = PopulationLimit.INDIVIDUALS
        const val population: Int = 50
    }

    fun serializeChangesFromDefault(): List<String> {

        val params = mutableListOf<String>()
        if (this.stoppingCondition != DefaultToolWindowState.stoppingCondition) {
            params.add("-Dstopping_condition=${this.stoppingCondition.name}")
        }
        if (this.searchBudget != DefaultToolWindowState.searchBudget) {
            params.add("-Dsearch_budget=${this.searchBudget}")
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
        if (this.populationLimit != DefaultToolWindowState.populationLimit) {
            params.add("-Dpopulation_limit=${this.populationLimit.name}")
        }
        if (this.population != DefaultToolWindowState.population) {
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
    TIMEDELTA("Time delta", "");

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
    fun units() : String = units
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