package nl.tudelft.ewi.se.ciselab.testgenie.settings

import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestGenieSettingsStateTest {
    private lateinit var settingsState: SettingsApplicationState
    private lateinit var fixture: CodeInsightTestFixture

    @BeforeEach
    fun setUp() {
        val factory = IdeaTestFixtureFactory.getFixtureFactory()
        val testFixture = factory.createLightFixtureBuilder(LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR).fixture
        fixture = factory.createCodeInsightFixture(testFixture, LightTempDirTestFixtureImpl(true))
        fixture.setUp()
        settingsState = SettingsApplicationState()

        settingsState.algorithm = ContentDigestAlgorithm.DYNAMOSA
    }

    @AfterEach
    fun tearDown() {
        fixture.tearDown()
    }

    @ParameterizedTest
    @MethodSource("valueGenerator")
    fun testSerialise(function: () -> Unit, expected: MutableList<String>) {
        function()
        assertThat(expected).isEqualTo(settingsState.serializeChangesFromDefault())
    }

    private fun valueGenerator(): Stream<Arguments> {
        return Stream.of(
            Arguments.of({ settingsState.minimize = !settingsState.minimize }, mutableListOf("-Dminimize=false")),
            Arguments.of({ settingsState.junitCheck = !settingsState.junitCheck }, mutableListOf("-Djunit_check=true")),
            Arguments.of(
                { settingsState.algorithm = ContentDigestAlgorithm.RANDOM_SEARCH },
                mutableListOf("-Dalgorithm=RANDOM_SEARCH")
            ),
            Arguments.of({ settingsState.assertions = !settingsState.assertions }, mutableListOf("-Dassertions=false")),
            Arguments.of({ settingsState.sandbox = !settingsState.sandbox }, mutableListOf("-Dsandbox=false")),
            Arguments.of({}, mutableListOf<String>())
        )
    }
}
