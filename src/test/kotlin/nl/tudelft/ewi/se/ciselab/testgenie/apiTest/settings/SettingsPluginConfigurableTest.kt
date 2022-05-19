package nl.tudelft.ewi.se.ciselab.testgenie.apiTest.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl
import nl.tudelft.ewi.se.ciselab.testgenie.settings.SettingsPluginComponent
import nl.tudelft.ewi.se.ciselab.testgenie.settings.SettingsPluginConfigurable
import nl.tudelft.ewi.se.ciselab.testgenie.settings.TestGenieSettingsService
import nl.tudelft.ewi.se.ciselab.testgenie.settings.TestGenieSettingsState
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SettingsPluginConfigurableTest {
    private val settingsConfigurable = SettingsPluginConfigurable()
    private lateinit var settingsComponent: SettingsPluginComponent
    private lateinit var settingsState: TestGenieSettingsState
    private lateinit var fixture: CodeInsightTestFixture

    @BeforeEach
    fun setUp() {
        val factory = IdeaTestFixtureFactory.getFixtureFactory()
        val testFixture = factory.createLightFixtureBuilder(EMPTY_PROJECT_DESCRIPTOR).fixture
        fixture = factory.createCodeInsightFixture(testFixture, LightTempDirTestFixtureImpl(true))
        fixture.setUp()

        settingsConfigurable.createComponent()
        settingsConfigurable.reset()
        settingsComponent = settingsConfigurable.settingsComponent!!
        settingsState = ApplicationManager.getApplication().getService(TestGenieSettingsService::class.java).state
    }

    @Test
    fun testIsModifiedShowCoverage() {
        settingsComponent.showCoverage = !settingsComponent.showCoverage
        assertThat(settingsConfigurable.isModified).isTrue
        assertThat(settingsComponent.showCoverage).isNotEqualTo(settingsState.showCoverage)
    }

    @Test
    fun testResetShowCoverage() {
        val oldValue = settingsComponent.showCoverage
        settingsComponent.showCoverage = !settingsComponent.showCoverage
        settingsConfigurable.reset()
        assertThat(oldValue).isEqualTo(settingsComponent.showCoverage)
    }

    @Test
    fun testApplyShowCoverage() {
        val oldValue = settingsComponent.showCoverage
        settingsComponent.showCoverage = !settingsComponent.showCoverage
        settingsConfigurable.apply()
        assertThat(oldValue).isNotEqualTo(settingsComponent.showCoverage)
        assertThat(oldValue).isNotEqualTo(settingsState.showCoverage)
    }

    @AfterEach
    fun tearDown() {
        fixture.tearDown()
    }
}
