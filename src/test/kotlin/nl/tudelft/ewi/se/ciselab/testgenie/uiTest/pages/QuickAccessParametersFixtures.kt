package nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.ComboBoxFixture
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.DefaultXpath
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.fixtures.JButtonFixture
import com.intellij.remoterobot.fixtures.JLabelFixture
import com.intellij.remoterobot.search.locators.byXpath
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.customfixtures.JSpinnerFixture
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.customfixtures.basicArrowButton
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.customfixtures.jSpinner

@FixtureName("Quick Access Parameters")
@DefaultXpath(
    "type", "//div[@accessiblename='Parameters Tool Window']"
)
class QuickAccessParametersFixtures(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) :
    CommonContainerFixture(remoteRobot, remoteComponent) {

    // The tab label "Parameters"
    private val parametersTab
        get() = actionLink(byXpath("//div[@text='Parameters']"))

    // The title "Quick Access Parameters"
    val title
        get() = jLabel(byXpath("//div[@javaclass='javax.swing.JLabel']"))

    /**
     * SEARCH BUDGET SECTION
     */

    // The search budget separator line
    val searchBudgetSeparator
        get() = jLabel(byXpath("//div[@accessiblename='Search budget' and @class='JLabel' and @text='Search budget']"))

    /**
     * Search budget type
     */

    // The search budget type text
    val searchBudgetTypeLabel
        get() = jLabel(byXpath("//div[@text='Search budget type']"))

    // Tool tip for search budget type
    private val searchBudgetTypeTooltip: String = "What condition should be checked to end the search."

    // The search budget type combo-box
    val searchBudgetTypeComboBox
        get() = comboBox(byXpath("//div[@class='ComboBox' and @tooltiptext='$searchBudgetTypeTooltip']"))

    // The search budget type combo-box arrow
    val searchBudgetTypeArrow
        get() = basicArrowButton(byXpath("//div[@class='BasicArrowButton' and @tooltiptext='$searchBudgetTypeTooltip']"))

    /**
     * Search budget (value)
     */
    // The search budget value text
    val searchBudgetValueLabel
        get() = jLabel(byXpath("//div[@accessiblename='Search budget' and @class='JBLabel' and @text='Search budget']"))

    // Tool tip for search budget value
    val searchBudgetValueTooltip: String = "Maximum search duration."

    // The search budget value spinner
    val searchBudgetValueSpinner
        get() = jSpinner(byXpath("//div[@class='JSpinner' and @tooltiptext='$searchBudgetValueTooltip']"))

    // The search budget value text field of the spinner
    val searchBudgetValueSpinnerTextField
        get() = textField(byXpath("//div[@class='JFormattedTextField' and @name='Spinner.formattedTextField' and @tooltiptext='$searchBudgetValueTooltip']"))

    // The arrow to increase the value
    val searchBudgetValueUpArrow
        get() = basicArrowButton(byXpath("//div[@class='BasicArrowButton' and @name='Spinner.nextButton' and @tooltiptext='$searchBudgetValueTooltip']"))

    // The arrow to decrease the value
    val searchBudgetValueDownArrow
        get() = basicArrowButton(byXpath("//div[@class='BasicArrowButton' and @name='Spinner.previousButton' and @tooltiptext='$searchBudgetValueTooltip']"))

    /**
     * TIMEOUTS SECTION
     */

    // The timeouts' separator line
    val timeoutsSeparator
        get() = jLabel(byXpath("//div[@text='Timeouts']"))

    /**
     * Initialization timeout
     */

    // The initialization timeout text
    val initializationTimeoutLabel
        get() = jLabel(byXpath("//div[@text='Initialization timeout']"))

    // The tooltip for initialization timeout
    val initializationTimeoutTooltip: String = "Seconds allowed for initializing the search."

    // The initialization timeout spinner
    val initializationTimeoutSpinner
        get() = jSpinner(byXpath("//div[@class='JSpinner' and @tooltiptext='$initializationTimeoutTooltip']"))

    // The initialization timeout text field of the spinner
    val initializationTimeoutSpinnerTextField
        get() = textField(byXpath("//div[@class='JFormattedTextField' and @name='Spinner.formattedTextField' and @tooltiptext='$initializationTimeoutTooltip']"))

    // The arrow to increase the value
    val initializationTimeoutUpArrow
        get() = basicArrowButton(byXpath("//div[@class='BasicArrowButton' and @name='Spinner.nextButton' and @tooltiptext='$initializationTimeoutTooltip']"))

    // The arrow to decrease the value
    val initializationTimeoutDownArrow
        get() = basicArrowButton(byXpath("//div[@class='BasicArrowButton' and @name='Spinner.previousButton' and @tooltiptext='$initializationTimeoutTooltip']"))

    /**
     * Minimization timeout
     */

    // The minimization timeout text
    val minimizationTimeoutLabel
        get() = jLabel(byXpath("//div[@text='Minimisation timeout']"))

    // The tooltip for minimization timeout
    val minimizationTimeoutTooltip: String = "Seconds allowed for minimization at the end."

    // The minimization timeout spinner
    val minimizationTimeoutSpinner
        get() = jSpinner(byXpath("//div[@class='JSpinner' and @tooltiptext='$minimizationTimeoutTooltip']"))

    // The minimization timeout text field of the spinner
    val minimizationTimeoutSpinnerTextField
        get() = textField(byXpath("//div[@class='JFormattedTextField' and @name='Spinner.formattedTextField' and @tooltiptext='$minimizationTimeoutTooltip']"))

    // The arrow to increase the value
    val minimizationTimeoutUpArrow
        get() = basicArrowButton(byXpath("//div[@class='BasicArrowButton' and @name='Spinner.nextButton' and @tooltiptext='$minimizationTimeoutTooltip']"))

    // The arrow to decrease the value
    val minimizationTimeoutDownArrow
        get() = basicArrowButton(byXpath("//div[@class='BasicArrowButton' and @name='Spinner.previousButton' and @tooltiptext='$minimizationTimeoutTooltip']"))

    /**
     * Assertion timeout
     */

    // The assertion timeout text
    val assertionTimeoutLabel
        get() = jLabel(byXpath("//div[@text='Assertion timeout']"))

    // The tooltip for assertion timeout
    val assertionTimeoutTooltip: String = "Seconds allowed for assertion generation at the end."

    // The assertion timeout spinner
    val assertionTimeoutSpinner
        get() = jSpinner(byXpath("//div[@class='JSpinner' and @tooltiptext='$assertionTimeoutTooltip']"))

    // The assertion timeout text field of the spinner
    val assertionTimeoutSpinnerTextField
        get() = textField(byXpath("//div[@class='JFormattedTextField' and @name='Spinner.formattedTextField' and @tooltiptext='$assertionTimeoutTooltip']"))

    // The arrow to increase the value
    val assertionTimeoutUpArrow
        get() = basicArrowButton(byXpath("//div[@class='BasicArrowButton' and @name='Spinner.nextButton' and @tooltiptext='$assertionTimeoutTooltip']"))

    // The arrow to decrease the value
    val assertionTimeoutDownArrow
        get() = basicArrowButton(byXpath("//div[@class='BasicArrowButton' and @name='Spinner.previousButton' and @tooltiptext='$assertionTimeoutTooltip']"))

    /**
     * JUnit check timeout
     */

    // The JUnit check timeout text
    val jUnitCheckTimeoutLabel
        get() = jLabel(byXpath("//div[@text='JUnit check timeout']"))

    // The tooltip for JUnit check timeout
    val jUnitCheckTimeoutTooltip: String = "Seconds allowed for checking the generated JUnit files <p/>(e.g., compilation and stability)."

    // The JUnit check timeout spinner
    val jUnitCheckTimeoutSpinner
        get() = jSpinner(byXpath("//div[@class='JSpinner' and @tooltiptext='$jUnitCheckTimeoutTooltip']"))

    // The JUnit check timeout text field of the spinner
    val jUnitCheckTimeoutSpinnerTextField
        get() = textField(byXpath("//div[@class='JFormattedTextField' and @name='Spinner.formattedTextField' and @tooltiptext='$jUnitCheckTimeoutTooltip']"))

    // The arrow to increase the value
    val jUnitCheckTimeoutUpArrow
        get() = basicArrowButton(byXpath("//div[@class='BasicArrowButton' and @name='Spinner.nextButton' and @tooltiptext='$jUnitCheckTimeoutTooltip']"))

    // The arrow to decrease the value
    val jUnitCheckTimeoutDownArrow
        get() = basicArrowButton(byXpath("//div[@class='BasicArrowButton' and @name='Spinner.previousButton' and @tooltiptext='$jUnitCheckTimeoutTooltip']"))

    /**
     * GENETIC ALGORITHM SECTION
     */
    // The genetic algorithm separator line
    val geneticAlgorithmSeparator
        get() = jLabel(byXpath("//div[@text='Genetic Algorithm']"))

    /**
     * Population limit
     */

    // The population limit text
    val populationLimitLabel
        get() = jLabel(byXpath("//div[@text='Population limit']"))

    // The tooltip for population limit
    private val populationLimitTooltip: String = "What to use as limit for the population size."

    // The population limit combo-box
    val populationLimitComboBox
        get() = comboBox(byXpath("//div[@class='ComboBox' and @tooltiptext='$populationLimitTooltip']"))

    // The population limit combo-box arrow
    val populationLimitArrow
        get() = basicArrowButton(byXpath("//div[@class='BasicArrowButton' and @tooltiptext='$populationLimitTooltip']"))

    /**
     * Population value
     */

    // The population value text
    val populationValueLabel
        get() = jLabel(byXpath("//div[@text='Population']"))

    // The tooltip for population value
    val populationValueTooltip: String = "Population size of genetic algorithm."

    // The population value spinner
    val populationValueSpinner
        get() = jSpinner(byXpath("//div[@class='JSpinner' and @tooltiptext='$populationValueTooltip']"))

    // The population value text field of the spinner
    val populationValueSpinnerTextField
        get() = textField(byXpath("//div[@class='JFormattedTextField' and @name='Spinner.formattedTextField' and @tooltiptext='$populationValueTooltip']"))

    // The arrow to increase the value
    val populationValueUpArrow
        get() = basicArrowButton(byXpath("//div[@class='BasicArrowButton' and @name='Spinner.nextButton' and @tooltiptext='$populationValueTooltip']"))

    // The arrow to decrease the value
    val populationValueDownArrow
        get() = basicArrowButton(byXpath("//div[@class='BasicArrowButton' and @name='Spinner.previousButton' and @tooltiptext='$populationValueTooltip']"))

    /**
     * ACTION LINK AND BUTTONS
     */

    // The action link text
    val advancedSettingsButton
        get() = actionLink(byXpath("//div[@class='ActionLink']"))

    // The save button
    val saveButton
        get() = button(byXpath("//div[@text='Save']"))

    // The reset button
    val resetButton
        get() = button(byXpath("//div[@text='Reset']"))

    /**
     * Clicks on the "Parameters" tab to open the panel with parameters.
     */
    fun openQuickAccessParametersTab() {
        parametersTab.click()
    }

    /**
     * Gets all the titles, namely, the title and three category separators.
     *
     * @return the list of titles, as described above
     */
    fun getTitles(): List<JLabelFixture> {
        return listOf(
            title, searchBudgetSeparator,
            timeoutsSeparator, geneticAlgorithmSeparator
        )
    }

    /**
     * Gets the labels of the UI elements (checkboxes and spinners etc.).
     *
     * @return the list of labels of the UI elements
     */
    fun getUIElementLabels(): List<JLabelFixture> {
        return listOf(
            searchBudgetTypeLabel, searchBudgetValueLabel, initializationTimeoutLabel,
            minimizationTimeoutLabel, assertionTimeoutLabel, jUnitCheckTimeoutLabel,
            populationLimitLabel, populationValueLabel
        )
    }

    /**
     * Gets save and reset buttons in one list.
     *
     * @return the list with save and reset buttons
     */
    fun getButtons(): List<JButtonFixture> {
        return listOf(saveButton, resetButton)
    }

    /**
     * Gets the combo-boxes, for search budget type (aka stopping condition) and population limit.
     *
     * @return the list of check-boxes, as described above
     */
    fun getComboBoxes(): List<ComboBoxFixture> {
        return listOf(searchBudgetTypeComboBox, populationLimitComboBox)
    }

    /**
     * Gets the spinners, 1 for search budget, 4 for timeouts and 1 for population.
     *
     * @return the list of spinners, as described above
     */
    fun getJSpinners(): List<JSpinnerFixture> {
        return listOf(
            searchBudgetValueSpinner, initializationTimeoutSpinner,
            minimizationTimeoutSpinner, assertionTimeoutSpinner,
            jUnitCheckTimeoutSpinner, populationValueSpinner
        )
    }
}
