package org.jetbrains.research.testgenie.toolwindow

import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl
import org.jetbrains.research.testgenie.TestGenieDefaultsBundle
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QuickAccessParametersStateTest {
    private lateinit var state: QuickAccessParametersState
    private lateinit var fixture: CodeInsightTestFixture

    @BeforeEach
    fun setUp() {
        val factory = IdeaTestFixtureFactory.getFixtureFactory()
        val testFixture = factory.createLightFixtureBuilder(LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR).fixture
        fixture = factory.createCodeInsightFixture(testFixture, LightTempDirTestFixtureImpl(true))
        fixture.setUp()

        state = QuickAccessParametersState()
    }

    @AfterEach
    fun tearDown() {
        fixture.tearDown()
    }

    @ParameterizedTest
    @MethodSource("valueGenerator")
    fun testSerialise(updateValue: () -> Unit, expected: MutableList<String>) {
        updateValue()
        assertThat(expected).isEqualTo(state.serializeChangesFromDefault())
    }

    /**
     * Generates the arguments for `testSerialise` method:
     *   - the function performs an update of one value
     *   - the list is the expected command that has to be generated
     */
    private fun valueGenerator(): Stream<Arguments> = Stream.of(
        Arguments.of(
            { state.stoppingCondition = StoppingCondition.MAXFITNESSEVALUATIONS },
            mutableListOf("-Dstopping_condition=MAXFITNESSEVALUATIONS")
        ),
        Arguments.of(
            { state.searchBudget = 42 },
            mutableListOf("-Dsearch_budget=42")
        ),
        Arguments.of(
            { state.initializationTimeout = 69 },
            mutableListOf("-Dinitialization_timeout=69")
        ),
        Arguments.of(
            { state.minimizationTimeout = 256 },
            mutableListOf("-Dminimization_timeout=256")
        ),
        Arguments.of(
            { state.assertionTimeout = 61 },
            mutableListOf("-Dassertion_timeout=61"),
        ),

        Arguments.of(
            { state.junitCheckTimeout = 59 },
            mutableListOf("-Djunit_check_timeout=59")
        ),
        Arguments.of(
            { state.populationLimit = PopulationLimit.STATEMENTS },
            mutableListOf("-Dpopulation_limit=STATEMENTS")
        ),
        Arguments.of(
            { state.population = 128 },
            mutableListOf("-Dpopulation=128")
        )
    )

    @Test
    fun testDefaultStateNotAffected() {
        state.stoppingCondition = StoppingCondition.TIMEDELTA
        state.searchBudget = 123
        state.initializationTimeout = 890
        state.minimizationTimeout = 534
        state.assertionTimeout = 231
        state.junitCheckTimeout = 217
        state.populationLimit = PopulationLimit.TESTS
        state.population = 1

        val state = QuickAccessParametersState.DefaultState

        assertThat(state.stoppingCondition.name).isEqualTo(TestGenieDefaultsBundle.defaultValue("stoppingCondition"))
        assertThat(state.searchBudget).isEqualTo(TestGenieDefaultsBundle.defaultValue("searchBudget").toInt())
        assertThat(state.initializationTimeout).isEqualTo(TestGenieDefaultsBundle.defaultValue("initializationTimeout").toInt())
        assertThat(state.minimizationTimeout).isEqualTo(TestGenieDefaultsBundle.defaultValue("minimizationTimeout").toInt())
        assertThat(state.assertionTimeout).isEqualTo(TestGenieDefaultsBundle.defaultValue("assertionTimeout").toInt())
        assertThat(state.junitCheckTimeout).isEqualTo(TestGenieDefaultsBundle.defaultValue("junitCheckTimeout").toInt())
        assertThat(state.populationLimit.name).isEqualTo(TestGenieDefaultsBundle.defaultValue("populationLimit"))
        assertThat(state.population).isEqualTo(TestGenieDefaultsBundle.defaultValue("population").toInt())
    }

    @Test
    fun testStoppingConditionToString() {
        assertThat(StoppingCondition.MAXTIME.toString()).isEqualTo("Max time")
        assertThat(StoppingCondition.MAXSTATEMENTS.toString()).isEqualTo("Max statements")
        assertThat(StoppingCondition.MAXTESTS.toString()).isEqualTo("Max tests")
        assertThat(StoppingCondition.MAXGENERATIONS.toString()).isEqualTo("Max generations")
        assertThat(StoppingCondition.MAXFITNESSEVALUATIONS.toString()).isEqualTo("Max fitness evaluations")
        assertThat(StoppingCondition.TIMEDELTA.toString()).isEqualTo("Time delta")
    }

    @Test
    fun testStoppingConditionUnits() {
        assertThat(StoppingCondition.MAXTIME.units()).isEqualTo("seconds")
        assertThat(StoppingCondition.MAXSTATEMENTS.units()).isEqualTo("statements")
        assertThat(StoppingCondition.MAXTESTS.units()).isEqualTo("tests")
        assertThat(StoppingCondition.MAXGENERATIONS.units()).isEqualTo("generations")
        assertThat(StoppingCondition.MAXFITNESSEVALUATIONS.units()).isEqualTo("evaluations")
        assertThat(StoppingCondition.TIMEDELTA.units()).isEqualTo("")
    }

    @Test
    fun testPopulationLimitToString() {
        assertThat(PopulationLimit.INDIVIDUALS.toString()).isEqualTo("Individuals")
        assertThat(PopulationLimit.TESTS.toString()).isEqualTo("Tests")
        assertThat(PopulationLimit.STATEMENTS.toString()).isEqualTo("Statements")
    }
}
