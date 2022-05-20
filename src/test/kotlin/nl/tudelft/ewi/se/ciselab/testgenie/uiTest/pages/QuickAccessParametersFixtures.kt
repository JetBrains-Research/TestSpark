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

    // The actual panel
    val quickAccessParametersContent
        get() = actionLink(byXpath("//div[@class='DumbUnawareHider']"))

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

    // The search budget type combo-box
    val searchBudgetTypeComboBox
        get() = comboBox(byXpath("//div[@class='ComboBox' and @tooltiptext='What condition should be checked to end the search.']"))

    // The search budget type combo-box arrow
    val searchBudgetTypeArrow
        get() = basicArrowButton(byXpath("//div[@class='BasicArrowButton' and @tooltiptext='What condition should be checked to end the search.']"))

    // The text with the default value for the search budget type
    val searchBudgetTypeDefaultTooltip
        get() = jLabel(byXpath("//div[@text='Default: Max time']"))

    /**
     * Search budget (value)
     */
    // The search budget value text
    val searchBudgetValueLabel
        get() = jLabel(byXpath("//div[@accessiblename='Search budget' and @class='JBLabel' and @text='Search budget']"))

    // The search budget value spinner
    private val searchBudgetValueSpinner
        get() = jSpinner(byXpath("//div[@class='JSpinner' and @tooltiptext='Maximum search duration.']"))

    // The text with the default value for the search budget value TODO: find it
//    val searchBudgetValueDefaultTooltip
//        get() = jLabel(byXpath(""))
//    get() = jLabel(byXpath("//div[@accessiblename='Search budget' and @class='JBLabel' and @text='Search budget']"))

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

    // The initialization timeout spinner
    private val initializationTimeoutSpinner
        get() = jSpinner(byXpath("//div[@class='JSpinner' and @tooltiptext='Seconds allowed for initializing the search.']"))

    // The text with the default value for the initialization timeout
    val initializationTimeoutDefaultTooltip
        get() = jLabel(byXpath("//div[@text='Default: 120 seconds']"))

    /**
     * Minimization timeout
     */

    // The minimisation timeout text
    val minimisationTimeoutLabel
        get() = jLabel(byXpath("//div[@text='Minimisation timeout']"))

    // The minimisation timeout spinner
    private val minimisationTimeoutSpinner
        get() = jSpinner(byXpath("//div[@class='JSpinner' and @tooltiptext='Seconds allowed for minimization at the end.']"))

    // The text with the default value for the minimisation timeout TODO: find it
//    val minimisationTimeoutDefaultTooltip
//        get() = jLabel(byXpath(""))

    /**
     * Assertion timeout
     */

    // The assertion timeout text
    val assertionTimeoutLabel
        get() = jLabel(byXpath("//div[@text='Assertion timeout']"))

    // The assertion timeout spinner
    private val assertionTimeoutSpinner
        get() = jSpinner(byXpath("//div[@class='JSpinner' and @tooltiptext='Seconds allowed for assertion generation at the end.']"))

    // The text with the default value for the assertion timeout TODO: find it
//    val assertionTimeoutDefaultTooltip
//        get() = jLabel(byXpath(""))

    /**
     * JUnit check timeout
     */

    // The JUnit check timeout text
    val jUnitCheckTimeoutLabel
        get() = jLabel(byXpath("//div[@text='JUnit check timeout']"))

    // The JUnit check timeout spinner
    private val jUnitCheckTimeoutSpinner
        get() = jSpinner(byXpath("//div[@class='JSpinner' and @tooltiptext='Seconds allowed for checking the generated JUnit files <p/>(e.g., compilation and stability).']"))

    // The text with the default value for the JUnit check timeout TODO: find it
//    val jUnitCheckTimeoutDefaultTooltip
//        get() = jLabel(byXpath(""))

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

    // The population limit combo-box
    val populationLimitComboBox
        get() = comboBox(byXpath("//div[@class='ComboBox' and @tooltiptext='What to use as limit for the population size.']"))

    // The population limit combo-box arrow
    val populationLimitArrow
        get() = basicArrowButton(byXpath("//div[@class='BasicArrowButton' and @tooltiptext='What to use as limit for the population size.']"))

    // The text with the default value for the population limit
    val populationLimitDefaultTooltip
        get() = jLabel(byXpath("//div[@text='Default: Individuals']"))

    /**
     * Population value
     */

    // The population value text
    val populationValueLabel
        get() = jLabel(byXpath("//div[@text='Population']"))

    // The population value spinner
    private val populationValueSpinner
        get() = jSpinner(byXpath("//div[@class='JSpinner' and @tooltiptext='Population size of genetic algorithm.']"))

    // The text with the default value for the population value TODO: find it
//    val populationValueDefaultTooltip
//        get() = jLabel(byXpath(""))

    /**
     * ACTION LINK AND BUTTONS
     */

    // The action link text
    val advancedSettingsButton
        get() = actionLink(byXpath("//div[@class='ActionLink']"))

    // The save button
    private val saveButton
        get() = button(byXpath("//div[@text='Save']"))

    // The reset button
    private val resetButton
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
            minimisationTimeoutLabel, assertionTimeoutLabel, jUnitCheckTimeoutLabel,
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
     * Gets the tooltips with default values of the parameters.
     *
     * @return the list of "default tooltips"
     */
    fun getDefaultTooltips(): List<JLabelFixture> {
        // TODO: pass others once they are found
        return listOf(
            searchBudgetTypeDefaultTooltip,
            initializationTimeoutDefaultTooltip,
            populationLimitDefaultTooltip
        )
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
            minimisationTimeoutSpinner, assertionTimeoutSpinner,
            jUnitCheckTimeoutSpinner, populationValueSpinner
        )
    }
}
