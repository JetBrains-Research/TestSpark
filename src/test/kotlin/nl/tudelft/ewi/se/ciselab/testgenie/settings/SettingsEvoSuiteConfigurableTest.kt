package nl.tudelft.ewi.se.ciselab.testgenie.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.testFramework.fixtures.JavaTestFixtureFactory
import com.intellij.testFramework.fixtures.TestFixtureBuilder
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SettingsEvoSuiteConfigurableTest {
    private val settingsConfigurable = SettingsEvoSuiteConfigurable()
    private lateinit var settingsComponent: SettingsEvoSuiteComponent
    private lateinit var settingsState: TestGenieSettingsState
    private lateinit var fixture: CodeInsightTestFixture

    @BeforeEach
    fun setUp() {
        val projectBuilder: TestFixtureBuilder<IdeaProjectTestFixture> =
            IdeaTestFixtureFactory.getFixtureFactory().createFixtureBuilder("project")

        fixture = JavaTestFixtureFactory.getFixtureFactory()
            .createCodeInsightFixture(projectBuilder.fixture)
        fixture.setUp()

        settingsConfigurable.createComponent()
        settingsConfigurable.reset()
        settingsComponent = settingsConfigurable.settingsComponent!!
        settingsState = ApplicationManager.getApplication().getService(TestGenieSettingsService::class.java).state
    }

    @AfterEach
    fun tearDown() {
        fixture.tearDown()
    }

    @Test
    fun testIsModifiedSeed() {
        val oldValue = settingsComponent.seed
        settingsComponent.seed = "7"
        assertThat(settingsConfigurable.isModified).isTrue
    }

    @Test
    fun testResetSeed() {
        val oldValue = settingsComponent.seed
        settingsComponent.seed = "5"
        settingsConfigurable.reset()
        assertThat(oldValue).isEqualTo(settingsComponent.seed)
    }

    @Test
    fun testApplySeedCorrect() {
        val oldValue = settingsComponent.seed
        settingsComponent.seed = "3"
        settingsConfigurable.apply()
        assertThat(oldValue).isNotEqualTo(settingsComponent.seed)
        assertThat(oldValue).isNotEqualTo(settingsState.seed)
    }

    @Test
    fun testApplySeedIncorrect() {
        val oldValue = settingsComponent.seed
        settingsComponent.seed = "not a number"
        assertThatThrownBy { settingsConfigurable.apply() }.isInstanceOf(RuntimeException::class.java)
    }

    @Test
    fun testIsModifiedConfigurationId() {
        val oldValue = settingsComponent.configurationId
        settingsComponent.configurationId = "7id"
        assertThat(settingsConfigurable.isModified).isTrue
    }

    @Test
    fun testResetConfigurationId() {
        val oldValue = settingsComponent.configurationId
        settingsComponent.configurationId = "5id"
        settingsConfigurable.reset()
        assertThat(oldValue).isEqualTo(settingsComponent.configurationId)
    }

    @Test
    fun testApplyConfigurationId() {
        val oldValue = settingsComponent.configurationId
        settingsComponent.configurationId = "3id"
        settingsConfigurable.apply()
        assertThat(oldValue).isNotEqualTo(settingsComponent.configurationId)
        assertThat(oldValue).isNotEqualTo(settingsState.configurationId)
    }

    @Test
    fun testResetAlgorithmComboBox() {
        val oldValue = settingsComponent.algorithm
        settingsComponent.algorithm = ContentDigestAlgorithm.NSGAII
        settingsConfigurable.reset()
        assertThat(oldValue).isEqualTo(settingsComponent.algorithm)
    }

    @Test
    fun testApplyAlgorithmComboBox() {
        val oldValue = settingsComponent.algorithm
        settingsComponent.algorithm = ContentDigestAlgorithm.BREEDER_GA
        settingsConfigurable.apply()
        assertThat(oldValue).isNotEqualTo(settingsComponent.algorithm)
        assertThat(oldValue).isNotEqualTo(settingsState.algorithm)
    }

    @Test
    fun testIsModifiedAlgorithmComboBox() {
        settingsComponent.algorithm = ContentDigestAlgorithm.DYNAMOSA
        assertThat(settingsConfigurable.isModified).isTrue
    }

    @ParameterizedTest
    @MethodSource("checkBoxValueGenerator")
    fun testResetCheckBoxes(function: () -> Boolean, component: () -> Boolean, state: () -> Boolean) {
        val oldValue = function()
        settingsConfigurable.reset()
        assertThat(component()).isEqualTo(oldValue)
        assertThat(state()).isEqualTo(oldValue)
    }

    @ParameterizedTest
    @MethodSource("checkBoxValueGenerator")
    fun testApplyCheckBoxes(function: () -> Boolean, component: () -> Boolean, state: () -> Boolean) {
        val oldValue = function()
        settingsConfigurable.apply()
        assertThat(component()).isNotEqualTo(oldValue)
        assertThat(state()).isNotEqualTo(oldValue)
    }

    @ParameterizedTest
    @MethodSource("checkBoxValueGenerator")
    fun testIsModifiedCheckBoxes(function: () -> Boolean, component: () -> Boolean, state: () -> Boolean) {
        val oldValue = function()
        assertThat(settingsConfigurable.isModified).isTrue
        assertThat(component()).isNotEqualTo(oldValue)
        assertThat(state()).isEqualTo(oldValue)
    }

    /**
     * Helper method to generate the stream of functions.
     *
     * @return stream of functions
     */
    private fun checkBoxValueGenerator(): Stream<Arguments> {
        return Stream.of(
            Arguments.of(
                {
                    val oldValue = settingsComponent.junitCheck
                    settingsComponent.junitCheck = !settingsComponent.junitCheck
                    return@of oldValue
                },
                { settingsComponent.junitCheck },
                { settingsState.junitCheck }
            ),
            Arguments.of(
                {
                    val oldValue = settingsComponent.assertions
                    settingsComponent.assertions = !settingsComponent.assertions
                    return@of oldValue
                },
                { settingsComponent.assertions },
                { settingsState.assertions }
            ),
            Arguments.of(
                {
                    val oldValue = settingsComponent.clientOnThread
                    settingsComponent.clientOnThread = !settingsComponent.clientOnThread
                    return@of oldValue
                },
                { settingsComponent.clientOnThread },
                { settingsState.clientOnThread }
            ),
            Arguments.of(
                {
                    val oldValue = settingsComponent.criterionBranch
                    settingsComponent.criterionBranch = !settingsComponent.criterionBranch
                    return@of oldValue
                },
                { settingsComponent.criterionBranch },
                { settingsState.criterionBranch }
            ),
            Arguments.of(
                {
                    val oldValue = settingsComponent.criterionCBranch
                    settingsComponent.criterionCBranch = !settingsComponent.criterionCBranch
                    return@of oldValue
                },
                { settingsComponent.criterionCBranch },
                { settingsState.criterionCBranch }
            ),
            Arguments.of(
                {
                    val oldValue = settingsComponent.criterionException
                    settingsComponent.criterionException = !settingsComponent.criterionException
                    return@of oldValue
                },
                { settingsComponent.criterionException },
                { settingsState.criterionException }
            ),
            Arguments.of(
                {
                    val oldValue = settingsComponent.criterionLine
                    settingsComponent.criterionLine = !settingsComponent.criterionLine
                    return@of oldValue
                },
                { settingsComponent.criterionLine },
                { settingsState.criterionLine }
            ),
            Arguments.of(
                {
                    val oldValue = settingsComponent.criterionMethod
                    settingsComponent.criterionMethod = !settingsComponent.criterionMethod
                    return@of oldValue
                },
                { settingsComponent.criterionMethod },
                { settingsState.criterionMethod }
            ),
            Arguments.of(
                {
                    val oldValue = settingsComponent.criterionMethodNoException
                    settingsComponent.criterionMethodNoException = !settingsComponent.criterionMethodNoException
                    return@of oldValue
                },
                { settingsComponent.criterionMethodNoException },
                { settingsState.criterionMethodNoException }
            ),
            Arguments.of(
                {
                    val oldValue = settingsComponent.criterionOutput
                    settingsComponent.criterionOutput = !settingsComponent.criterionOutput
                    return@of oldValue
                },
                { settingsComponent.criterionOutput },
                { settingsState.criterionOutput }
            ),
            Arguments.of(
                {
                    val oldValue = settingsComponent.criterionWeakMutation
                    settingsComponent.criterionWeakMutation = !settingsComponent.criterionWeakMutation
                    return@of oldValue
                },
                { settingsComponent.criterionWeakMutation },
                { settingsState.criterionWeakMutation }
            ),
            Arguments.of(
                {
                    val oldValue = settingsComponent.minimize
                    settingsComponent.minimize = !settingsComponent.minimize
                    return@of oldValue
                },
                { settingsComponent.minimize },
                { settingsState.minimize }
            ),
            Arguments.of(
                {
                    val oldValue = settingsComponent.sandbox
                    settingsComponent.sandbox = !settingsComponent.sandbox
                    return@of oldValue
                },
                { settingsComponent.sandbox },
                { settingsState.sandbox }
            )
        )
    }
}
