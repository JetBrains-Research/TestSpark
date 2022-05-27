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
     * Resets the values to defaults.
     * Correctness of the reset button is tested in `testResetButton` and thus is assumed to be correct in other tests.
     */
    private fun reset(): Unit = with(remoteRobot) {
        quickAccessParameters.resetButton.click()
        find<JButtonFixture>(byXpath("//div[@text='Yes']")).click()
        find<JButtonFixture>(byXpath("//div[@text='OK']")).click()
    }

    /**
     * Opens an untitled project from the IntelliJ welcome screen.
     * Then opens the TestGenie sidebar on the right.
     */
    @BeforeAll
    fun setUpAll(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        // Open an 'untitled' projectLabel
        find(WelcomeFrame::class.java, timeout = Duration.ofSeconds(15)).apply {
            open("pizzeria")
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
        // Find the tool window frame
        quickAccessParameters = find(QuickAccessParametersFixtures::class.java, timeout = Duration.ofSeconds(15))
    }

    @Order(1)
    @Test
    fun testEverythingIsVisible() {
        // Find the "Quick Access Parameters" tab
        quickAccessParameters.openQuickAccessParametersTab()

        // Assert that all titles and labels are visible
        quickAccessParameters.getTitles().forEach { assertThat(it.isVisible()).isTrue }
        quickAccessParameters.getUIElementLabels().forEach { assertThat(it.isVisible()).isTrue }

        // Assert that buttons are visible
        assertThat(quickAccessParameters.advancedSettingsButton.isShowing).isTrue
        quickAccessParameters.getButtons().forEach { assertThat(it.isShowing).isTrue }

        // Assert that all combo-boxes are visible
        quickAccessParameters.getComboBoxes().forEach { assertThat(it.isShowing).isTrue }

        // Assert that all spinners are visible
        quickAccessParameters.getJSpinners().forEach { assertThat(it.isShowing).isTrue }

        // Assert that the (show coverage) check-box is visible
        assertThat(quickAccessParameters.showCoverageCheckBox.isShowing).isTrue
        assertThat(quickAccessParameters.showCoverageCheckBox.text).isEqualTo("Show visualised coverage")
    }

    @Order(2)
    @ParameterizedTest
    @MethodSource("valueGeneratorForTestLabelsAreCorrect")
    fun testLabelsAreCorrect(expectedText: String, label: JLabelFixture) {
        assertThat(label.value).isEqualTo(expectedText)
    }

    private fun valueGeneratorForTestLabelsAreCorrect(): Stream<Arguments> = Stream.of(
        Arguments.of("Search budget type", quickAccessParameters.searchBudgetTypeLabel),
        Arguments.of("Search budget", quickAccessParameters.searchBudgetValueLabel),
        Arguments.of("Initialization timeout", quickAccessParameters.initializationTimeoutLabel),
        Arguments.of("Minimisation timeout", quickAccessParameters.minimizationTimeoutLabel),
        Arguments.of("Assertion timeout", quickAccessParameters.assertionTimeoutLabel),
        Arguments.of("JUnit check timeout", quickAccessParameters.jUnitCheckTimeoutLabel),
        Arguments.of("Population limit", quickAccessParameters.populationLimitLabel),
        Arguments.of("Population", quickAccessParameters.populationValueLabel)
    )

    @Order(3)
    @ParameterizedTest
    @MethodSource("valueGeneratorForTestTitlesAreCorrect")
    fun testTitlesAreCorrect(expectedText: String, title: JLabelFixture) {
        assertThat(title.value).isEqualTo(expectedText)
    }

    private fun valueGeneratorForTestTitlesAreCorrect(): Stream<Arguments> = Stream.of(
        Arguments.of("Quick Access Parameters", quickAccessParameters.title),
        Arguments.of("Search Budget", quickAccessParameters.searchBudgetSeparator),
        Arguments.of("Timeouts", quickAccessParameters.timeoutsSeparator),
        Arguments.of("Genetic Algorithm", quickAccessParameters.geneticAlgorithmSeparator)
    )

    @Order(4)
    @Test
    fun testDefaultToolTips(): Unit = with(remoteRobot) {
        // Default tooltips for `search budget type`, `initialization timeout` and `population limit` and `population` have unique (non-repeating) default values
        // They can be found individually
        assertThat(find<JLabelFixture>(byXpath("//div[@text='Default: Max time']")).isShowing).isTrue
        assertThat(find<JLabelFixture>(byXpath("//div[@text='Default: 120 seconds']")).isShowing).isTrue
        assertThat(find<JLabelFixture>(byXpath("//div[@text='Default: Individuals']")).isShowing).isTrue
        assertThat(find<JLabelFixture>(byXpath("//div[@text='Default: 50 individuals']")).isShowing).isTrue

        // Default tooltips for `search budget`, `minimisation timeout`, `assertion timeout`, `JUnit timeout` and have the same default value of 60 seconds.
        // They cannot be found individually. Since these are the only labels with the text `Default: 60 seconds` the number of found such labels is asserted
        assertThat(findAll<JLabelFixture>(byXpath("//div[@class='JBLabel' and @text='Default: 60 seconds']")).size).isEqualTo(4)
    }

    @Order(5)
    @ParameterizedTest
    @MethodSource("valueGeneratorForTestSearchBudgetComboBoxes")
    fun testSearchBudgetComboBoxes(choice: String): Unit = with(remoteRobot) {
        quickAccessParameters.searchBudgetTypeArrow.click()
        val choices: JListFixture = find(byXpath("//div[@class='JList']"), Duration.ofSeconds(15))
        choices.clickItem(choice)
        assertThat(quickAccessParameters.searchBudgetTypeComboBox.hasText(choice)).isTrue
    }

    private fun valueGeneratorForTestSearchBudgetComboBoxes(): Stream<Arguments> = Stream.of(
        Arguments.of("Max statements"),
        Arguments.of("Max tests"),
        Arguments.of("Max generations"),
        Arguments.of("Max fitness evaluations"),
        Arguments.of("Time delta"),
        Arguments.of("Max time"),
    )

    @Order(6)
    @ParameterizedTest
    @MethodSource("valueGeneratorForTestPopulationLimitComboBoxes")
    fun testPopulationLimitComboBoxes(choice: String): Unit = with(remoteRobot) {
        quickAccessParameters.populationLimitArrow.click()
        val choices: JListFixture = find(byXpath("//div[@class='JList']"), Duration.ofSeconds(15))
        choices.clickItem(choice)
        assertThat(quickAccessParameters.populationLimitComboBox.hasText(choice)).isTrue
    }

    private fun valueGeneratorForTestPopulationLimitComboBoxes(): Stream<Arguments> = Stream.of(
        Arguments.of("Tests"),
        Arguments.of("Statements"),
        Arguments.of("Individuals")
    )

    @Order(7)
    @ParameterizedTest
    @MethodSource("valueGeneratorForTestSpinnerOnValidInputs")
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

    private fun valueGeneratorForTestSpinnerOnValidInputs(): Stream<Arguments> {
        // A random number between 1 (inclusive) and 6 (exclusive) to avoid always writing `random.nextInt(1,6)`
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

    @Order(8)
    @ParameterizedTest
    @MethodSource("valueGeneratorForTestSpinnerOnInvalidInputs")
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

    private fun valueGeneratorForTestSpinnerOnInvalidInputs(): Stream<Arguments> = Stream.of(
        Arguments.of(quickAccessParameters.searchBudgetValueSpinner, quickAccessParameters.searchBudgetValueTooltip),
        Arguments.of(quickAccessParameters.initializationTimeoutSpinner, quickAccessParameters.initializationTimeoutTooltip),
        Arguments.of(quickAccessParameters.minimizationTimeoutSpinner, quickAccessParameters.minimizationTimeoutTooltip),
        Arguments.of(quickAccessParameters.assertionTimeoutSpinner, quickAccessParameters.assertionTimeoutTooltip),
        Arguments.of(quickAccessParameters.jUnitCheckTimeoutSpinner, quickAccessParameters.jUnitCheckTimeoutTooltip),
        Arguments.of(quickAccessParameters.populationValueSpinner, quickAccessParameters.populationValueTooltip)
    )

    @Order(9)
    @Test
    fun testCheckBox() {
        // Has to be true by default
        assertThat(quickAccessParameters.showCoverageCheckBox.isSelected()).isTrue
        // Assert that toggling works correctly
        quickAccessParameters.showCoverageCheckBox.click()
        assertThat(quickAccessParameters.showCoverageCheckBox.isSelected()).isFalse

        // Restore everything back
        quickAccessParameters.showCoverageCheckBox.click()
    }

    @Order(10)
    @Test
    fun testAdvancedSettings(): Unit = with(remoteRobot) {
        quickAccessParameters.advancedSettingsButton.click()

        find(SettingsFrame::class.java, Duration.ofSeconds(20)).apply {
            val breadcrumbs = jLabel(byXpath("//div[@class='Breadcrumbs' and @visible_text='Tools || TestGenie || EvoSuite']"))
            assertThat(breadcrumbs.isShowing).isTrue
            closeSettings()
        }
    }

    @Order(11)
    @Test
    fun testSaveButton(): Unit = with(remoteRobot) {
        // Set new values everywhere
        val showCoverageNew: Boolean = !quickAccessParameters.showCoverageCheckBox.isSelected()
        quickAccessParameters.showCoverageCheckBox.click()

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

        // Save the modified values
        quickAccessParameters.saveButton.click()
        find<JButtonFixture>(byXpath("//div[@text='OK']")).click()

        // Close the tool window and the project
        closeAll(remoteRobot)

        // Open the project and tool window again
        setUpAll(remoteRobot)

        // Assert that the state has been modified
        val separator: String = if (remoteRobot.isMac()) "." else "," // The separator of coma and fullstops is different on macOS
        assertThat(quickAccessParameters.showCoverageCheckBox.isSelected()).isEqualTo(showCoverageNew)
        assertThat(quickAccessParameters.searchBudgetTypeComboBox.hasText(searchBudgetTypeNew))
        assertThat(quickAccessParameters.searchBudgetValueSpinnerTextField.text.replace(separator, "")).isEqualTo(searchBudgetValueNew)
        assertThat(quickAccessParameters.initializationTimeoutSpinnerTextField.text.replace(separator, "")).isEqualTo(initializationTimeoutNew)
        assertThat(quickAccessParameters.minimizationTimeoutSpinnerTextField.text.replace(separator, "")).isEqualTo(minimizationTimeoutNew)
        assertThat(quickAccessParameters.assertionTimeoutSpinnerTextField.text.replace(separator, "")).isEqualTo(assertionTimeoutNew)
        assertThat(quickAccessParameters.jUnitCheckTimeoutSpinnerTextField.text.replace(separator, "")).isEqualTo(junitCheckTimeoutNew)
        assertThat(quickAccessParameters.populationLimitComboBox.hasText(populationLimitNew))
        assertThat(quickAccessParameters.populationValueSpinnerTextField.text.replace(separator, "")).isEqualTo(populationValueNew)

        // Restore everything back
        reset()
    }

    @Order(12)
    @Test
    fun testResetButton(): Unit = with(remoteRobot) {
        // Set new values everywhere
        val showCoverageNew: Boolean = !quickAccessParameters.showCoverageCheckBox.isSelected()
        quickAccessParameters.showCoverageCheckBox.click()

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
        val separator: String = if (remoteRobot.isMac()) "." else "," // The separator of coma and fullstops is different on macOS
        assertThat(quickAccessParameters.showCoverageCheckBox.isSelected()).isEqualTo(showCoverageNew)
        assertThat(quickAccessParameters.searchBudgetTypeComboBox.hasText(searchBudgetTypeNew))
        assertThat(quickAccessParameters.searchBudgetValueSpinnerTextField.text.replace(separator, "")).isEqualTo(searchBudgetValueNew)
        assertThat(quickAccessParameters.initializationTimeoutSpinnerTextField.text.replace(separator, "")).isEqualTo(initializationTimeoutNew)
        assertThat(quickAccessParameters.minimizationTimeoutSpinnerTextField.text.replace(separator, "")).isEqualTo(minimizationTimeoutNew)
        assertThat(quickAccessParameters.assertionTimeoutSpinnerTextField.text.replace(separator, "")).isEqualTo(assertionTimeoutNew)
        assertThat(quickAccessParameters.jUnitCheckTimeoutSpinnerTextField.text.replace(separator, "")).isEqualTo(junitCheckTimeoutNew)
        assertThat(quickAccessParameters.populationLimitComboBox.hasText(populationLimitNew))
        assertThat(quickAccessParameters.populationValueSpinnerTextField.text.replace(separator, "")).isEqualTo(populationValueNew)

        // Now actually reset the values to defaults and assert that it has been successful
        reset()
        assertThatDefaultValuesHaveBeenSet()
    }

    private fun assertThatDefaultValuesHaveBeenSet() {
        val defaultState = QuickAccessParametersState.DefaultState
        assertThat(quickAccessParameters.showCoverageCheckBox.isSelected()).isEqualTo(defaultState.showCoverage)
        assertThat(quickAccessParameters.searchBudgetTypeComboBox.hasText(defaultState.stoppingCondition.toString())).isTrue
        assertThat(quickAccessParameters.searchBudgetValueSpinnerTextField.hasText(defaultState.searchBudget.toString())).isTrue
        assertThat(quickAccessParameters.initializationTimeoutSpinnerTextField.hasText(defaultState.initializationTimeout.toString())).isTrue
        assertThat(quickAccessParameters.minimizationTimeoutSpinnerTextField.hasText(defaultState.minimizationTimeout.toString())).isTrue
        assertThat(quickAccessParameters.assertionTimeoutSpinnerTextField.hasText(defaultState.assertionTimeout.toString())).isTrue
        assertThat(quickAccessParameters.jUnitCheckTimeoutSpinnerTextField.hasText(defaultState.junitCheckTimeout.toString())).isTrue
        assertThat(quickAccessParameters.populationLimitComboBox.hasText(defaultState.populationLimit.toString())).isTrue
        assertThat(quickAccessParameters.populationValueSpinnerTextField.hasText(defaultState.population.toString())).isTrue
    }

    @Order(13)
    @ParameterizedTest
    @MethodSource("valueGeneratorForTestSearchBudgetUnitsAreChanging")
    fun testSearchBudgetUnitsAreChanging(item: StoppingCondition): Unit = with(remoteRobot) {
        val locator = byXpath("//div[@text='Default: 60 ${item.units()}']")
        val foundBefore: Int = findAll<JLabelFixture>(locator).size

        quickAccessParameters.searchBudgetTypeArrow.click()
        find<JListFixture>(byXpath("//div[@class='JList']"), Duration.ofSeconds(15)).clickItem(item.toString())
        val foundAfter: Int = findAll<JLabelFixture>(locator).size
        assertThat(foundAfter).isEqualTo(foundBefore + 1)
    }

    private fun valueGeneratorForTestSearchBudgetUnitsAreChanging(): Stream<Arguments> = Stream.of(
        Arguments.of(StoppingCondition.MAXSTATEMENTS), Arguments.of(StoppingCondition.MAXTESTS),
        Arguments.of(StoppingCondition.MAXGENERATIONS), Arguments.of(StoppingCondition.MAXFITNESSEVALUATIONS),
        // Note that the default value has been set the last to leave everything as it was
        Arguments.of(StoppingCondition.TIMEDELTA), Arguments.of(StoppingCondition.MAXTIME)
    )

    @Order(14)
    @ParameterizedTest
    @MethodSource("valueGeneratorForTestPopulationLimitUnitsAreChanging")
    fun testPopulationLimitUnitsAreChanging(item: PopulationLimit, units: String): Unit = with(remoteRobot) {
        val locator = byXpath("//div[@text='Default: 50 $units']")
        val foundBefore: Int = findAll<JLabelFixture>(locator).size

        quickAccessParameters.populationLimitArrow.click()
        find<JListFixture>(byXpath("//div[@class='JList']"), Duration.ofSeconds(15)).clickItem(item.toString())
        val foundAfter: Int = findAll<JLabelFixture>(locator).size
        assertThat(foundAfter).isEqualTo(foundBefore + 1)
    }

    private fun valueGeneratorForTestPopulationLimitUnitsAreChanging(): Stream<Arguments> = Stream.of(
        Arguments.of(PopulationLimit.TESTS, "tests"),
        Arguments.of(PopulationLimit.STATEMENTS, "statements"),
        // Note that the default value has been set the last to leave everything as it was
        Arguments.of(PopulationLimit.INDIVIDUALS, "individuals")
    )

    /**
     * First closes the TestGenie sidebar by clicking on the stripe button again.
     * Secondly, closes the project itself and returns the sandbox to IntelliJ welcome state.
     */
    @AfterAll
    fun closeAll(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        // Close the tool window tab
        find(IdeaFrame::class.java, timeout = Duration.ofSeconds(15)).apply {
            clickOnToolWindow()
        }

        // Close the project
        find(IdeaFrame::class.java, timeout = Duration.ofSeconds(15)).apply {
            closeProject()
        }
    }
}
