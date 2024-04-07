package org.jetbrains.research.testspark.settings

import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.testFramework.fixtures.JavaTestFixtureFactory
import com.intellij.testFramework.fixtures.TestFixtureBuilder
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.jetbrains.research.testspark.services.SettingsApplicationService
import org.jetbrains.research.testspark.settings.evosuite.SettingsEvoSuiteComponent
import org.jetbrains.research.testspark.settings.evosuite.SettingsEvoSuiteConfigurable
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
    private lateinit var settingsComponent: SettingsEvoSuiteComponent
    private lateinit var settingsState: SettingsApplicationState
    private lateinit var fixture: CodeInsightTestFixture

    private lateinit var settingsConfigurable: SettingsEvoSuiteConfigurable

    @BeforeEach
    fun setUp() {
        val projectBuilder: TestFixtureBuilder<IdeaProjectTestFixture> =
            IdeaTestFixtureFactory.getFixtureFactory().createFixtureBuilder("project")

        fixture = JavaTestFixtureFactory.getFixtureFactory()
            .createCodeInsightFixture(projectBuilder.fixture)
        fixture.setUp()

        settingsConfigurable = SettingsEvoSuiteConfigurable(fixture.project)
        settingsConfigurable.createComponent()
        settingsConfigurable.reset()
        settingsComponent = settingsConfigurable.settingsComponent!!
        settingsState = fixture.project.getService(SettingsApplicationService::class.java).state
    }

    @AfterEach
    fun tearDown() {
        fixture.tearDown()
    }

    @Test
    fun testIsModifiedSeed() {
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
        settingsComponent.seed = "not a number"
        assertThatThrownBy { settingsConfigurable.apply() }.isInstanceOf(RuntimeException::class.java)
    }

    @Test
    fun testIsModifiedConfigurationId() {
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

    @ParameterizedTest
    @MethodSource("checkBoxValueGenerator")
    fun testResetCheckBoxes(oldValue: Boolean, function: () -> Unit, component: () -> Boolean, state: () -> Boolean) {
        function()
        settingsConfigurable.reset()
        assertThat(component()).isEqualTo(oldValue)
        assertThat(state()).isEqualTo(oldValue)
    }

    @ParameterizedTest
    @MethodSource("checkBoxValueGenerator")
    fun testApplyCheckBoxes(oldValue: Boolean, function: () -> Unit, component: () -> Boolean, state: () -> Boolean) {
        function()
        settingsConfigurable.apply()
        assertThat(component()).isNotEqualTo(oldValue)
        assertThat(state()).isNotEqualTo(oldValue)
    }

    @ParameterizedTest
    @MethodSource("checkBoxValueGenerator")
    fun testIsModifiedCheckBoxes(
        oldValue: Boolean,
        function: () -> Unit,
        component: () -> Boolean,
        state: () -> Boolean,
    ) {
        function()
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
                settingsComponent.junitCheck,
                { settingsComponent.junitCheck = !settingsComponent.junitCheck },
                { settingsComponent.junitCheck },
                { settingsState.junitCheck },
            ),
            Arguments.of(
                settingsComponent.assertions,
                { settingsComponent.assertions = !settingsComponent.assertions },
                { settingsComponent.assertions },
                { settingsState.assertions },
            ),
            Arguments.of(
                settingsComponent.clientOnThread,
                { settingsComponent.clientOnThread = !settingsComponent.clientOnThread },
                { settingsComponent.clientOnThread },
                { settingsState.clientOnThread },
            ),
            Arguments.of(
                settingsComponent.criterionBranch,
                { settingsComponent.criterionBranch = !settingsComponent.criterionBranch },
                { settingsComponent.criterionBranch },
                { settingsState.criterionBranch },
            ),
            Arguments.of(
                settingsComponent.criterionCBranch,
                { settingsComponent.criterionCBranch = !settingsComponent.criterionCBranch },
                { settingsComponent.criterionCBranch },
                { settingsState.criterionCBranch },
            ),
            Arguments.of(
                settingsState.criterionException,
                { settingsComponent.criterionException = !settingsComponent.criterionException },
                { settingsComponent.criterionException },
                { settingsState.criterionException },
            ),
            Arguments.of(
                settingsComponent.criterionLine,
                { settingsComponent.criterionLine = !settingsComponent.criterionLine },
                { settingsComponent.criterionLine },
                { settingsState.criterionLine },
            ),
            Arguments.of(
                settingsComponent.criterionMethod,
                { settingsComponent.criterionMethod = !settingsComponent.criterionMethod },
                { settingsComponent.criterionMethod },
                { settingsState.criterionMethod },
            ),
            Arguments.of(
                settingsComponent.criterionMethodNoException,
                { settingsComponent.criterionMethodNoException = !settingsComponent.criterionMethodNoException },
                { settingsComponent.criterionMethodNoException },
                { settingsState.criterionMethodNoException },
            ),
            Arguments.of(
                settingsComponent.criterionOutput,
                { settingsComponent.criterionOutput = !settingsComponent.criterionOutput },
                { settingsComponent.criterionOutput },
                { settingsState.criterionOutput },
            ),
            Arguments.of(
                settingsComponent.criterionWeakMutation,
                { settingsComponent.criterionWeakMutation = !settingsComponent.criterionWeakMutation },
                { settingsComponent.criterionWeakMutation },
                { settingsState.criterionWeakMutation },
            ),
            Arguments.of(
                settingsComponent.minimize,
                { settingsComponent.minimize = !settingsComponent.minimize },
                { settingsComponent.minimize },
                { settingsState.minimize },
            ),
            Arguments.of(
                settingsComponent.sandbox,
                { settingsComponent.sandbox = !settingsComponent.sandbox },
                { settingsComponent.sandbox },
                { settingsState.sandbox },
            ),
        )
    }
}
