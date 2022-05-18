package nl.tudelft.ewi.se.ciselab.testgenie.apiTest.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.tudelft.ewi.se.ciselab.testgenie.settings.ContentDigestAlgorithm
import nl.tudelft.ewi.se.ciselab.testgenie.settings.TestGenieSettingsService
import nl.tudelft.ewi.se.ciselab.testgenie.settings.TestGenieSettingsState
import org.assertj.core.api.Assertions.assertThat

class TestGenieSettingsStateTest : BasePlatformTestCase() {
    private lateinit var settingsState: TestGenieSettingsState

    override fun setUp() {
        super.setUp()
        settingsState = ApplicationManager.getApplication().getService(TestGenieSettingsService::class.java).state
    }

    fun testSerialiseEmpty() {
        settingsState.algorithm = ContentDigestAlgorithm.DYNAMOSA
        assertThat(settingsState.serializeChangesFromDefault()).isEqualTo(mutableListOf<String>())
    }

    fun testSerialiseSandbox() {
        settingsState.algorithm = ContentDigestAlgorithm.DYNAMOSA
        settingsState.sandbox = !settingsState.sandbox
        assertThat(settingsState.serializeChangesFromDefault()).isEqualTo(mutableListOf("-Dsandbox=false"))
        settingsState.sandbox = !settingsState.sandbox
    }

    fun testSerialiseAssertions() {
        settingsState.algorithm = ContentDigestAlgorithm.DYNAMOSA
        settingsState.assertions = !settingsState.assertions
        assertThat(settingsState.serializeChangesFromDefault()).isEqualTo(mutableListOf("-Dassertions=false"))
        settingsState.assertions = !settingsState.assertions
    }

    fun testSerialiseAlgorithm() {
        settingsState.algorithm = ContentDigestAlgorithm.RANDOM_SEARCH
        assertThat(settingsState.serializeChangesFromDefault()).isEqualTo(mutableListOf("-Dalgorithm=RANDOM_SEARCH"))
    }

    fun testSerialiseJunitCheck() {
        settingsState.algorithm = ContentDigestAlgorithm.DYNAMOSA
        settingsState.junitCheck = !settingsState.junitCheck
        assertThat(settingsState.serializeChangesFromDefault()).isEqualTo(mutableListOf("-Djunit_check=true"))
        settingsState.junitCheck = !settingsState.junitCheck
    }

    fun testSerialiseMinimize() {
        settingsState.algorithm = ContentDigestAlgorithm.DYNAMOSA
        settingsState.minimize = !settingsState.minimize
        assertThat(settingsState.serializeChangesFromDefault()).isEqualTo(mutableListOf("-Dminimize=false"))
        settingsState.minimize = !settingsState.minimize
    }
}
