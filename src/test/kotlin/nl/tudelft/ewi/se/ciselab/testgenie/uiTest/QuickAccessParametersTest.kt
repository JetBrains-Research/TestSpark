package nl.tudelft.ewi.se.ciselab.testgenie.uiTest

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.JLabelFixture
import com.intellij.remoterobot.fixtures.JListFixture
import com.intellij.remoterobot.search.locators.byXpath
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.customfixtures.BasicArrowButtonFixture
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.customfixtures.JSpinnerFixture
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

    private lateinit var remoteRobot: RemoteRobot

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
        Thread.sleep(10)
        // Open the TestGenie tool window
        find(IdeaFrame::class.java, timeout = Duration.ofSeconds(15)).apply {
            clickOnToolWindow()
        }
    }

    @BeforeEach
    fun setUp(_remoteRobot: RemoteRobot): Unit = with(_remoteRobot) {
        remoteRobot = _remoteRobot
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
    @ParameterizedTest
    @MethodSource("valueGeneratorForDefaultTooltipsAndTexts")
    fun testToolTipsOnUIElementLabels(expectedText: String, tooltipLabel: JLabelFixture) {
        // TODO: add other default tooltips
        Assertions.assertThat(tooltipLabel.value).isEqualTo(expectedText)
    }

    private fun valueGeneratorForDefaultTooltipsAndTexts(): Stream<Arguments> = Stream.of(
        Arguments.of(
            "Default: Max time", quickAccessParameters.searchBudgetTypeDefaultTooltip
        ),
        Arguments.of(
            "Default: 120 seconds", quickAccessParameters.initializationTimeoutDefaultTooltip
        ),
        Arguments.of(
            "Default: Individuals", quickAccessParameters.populationLimitDefaultTooltip
        )
    )

    @Order(5)
    @Test
    fun testComboBoxesAreVisible() {
        quickAccessParameters.getComboBoxes().forEach { it.isShowing }
    }

    @Order(6)
    @Test
    fun testSpinnersAreVisible() {
        quickAccessParameters.getJSpinners().forEach { Assertions.assertThat(it.isShowing) }
    }

    @Order(7)
    @ParameterizedTest
    @MethodSource("valueGeneratorForSearchBudgetTypeComboBoxChoices")
    fun testSearchBudgetComboBoxes(choice: String): Unit = with(remoteRobot) {
        quickAccessParameters.searchBudgetTypeArrow.click()
        val choices: JListFixture = find(byXpath("//div[@class='JList']"), Duration.ofSeconds(15))
        choices.clickItem(choice)
        Assertions.assertThat(quickAccessParameters.searchBudgetTypeComboBox.hasText(choice))
    }

    private fun valueGeneratorForSearchBudgetTypeComboBoxChoices(): Stream<Arguments> = Stream.of(
        Arguments.of("Max statements"),
        Arguments.of("Max tests"),
        Arguments.of("Max generations"),
        Arguments.of("Max fitness evaluations"),
        Arguments.of("Time delta"),
        Arguments.of("Max time"),
    )

    @Order(8)
    @ParameterizedTest
    @MethodSource("valueGeneratorForPopulationLimitComboBoxChoices")
    fun testPopulationLimitComboBoxes(choice: String): Unit = with(remoteRobot) {
        quickAccessParameters.populationLimitArrow.click()
        val choices: JListFixture = find(byXpath("//div[@class='JList']"), Duration.ofSeconds(15))
        choices.clickItem(choice)
        Assertions.assertThat(quickAccessParameters.populationLimitComboBox.hasText(choice))
    }

    private fun valueGeneratorForPopulationLimitComboBoxChoices(): Stream<Arguments> = Stream.of(
        Arguments.of("Tests"),
        Arguments.of("Statements"),
        Arguments.of("Individuals"),
    )

    // @Order(9)
    @ParameterizedTest
    @MethodSource("valueGeneratorForSpinners")
    fun testSpinner(arrowUp: BasicArrowButtonFixture, arrowDown: BasicArrowButtonFixture, spinner: JSpinnerFixture, clicksUp: Int, clicksDown: Int) {
        val oldValue = spinner.data.getAll()[0].text.toInt()

        for (i in 1..clicksUp) {
            arrowUp.click()
        }
        Assertions.assertThat(spinner.data.getAll()[0].text.toInt()).isEqualTo(oldValue + clicksUp)

        for (i in 1..clicksDown) {
            arrowDown.click()
        }
        Assertions.assertThat(spinner.data.getAll()[0].text.toInt()).isEqualTo(oldValue + clicksUp - clicksDown)
    }

    private fun valueGeneratorForSpinners(): Stream<Arguments> = Stream.of(
        Arguments.of(
            quickAccessParameters.searchBudgetValueUpArrow, quickAccessParameters.searchBudgetValueDownArrow,
            quickAccessParameters.searchBudgetValueSpinner, 2, 4
        ),
        Arguments.of(
            quickAccessParameters.initializationTimeoutUpArrow, quickAccessParameters.initializationTimeoutDownArrow,
            quickAccessParameters.initializationTimeoutSpinner, 4, 2
        ),
        Arguments.of(
            quickAccessParameters.minimisationTimeoutUpArrow, quickAccessParameters.minimisationTimeoutDownArrow,
            quickAccessParameters.minimisationTimeoutSpinner, 3, 3
        ),
        Arguments.of(
            quickAccessParameters.assertionTimeoutUpArrow, quickAccessParameters.assertionTimeoutDownArrow,
            quickAccessParameters.assertionTimeoutSpinner, 1, 2
        ),
        Arguments.of(
            quickAccessParameters.jUnitCheckTimeoutUpArrow, quickAccessParameters.jUnitCheckTimeoutDownArrow,
            quickAccessParameters.jUnitCheckTimeoutSpinner, 2, 1
        ),
        Arguments.of(
            quickAccessParameters.populationValueUpArrow, quickAccessParameters.populationValueDownArrow,
            quickAccessParameters.populationValueSpinner, 3, 4
        )
    )

    // TODO: spinner modifications

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
