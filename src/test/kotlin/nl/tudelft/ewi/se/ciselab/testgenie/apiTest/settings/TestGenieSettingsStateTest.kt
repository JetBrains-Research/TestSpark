package nl.tudelft.ewi.se.ciselab.testgenie.apiTest.settings

import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl
import nl.tudelft.ewi.se.ciselab.testgenie.settings.ContentDigestAlgorithm
import nl.tudelft.ewi.se.ciselab.testgenie.settings.TestGenieSettingsState
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TestGenieSettingsStateTest {
    private lateinit var settingsState: TestGenieSettingsState
    private lateinit var fixture: CodeInsightTestFixture

    @BeforeEach
    fun setUp() {
        val factory = IdeaTestFixtureFactory.getFixtureFactory()
        val testFixture = factory.createLightFixtureBuilder(LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR).fixture
        fixture = factory.createCodeInsightFixture(testFixture, LightTempDirTestFixtureImpl(true))
        fixture.setUp()
        settingsState = TestGenieSettingsState()

        settingsState.algorithm = ContentDigestAlgorithm.DYNAMOSA
    }

    @AfterEach
    fun tearDown() {
        fixture.tearDown()
    }

    @Test
    fun testSerialiseEmpty() {
        assertThat(settingsState.serializeChangesFromDefault()).isEqualTo(mutableListOf<String>())
    }

    @Test
    fun testSerialiseSandbox() {
        settingsState.sandbox = !settingsState.sandbox
        assertThat(settingsState.serializeChangesFromDefault()).isEqualTo(mutableListOf("-Dsandbox=false"))
    }

    @Test
    fun testSerialiseAssertions() {
        settingsState.assertions = !settingsState.assertions
        assertThat(settingsState.serializeChangesFromDefault()).isEqualTo(mutableListOf("-Dassertions=false"))
    }

    @Test
    fun testSerialiseAlgorithm() {
        settingsState.algorithm = ContentDigestAlgorithm.RANDOM_SEARCH
        assertThat(settingsState.serializeChangesFromDefault()).isEqualTo(mutableListOf("-Dalgorithm=RANDOM_SEARCH"))
    }

    @Test
    fun testSerialiseJunitCheck() {
        settingsState.junitCheck = !settingsState.junitCheck
        assertThat(settingsState.serializeChangesFromDefault()).isEqualTo(mutableListOf("-Djunit_check=true"))
    }

    @Test
    fun testSerialiseMinimize() {
        settingsState.minimize = !settingsState.minimize
        assertThat(settingsState.serializeChangesFromDefault()).isEqualTo(mutableListOf("-Dminimize=false"))
    }
}
