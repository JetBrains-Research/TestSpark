package org.jetbrains.research.testgenie.settings

import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.testFramework.fixtures.JavaTestFixtureFactory
import com.intellij.testFramework.fixtures.TestFixtureBuilder
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.research.testgenie.services.SettingsApplicationService
import org.jetbrains.research.testgenie.services.SettingsProjectService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SettingsPluginConfigurableTest {
    private lateinit var settingsConfigurable: SettingsPluginConfigurable
    private lateinit var settingsEvoConfigurable: SettingsEvoSuiteConfigurable
    private lateinit var settingsComponent: SettingsPluginComponent
    private lateinit var settingsEvoComponent: SettingsEvoSuiteComponent
    private lateinit var settingsState: SettingsProjectState
    private lateinit var fixture: CodeInsightTestFixture
    private lateinit var settingsApplicationState: SettingsApplicationState

    @BeforeEach
    fun setUp() {
        val projectBuilder: TestFixtureBuilder<IdeaProjectTestFixture> =
            IdeaTestFixtureFactory.getFixtureFactory().createFixtureBuilder("project")

        fixture = JavaTestFixtureFactory.getFixtureFactory()
            .createCodeInsightFixture(projectBuilder.fixture)
        fixture.setUp()

        settingsConfigurable = SettingsPluginConfigurable(fixture.project)

        settingsConfigurable.createComponent()
        settingsConfigurable.reset()

        settingsEvoConfigurable = SettingsEvoSuiteConfigurable()
        settingsEvoConfigurable.createComponent()
        settingsEvoConfigurable.reset()

        settingsEvoComponent = settingsEvoConfigurable.settingsComponent!!

        settingsComponent = settingsConfigurable.settingsComponent!!
        settingsState = fixture.project.service<SettingsProjectService>().state

        settingsApplicationState = SettingsApplicationService.getInstance().state!!
    }

    @AfterEach
    fun tearDown() {
        fixture.tearDown()
        settingsConfigurable.disposeUIResources()
    }

    @ParameterizedTest
    @Order(2)
    @MethodSource("intValueGenerator")
    fun testIsModifiedValues(
        oldValue: Int,
        function: () -> Unit,
        component: () -> Int,
        state: () -> Int,
    ) {
        function()
        assertThat(settingsConfigurable.isModified).isTrue
    }

    @ParameterizedTest
    @MethodSource("intValueGenerator")
    @Order(3)
    fun testApplyValues(oldValue: Int, function: () -> Unit, component: () -> Int, state: () -> Int) {
        function()
        settingsConfigurable.apply()
        assertThat(component()).isNotEqualTo(oldValue)
        assertThat(state()).isNotEqualTo(oldValue)
    }

    @Test
    @Order(1)
    fun testIsModifiedJavaPath() {
        settingsApplicationState.javaPath = "this is modified: first"
        assertThat(settingsEvoConfigurable.isModified).isTrue
        assertThat(settingsEvoComponent.javaPath).isNotEqualTo(settingsApplicationState.javaPath)
    }

    @Order(4)
    @Test
    fun testResetJavaPath() {
        val oldValue = settingsEvoComponent.javaPath
        settingsEvoComponent.javaPath = "this is modified: second"
        settingsEvoConfigurable.reset()
        assertThat(oldValue).isEqualTo(settingsEvoComponent.javaPath)
    }

    @Test
    fun testApplyJavaPath() {
        val oldValue = settingsApplicationState.javaPath
        settingsApplicationState.javaPath = "this is modified: third"
        settingsConfigurable.apply()
        assertThat(oldValue).isNotEqualTo(settingsApplicationState.javaPath)
        assertThat(oldValue).isNotEqualTo(settingsApplicationState.javaPath)
    }

    private fun intValueGenerator(): Stream<Arguments> {
        return Stream.of(
            Arguments.of(
                settingsComponent.colorBlue,
                { settingsComponent.colorBlue += 20 },
                { settingsComponent.colorBlue },
                { settingsState.colorBlue },
            ),
            Arguments.of(
                settingsComponent.colorRed,
                { settingsComponent.colorRed += 30 },
                { settingsComponent.colorRed },
                { settingsState.colorRed },
            ),
            Arguments.of(
                settingsComponent.colorGreen,
                { settingsComponent.colorGreen += 10 },
                { settingsComponent.colorGreen },
                { settingsState.colorGreen },
            ),
        )
    }
}
