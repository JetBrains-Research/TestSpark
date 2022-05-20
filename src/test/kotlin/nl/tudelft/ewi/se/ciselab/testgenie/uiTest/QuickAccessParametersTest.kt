package nl.tudelft.ewi.se.ciselab.testgenie.uiTest

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.JLabelFixture
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.IdeaFrame
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.QuickAccessParametersFixtures
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.WelcomeFrame
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.utils.RemoteRobotExtension
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.Duration
import java.util.stream.Stream

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(RemoteRobotExtension::class)
class QuickAccessParametersTest {
    // Open the tool window frame
    private lateinit var quickAccessParameters: QuickAccessParametersFixtures

    /**
     * Opens an untitled project from the IntelliJ welcome screen.
     * Then opens the TestGenie sidebar on the right.
     */
    @BeforeAll
    fun setUpAll(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        // Open an 'untitled' projectLabel
        find(WelcomeFrame::class.java, timeout = Duration.ofSeconds(15)).apply {
            open("untitled")
        }

        // Open the TestGenie tool window
        find(IdeaFrame::class.java, timeout = Duration.ofSeconds(15)).apply {
            clickOnToolWindow()
        }
    }

    @BeforeEach
    fun setUp(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        // Open the tool window frame
        quickAccessParameters = find(QuickAccessParametersFixtures::class.java, timeout = Duration.ofSeconds(15))
        // Open the "Quick Access Parameters" tab
        quickAccessParameters.openQuickAccessParametersTab()
    }

    @Order(1)
    @Test
    fun testEverythingIsVisible() {
        // Assert text is visible
        quickAccessParameters.getTitles().forEach { Assertions.assertThat(it.isVisible()) }
        quickAccessParameters.getUIElementLabels().forEach { Assertions.assertThat(it.isVisible()) }

        // Assert buttons are visible
        Assertions.assertThat(quickAccessParameters.advancedSettingsButton.isShowing).isTrue
        quickAccessParameters.getButtons().forEach { Assertions.assertThat(it.isShowing) }
    }

    @Order(2)
    @ParameterizedTest
    @MethodSource("valueGeneratorForLabelsAndTexts")
    fun testLabelsAreCorrect(expectedText: String, label: JLabelFixture) {
        // Assert labels have the correct text
        Assertions.assertThat(label.value).isEqualTo(expectedText)
    }

    private fun valueGeneratorForLabelsAndTexts(): Stream<Arguments> = Stream.of(
        Arguments.of(
            "Search budget type", quickAccessParameters.searchBudgetTypeLabel
        ),
        Arguments.of(
            "Search budget", quickAccessParameters.searchBudgetValueLabel
        ),
        Arguments.of(
            "Initialization timeout", quickAccessParameters.initializationTimeoutLabel
        ),
        Arguments.of(
            "Minimisation timeout", quickAccessParameters.minimisationTimeoutLabel
        ),
        Arguments.of(
            "Assertion timeout", quickAccessParameters.assertionTimeoutLabel
        ),
        Arguments.of(
            "JUnit check timeout", quickAccessParameters.jUnitCheckTimeoutLabel
        ),
        Arguments.of(
            "Population limit", quickAccessParameters.populationLimitLabel
        ),
        Arguments.of(
            "Population", quickAccessParameters.populationValueLabel
        )
    )

    @Order(3)
    @ParameterizedTest
    @MethodSource("valueGeneratorForTitlesAndTexts")
    fun testTitlesAreCorrect(expectedText: String, title: JLabelFixture) {
        // Assert labels have the text
        Assertions.assertThat(title.value).isEqualTo(expectedText)
    }

    private fun valueGeneratorForTitlesAndTexts(): Stream<Arguments> = Stream.of(
        Arguments.of(
            "Quick Access Parameters", quickAccessParameters.title
        ),
        Arguments.of(
            "Search budget", quickAccessParameters.searchBudgetSeparator
        ),
        Arguments.of(
            "Timeouts", quickAccessParameters.timeoutsSeparator
        ),
        Arguments.of(
            "Genetic Algorithm", quickAccessParameters.geneticAlgorithmSeparator
        )
    )

    @Order(4)
    @Test
    fun testToolTipsOnUIElementLabels() {
        // TODO: add other default tooltips
        val tooltipTexts = listOf(
            "Default: Max time", "Default: 120 seconds", "Default: Individuals"
        )
        quickAccessParameters.getDefaultTooltips().zip(tooltipTexts).forEach { Assertions.assertThat(it.first.value).isEqualTo(it.second) }
    }

    @Order(5)
    @Test
    fun testComboBoxesAreVisible() {
        quickAccessParameters.findComboBoxes().forEach { it.isShowing }
    }

    // TODO: spinners + modifications

    // TODO: combo-boxes + modifications

    // TODO: advanced settings button

    // TODO: save button

    // TODO: reset button

    // TODO (Optional): tooltips on hovering

    /**
     * First closes the TestGenie sidebar by clicking on the stripe button again.
     * Secondly, closes the project itself and returns the sandbox to IntelliJ welcome state.
     */
    @AfterAll
    fun closeAll(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {

        find(IdeaFrame::class.java, timeout = Duration.ofSeconds(15)).apply {
            clickOnToolWindow()
        }

        find(IdeaFrame::class.java, timeout = Duration.ofSeconds(15)).apply {
            closeProject()
        }
    }
}
