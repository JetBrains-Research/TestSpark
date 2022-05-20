package nl.tudelft.ewi.se.ciselab.testgenie.uiTest

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.JLabelFixture
import com.intellij.remoterobot.fixtures.JListFixture
import com.intellij.remoterobot.fixtures.JTextFieldFixture
import com.intellij.remoterobot.search.locators.byXpath
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.customfixtures.BasicArrowButtonFixture
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.customfixtures.JSpinnerFixture
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.IdeaFrame
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.QuickAccessParametersFixtures
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.WelcomeFrame
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.utils.RemoteRobotExtension
import org.assertj.core.api.Assertions.assertThat
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
import kotlin.random.Random

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
        Thread.sleep(10000)
        // Open the TestGenie tool window
        find(IdeaFrame::class.java, timeout = Duration.ofSeconds(15)).apply {
            clickOnToolWindow()
        }
        quickAccessParameters = find(QuickAccessParametersFixtures::class.java, timeout = Duration.ofSeconds(15))
        quickAccessParameters.openQuickAccessParametersTab()
    }

    @BeforeEach
    fun setUp(_remoteRobot: RemoteRobot): Unit = with(_remoteRobot) {
        remoteRobot = _remoteRobot
        // Open the tool window frame
        quickAccessParameters = find(QuickAccessParametersFixtures::class.java, timeout = Duration.ofSeconds(15))
    }

    @Order(1)
    @Test
    fun testEverythingIsVisible() {
        // Open the "Quick Access Parameters" tab
        quickAccessParameters.openQuickAccessParametersTab()

        // Assert text is visible
        quickAccessParameters.getTitles().forEach { assertThat(it.isVisible()) }
        quickAccessParameters.getUIElementLabels().forEach { assertThat(it.isVisible()) }

        // Assert buttons are visible
        assertThat(quickAccessParameters.advancedSettingsButton.isShowing).isTrue
        quickAccessParameters.getButtons().forEach { assertThat(it.isShowing) }
    }

    @Order(2)
    @ParameterizedTest
    @MethodSource("valueGeneratorForLabelsAndTexts")
    fun testLabelsAreCorrect(expectedText: String, label: JLabelFixture) {
        // Assert labels have the correct text
        assertThat(label.value).isEqualTo(expectedText)
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
        assertThat(title.value).isEqualTo(expectedText)
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
        assertThat(tooltipLabel.value).isEqualTo(expectedText)
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
        quickAccessParameters.getJSpinners().forEach { assertThat(it.isShowing) }
    }

    @Order(7)
    @ParameterizedTest
    @MethodSource("valueGeneratorForSearchBudgetTypeComboBoxChoices")
    fun testSearchBudgetComboBoxes(choice: String): Unit = with(remoteRobot) {
        quickAccessParameters.searchBudgetTypeArrow.click()
        val choices: JListFixture = find(byXpath("//div[@class='JList']"), Duration.ofSeconds(15))
        choices.clickItem(choice)
        assertThat(quickAccessParameters.searchBudgetTypeComboBox.hasText(choice))
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
        assertThat(quickAccessParameters.populationLimitComboBox.hasText(choice))
    }

    private fun valueGeneratorForPopulationLimitComboBoxChoices(): Stream<Arguments> = Stream.of(
        Arguments.of("Tests"),
        Arguments.of("Statements"),
        Arguments.of("Individuals")
    )

    @Order(9)
    @ParameterizedTest
    @MethodSource("valueGeneratorForSpinnersOnValidInputs")
    fun testSpinnerOnValidInputs(
        arrowUp: BasicArrowButtonFixture,
        arrowDown: BasicArrowButtonFixture,
        spinner: JSpinnerFixture,
        clicksUp: Int,
        clicksDown: Int,
        toolTipText: String
    ) {
        with(remoteRobot) {
            // Get the actual interior text field
            val textField: JTextFieldFixture = find(byXpath("//div[@class='JFormattedTextField' and @name='Spinner.formattedTextField' and @tooltiptext='$toolTipText']"))

            // Save the old value to restore it later
            val oldValue: Int = textField.text.toInt()

            // Increment the specified number of times
            for (i in 1..clicksUp) {
                arrowUp.click()
            }
            assertThat(spinner.data.getAll()[0].text.toInt()).isEqualTo(oldValue + clicksUp)

            // Decrement the specified number of times
            for (i in 1..clicksDown) {
                arrowDown.click()
            }
            assertThat(textField.text.toInt()).isEqualTo(oldValue + clicksUp - clicksDown)

            // Set back the old value
            textField.text = oldValue.toString()
            // Click on another non-label element to make sure the value of the spinner has been updated (this is required)
            quickAccessParameters.searchBudgetTypeComboBox.doubleClick()
        }
    }

    private fun valueGeneratorForSpinnersOnValidInputs(): Stream<Arguments> {
        fun rand(): Int = Random.nextInt(1, 6)

        return Stream.of(
            Arguments.of(
                quickAccessParameters.searchBudgetValueUpArrow, quickAccessParameters.searchBudgetValueDownArrow,
                quickAccessParameters.searchBudgetValueSpinner, rand(), rand(), "Maximum search duration."
            ),
            Arguments.of(
                quickAccessParameters.initializationTimeoutUpArrow, quickAccessParameters.initializationTimeoutDownArrow,
                quickAccessParameters.initializationTimeoutSpinner, rand(), rand(), "Seconds allowed for initializing the search."
            ),
            Arguments.of(
                quickAccessParameters.minimisationTimeoutUpArrow, quickAccessParameters.minimisationTimeoutDownArrow,
                quickAccessParameters.minimisationTimeoutSpinner, rand(), rand(), "Seconds allowed for minimization at the end."
            ),
            Arguments.of(
                quickAccessParameters.assertionTimeoutUpArrow, quickAccessParameters.assertionTimeoutDownArrow,
                quickAccessParameters.assertionTimeoutSpinner, rand(), rand(), "Seconds allowed for assertion generation at the end."
            ),
            Arguments.of(
                quickAccessParameters.jUnitCheckTimeoutUpArrow, quickAccessParameters.jUnitCheckTimeoutDownArrow,
                quickAccessParameters.jUnitCheckTimeoutSpinner, rand(), rand(), "Seconds allowed for checking the generated JUnit files <p/>(e.g., compilation and stability)."
            ),
            Arguments.of(
                quickAccessParameters.populationValueUpArrow, quickAccessParameters.populationValueDownArrow,
                quickAccessParameters.populationValueSpinner, rand(), rand(), "Population size of genetic algorithm."
            )
        )
    }

    @Order(10)
    @ParameterizedTest
    @MethodSource("valueGeneratorForSpinnersOnInvalidInputs")
    fun testSpinnerOnInvalidInputs(spinner: JSpinnerFixture, toolTipText: String): Unit = with(remoteRobot) {
        // Get the actual interior text field
        val textField: JTextFieldFixture = find(byXpath("//div[@class='JFormattedTextField' and @name='Spinner.formattedTextField' and @tooltiptext='$toolTipText']"))

        // Save the value to restore it later
        val oldValue: String = textField.text

        // Update the value of the spinner to something invalid
        textField.text = "gibberish"
        // Click on another non-label element to make sure the value of the spinner has been updated (this is required)
        quickAccessParameters.searchBudgetTypeComboBox.doubleClick()
        // Assert that the old value is set instead
        assertThat(textField.text).isEqualTo(oldValue)

        // Update the value of the spinner to no input
        textField.text = ""
        // Click on another non-label element to make sure the value of the spinner has been updated (this is required)
        quickAccessParameters.searchBudgetTypeComboBox.doubleClick()
        // Assert that the old value is set instead
        assertThat(textField.text).isEqualTo(oldValue)
    }

    private fun valueGeneratorForSpinnersOnInvalidInputs(): Stream<Arguments> = Stream.of(
        Arguments.of(
            quickAccessParameters.searchBudgetValueSpinner, "Maximum search duration."
        ),
        Arguments.of(
            quickAccessParameters.initializationTimeoutSpinner, "Seconds allowed for initializing the search."
        ),
        Arguments.of(
            quickAccessParameters.minimisationTimeoutSpinner, "Seconds allowed for minimization at the end."
        ),
        Arguments.of(
            quickAccessParameters.assertionTimeoutSpinner, "Seconds allowed for assertion generation at the end."
        ),
        Arguments.of(
            quickAccessParameters.jUnitCheckTimeoutSpinner, "Seconds allowed for checking the generated JUnit files <p/>(e.g., compilation and stability)."
        ),
        Arguments.of(
            quickAccessParameters.populationValueSpinner, "Population size of genetic algorithm."
        )
    )

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
