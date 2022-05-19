package nl.tudelft.ewi.se.ciselab.testgenie.uiTest

import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.tudelft.ewi.se.ciselab.testgenie.settings.SettingsPluginComponent
import nl.tudelft.ewi.se.ciselab.testgenie.settings.SettingsPluginConfigurable
import nl.tudelft.ewi.se.ciselab.testgenie.settings.TestGenieSettingsService
import nl.tudelft.ewi.se.ciselab.testgenie.settings.TestGenieSettingsState
import org.assertj.core.api.Assertions.assertThat


class SettingsPluginConfigurableTest {
    private val settingsConfigurable = SettingsPluginConfigurable()
    private lateinit var settingsComponent: SettingsPluginComponent
    private lateinit var settingsState: TestGenieSettingsState

    override fun setUp() {
        super.setUp()
        settingsConfigurable.createComponent()
        settingsConfigurable.reset()
        settingsComponent = settingsConfigurable.settingsComponent!!
        settingsState = ApplicationManager.getApplication().getService(TestGenieSettingsService::class.java).state
    }

    fun testIsModifiedShowCoverage() {
        settingsComponent.showCoverage = !settingsComponent.showCoverage
        assertThat(settingsConfigurable.isModified).isTrue
        assertThat(settingsComponent.showCoverage).isNotEqualTo(settingsState.showCoverage)
    }

    fun testResetShowCoverage() {
        val oldValue = settingsComponent.showCoverage
        settingsComponent.showCoverage = !settingsComponent.showCoverage
        settingsConfigurable.reset()
        assertThat(oldValue).isEqualTo(settingsComponent.showCoverage)
    }

    fun testApplyShowCoverage() {
        val oldValue = settingsComponent.showCoverage
        settingsComponent.showCoverage = !settingsComponent.showCoverage
        settingsConfigurable.apply()
        assertThat(oldValue).isNotEqualTo(settingsComponent.showCoverage)
        assertThat(oldValue).isNotEqualTo(settingsState.showCoverage)
    }
}
