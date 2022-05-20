package nl.tudelft.ewi.se.ciselab.testgenie.toolwindow

import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
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
        Assertions.assertThat(expected).isEqualTo(state.serializeChangesFromDefault())
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
}
