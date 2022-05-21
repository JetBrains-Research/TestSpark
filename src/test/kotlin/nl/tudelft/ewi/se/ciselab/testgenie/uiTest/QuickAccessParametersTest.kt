package nl.tudelft.ewi.se.ciselab.testgenie.uiTest

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.JButtonFixture
import com.intellij.remoterobot.fixtures.JLabelFixture
import com.intellij.remoterobot.fixtures.JListFixture
import com.intellij.remoterobot.fixtures.JTextFieldFixture
import com.intellij.remoterobot.search.locators.byXpath
import nl.tudelft.ewi.se.ciselab.testgenie.toolwindow.PopulationLimit
import nl.tudelft.ewi.se.ciselab.testgenie.toolwindow.QuickAccessParametersState
import nl.tudelft.ewi.se.ciselab.testgenie.toolwindow.StoppingCondition
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.customfixtures.BasicArrowButtonFixture
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.customfixtures.JSpinnerFixture
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.IdeaFrame
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.QuickAccessParametersFixtures
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.SettingsFrame
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

    private val random: Random = Random.Default

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
            "Minimisation timeout", quickAccessParameters.minimizationTimeoutLabel
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
        fun rand(): Int = random.nextInt(1, 6)

        return Stream.of(
            Arguments.of(
                quickAccessParameters.searchBudgetValueUpArrow, quickAccessParameters.searchBudgetValueDownArrow,
                quickAccessParameters.searchBudgetValueSpinner, rand(), rand(), quickAccessParameters.searchBudgetValueTooltip
            ),
            Arguments.of(
                quickAccessParameters.initializationTimeoutUpArrow, quickAccessParameters.initializationTimeoutDownArrow,
                quickAccessParameters.initializationTimeoutSpinner, rand(), rand(), quickAccessParameters.initializationTimeoutTooltip
            ),
            Arguments.of(
                quickAccessParameters.minimizationTimeoutUpArrow, quickAccessParameters.minimizationTimeoutDownArrow,
                quickAccessParameters.minimizationTimeoutSpinner, rand(), rand(), quickAccessParameters.minimizationTimeoutTooltip
            ),
            Arguments.of(
                quickAccessParameters.assertionTimeoutUpArrow, quickAccessParameters.assertionTimeoutDownArrow,
                quickAccessParameters.assertionTimeoutSpinner, rand(), rand(), quickAccessParameters.assertionTimeoutTooltip
            ),
            Arguments.of(
                quickAccessParameters.jUnitCheckTimeoutUpArrow, quickAccessParameters.jUnitCheckTimeoutDownArrow,
                quickAccessParameters.jUnitCheckTimeoutSpinner, rand(), rand(), quickAccessParameters.jUnitCheckTimeoutTooltip
            ),
            Arguments.of(
                quickAccessParameters.populationValueUpArrow, quickAccessParameters.populationValueDownArrow,
                quickAccessParameters.populationValueSpinner, rand(), rand(), quickAccessParameters.populationValueTooltip,
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
            quickAccessParameters.searchBudgetValueSpinner, quickAccessParameters.searchBudgetValueTooltip
        ),
        Arguments.of(
            quickAccessParameters.initializationTimeoutSpinner, quickAccessParameters.initializationTimeoutTooltip
        ),
        Arguments.of(
            quickAccessParameters.minimizationTimeoutSpinner, quickAccessParameters.minimizationTimeoutTooltip
        ),
        Arguments.of(
            quickAccessParameters.assertionTimeoutSpinner, quickAccessParameters.assertionTimeoutTooltip
        ),
        Arguments.of(
            quickAccessParameters.jUnitCheckTimeoutSpinner, quickAccessParameters.jUnitCheckTimeoutTooltip
        ),
        Arguments.of(
            quickAccessParameters.populationValueSpinner, quickAccessParameters.populationValueTooltip
        )
    )

    @Order(11)
    @Test
    fun testAdvancedSettings(): Unit = with(remoteRobot) {
        quickAccessParameters.advancedSettingsButton.click()

        find(SettingsFrame::class.java, Duration.ofSeconds(20)).apply {
            val breadcrumbs = jLabel(byXpath("//div[@class='Breadcrumbs' and @visible_text='Tools || TestGenie || EvoSuite']"))
            assertThat(breadcrumbs.isShowing)
            closeSettings()
        }
    }

    @Order(12)
    @Test
    fun testSaveButton(): Unit = with(remoteRobot) {
        // Set new values everywhere and store the old values to restore them at the end of the test

        val searchBudgetTypeOld: String = quickAccessParameters.searchBudgetTypeComboBox.data.getAll()[0].text
        val searchBudgetTypeNew: String = StoppingCondition.values()[random.nextInt(0, StoppingCondition.values().size)].toString()
        quickAccessParameters.searchBudgetTypeArrow.click()
        find<JListFixture>(byXpath("//div[@class='JList']"), Duration.ofSeconds(15)).clickItem(searchBudgetTypeNew)

        val searchBudgetValueOld: String = quickAccessParameters.searchBudgetValueSpinnerTextField.text
        val searchBudgetValueNew: String = random.nextInt(0, 10000).toString()
        quickAccessParameters.searchBudgetValueSpinnerTextField.text = searchBudgetValueNew

        val initializationTimeoutOld: String = quickAccessParameters.initializationTimeoutSpinnerTextField.text
        val initializationTimeoutNew: String = random.nextInt(0, 10000).toString()
        quickAccessParameters.initializationTimeoutSpinnerTextField.text = initializationTimeoutNew

        val minimizationTimeoutOld: String = quickAccessParameters.minimizationTimeoutSpinnerTextField.text
        val minimizationTimeoutNew: String = random.nextInt(0, 10000).toString()
        quickAccessParameters.minimizationTimeoutSpinnerTextField.text = minimizationTimeoutNew

        val assertionTimeoutOld: String = quickAccessParameters.assertionTimeoutSpinnerTextField.text
        val assertionTimeoutNew: String = random.nextInt(0, 10000).toString()
        quickAccessParameters.assertionTimeoutSpinnerTextField.text = assertionTimeoutNew

        val junitCheckTimeoutOld: String = quickAccessParameters.jUnitCheckTimeoutSpinnerTextField.text
        val junitCheckTimeoutNew: String = random.nextInt(0, 10000).toString()
        quickAccessParameters.jUnitCheckTimeoutSpinnerTextField.text = junitCheckTimeoutNew

        val populationLimitOld: String = quickAccessParameters.populationLimitComboBox.data.getAll()[0].text
        val populationLimitNew: String = PopulationLimit.values()[random.nextInt(0, PopulationLimit.values().size)].toString()
        quickAccessParameters.populationLimitArrow.click()
        find<JListFixture>(byXpath("//div[@class='JList']"), Duration.ofSeconds(15)).clickItem(populationLimitNew)

        val populationValueOld: String = quickAccessParameters.populationValueSpinnerTextField.text
        val populationValueNew: String = random.nextInt(0, 10000).toString()
        quickAccessParameters.populationValueSpinnerTextField.text = populationValueNew

        // Click somewhere else to make sure that the value in the last spinner is recorded
        quickAccessParameters.populationLimitArrow.doubleClick()

        // Save the modified values
        quickAccessParameters.saveButton.click()
        find<JButtonFixture>(byXpath("//div[@text='OK']")).click()

        // Close the tool window and the project
        closeAll(remoteRobot)

        // Open the project and tool window again
        setUpAll(remoteRobot)

        // Assert that the state has been modified
        assertThat(quickAccessParameters.searchBudgetTypeComboBox.data.getAll()[0].text).isEqualTo(searchBudgetTypeNew)
        assertThat(quickAccessParameters.searchBudgetValueSpinnerTextField.text).isEqualTo(searchBudgetValueNew)
        assertThat(quickAccessParameters.initializationTimeoutSpinnerTextField.text).isEqualTo(initializationTimeoutNew)
        assertThat(quickAccessParameters.minimizationTimeoutSpinnerTextField.text).isEqualTo(minimizationTimeoutNew)
        assertThat(quickAccessParameters.assertionTimeoutSpinnerTextField.text).isEqualTo(assertionTimeoutNew)
        assertThat(quickAccessParameters.jUnitCheckTimeoutSpinnerTextField.text).isEqualTo(junitCheckTimeoutNew)
        assertThat(quickAccessParameters.populationLimitComboBox.data.getAll()[0].text).isEqualTo(populationLimitNew)
        assertThat(quickAccessParameters.populationValueSpinnerTextField.text).isEqualTo(populationValueNew)

        // Restore everything back
        quickAccessParameters.searchBudgetTypeArrow.click()
        find<JListFixture>(byXpath("//div[@class='JList']"), Duration.ofSeconds(15)).clickItem(searchBudgetTypeOld)
        quickAccessParameters.searchBudgetValueSpinnerTextField.text = searchBudgetValueOld
        quickAccessParameters.initializationTimeoutSpinnerTextField.text = initializationTimeoutOld
        quickAccessParameters.minimizationTimeoutSpinnerTextField.text = minimizationTimeoutOld
        quickAccessParameters.assertionTimeoutSpinnerTextField.text = assertionTimeoutOld
        quickAccessParameters.jUnitCheckTimeoutSpinnerTextField.text = junitCheckTimeoutOld
        quickAccessParameters.populationLimitArrow.click()
        find<JListFixture>(byXpath("//div[@class='JList']"), Duration.ofSeconds(15)).clickItem(populationLimitOld)
        quickAccessParameters.populationValueSpinnerTextField.text = populationValueOld
        quickAccessParameters.populationLimitArrow.doubleClick()
    }

    @Order(13)
    @Test
    fun testResetButton(): Unit = with(remoteRobot) {
        // Set new values everywhere

        val searchBudgetTypeNew: String = StoppingCondition.values()[random.nextInt(0, StoppingCondition.values().size)].toString()
        quickAccessParameters.searchBudgetTypeArrow.click()
        find<JListFixture>(byXpath("//div[@class='JList']"), Duration.ofSeconds(15)).clickItem(searchBudgetTypeNew)

        val searchBudgetValueNew: String = random.nextInt(0, 10000).toString()
        quickAccessParameters.searchBudgetValueSpinnerTextField.text = searchBudgetValueNew

        val initializationTimeoutNew: String = random.nextInt(0, 10000).toString()
        quickAccessParameters.initializationTimeoutSpinnerTextField.text = initializationTimeoutNew

        val minimizationTimeoutNew: String = random.nextInt(0, 10000).toString()
        quickAccessParameters.minimizationTimeoutSpinnerTextField.text = minimizationTimeoutNew

        val assertionTimeoutNew: String = random.nextInt(0, 10000).toString()
        quickAccessParameters.assertionTimeoutSpinnerTextField.text = assertionTimeoutNew

        val junitCheckTimeoutNew: String = random.nextInt(0, 10000).toString()
        quickAccessParameters.jUnitCheckTimeoutSpinnerTextField.text = junitCheckTimeoutNew

        val populationLimitNew: String = PopulationLimit.values()[random.nextInt(0, PopulationLimit.values().size)].toString()
        quickAccessParameters.populationLimitArrow.click()
        find<JListFixture>(byXpath("//div[@class='JList']"), Duration.ofSeconds(15)).clickItem(populationLimitNew)

        val populationValueNew: String = random.nextInt(0, 10000).toString()
        quickAccessParameters.populationValueSpinnerTextField.text = populationValueNew

        // Click somewhere else to make sure that the value in the last spinner is recorded
        quickAccessParameters.populationLimitArrow.doubleClick()

        // Try to reset the modified values
        quickAccessParameters.resetButton.click()
        find<JButtonFixture>(byXpath("//div[@text='No']")).click()

        // Assert that nothing has been modified
        assertThat(quickAccessParameters.searchBudgetTypeComboBox.data.getAll()[0].text).isEqualTo(searchBudgetTypeNew)
        assertThat(quickAccessParameters.searchBudgetValueSpinnerTextField.text.replace(",", "")).isEqualTo(searchBudgetValueNew)
        assertThat(quickAccessParameters.initializationTimeoutSpinnerTextField.text.replace(",", "")).isEqualTo(initializationTimeoutNew)
        assertThat(quickAccessParameters.minimizationTimeoutSpinnerTextField.text.replace(",", "")).isEqualTo(minimizationTimeoutNew)
        assertThat(quickAccessParameters.assertionTimeoutSpinnerTextField.text.replace(",", "")).isEqualTo(assertionTimeoutNew)
        assertThat(quickAccessParameters.jUnitCheckTimeoutSpinnerTextField.text.replace(",", "")).isEqualTo(junitCheckTimeoutNew)
        assertThat(quickAccessParameters.populationLimitComboBox.data.getAll()[0].text).isEqualTo(populationLimitNew)
        assertThat(quickAccessParameters.populationValueSpinnerTextField.text.replace(",", "")).isEqualTo(populationValueNew)

        // Now actually reset the values to defaults
        quickAccessParameters.resetButton.click()
        find<JButtonFixture>(byXpath("//div[@text='Yes']")).click()
        find<JButtonFixture>(byXpath("//div[@text='OK']")).click()

        // Assert that the default values have been set
        val defaultState = QuickAccessParametersState.DefaultState
        assertThat(quickAccessParameters.searchBudgetTypeComboBox.data.getAll()[0].text).isEqualTo(defaultState.stoppingCondition.toString())
        assertThat(quickAccessParameters.searchBudgetValueSpinnerTextField.text).isEqualTo(defaultState.searchBudget.toString())
        assertThat(quickAccessParameters.initializationTimeoutSpinnerTextField.text).isEqualTo(defaultState.initializationTimeout.toString())
        assertThat(quickAccessParameters.minimizationTimeoutSpinnerTextField.text).isEqualTo(defaultState.minimizationTimeout.toString())
        assertThat(quickAccessParameters.assertionTimeoutSpinnerTextField.text).isEqualTo(defaultState.assertionTimeout.toString())
        assertThat(quickAccessParameters.jUnitCheckTimeoutSpinnerTextField.text).isEqualTo(defaultState.junitCheckTimeout.toString())
        assertThat(quickAccessParameters.populationLimitComboBox.data.getAll()[0].text).isEqualTo(defaultState.populationLimit.toString())
        assertThat(quickAccessParameters.populationValueSpinnerTextField.text).isEqualTo(defaultState.population.toString())
    }

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
