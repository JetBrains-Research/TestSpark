package nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.*
import com.intellij.remoterobot.search.locators.byXpath

@FixtureName("Tool Window Frame")
@DefaultXpath(
    "type", "//div[@accessiblename='Parameters Tool Window']"
)
class ToolWindowFrame(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) :
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

    // The search budget separator line
    val searchBudgetSeparator
        get() = jLabel(byXpath("//div[@accessiblename='Search budget' and @class='JLabel' and @text='Search budget']"))

    // The search budget type text
    val searchBudgetType
        get() = jLabel(byXpath("//div[@text='Search budget type']"))

    // The search buget value text
    val searchBudgetValue
        get() = jLabel(byXpath("//div[@accessiblename='Search budget' and @class='JBLabel' and @text='Search budget']"))

    // The timeouts' separator line
    val timeoutsSeparator
        get() = jLabel(byXpath("//div[@text='Timeouts']"))

    // The initialization timeout text
    val initializationTimeout
        get() = jLabel(byXpath("//div[@text='Initialization timeout']"))

    // The minimisation timeout text
    val minimisationTimeout
        get() = jLabel(byXpath("//div[@text='Minimisation timeout']"))

    // The assertion timeout text
    val assertionTimeout
        get() = jLabel(byXpath("//div[@text='Assertion timeout']"))

    // The JUnit check timeout text
    val jUnitCheckTimeout
        get() = jLabel(byXpath("//div[@text='JUnit check timeout']"))

    // The genetic algorithm separator line
    val geneticAlgorithmSeparator
        get() = jLabel(byXpath("//div[@text='Genetic Algorithm']"))

    // The population limit text
    val populationLimit
        get() = jLabel(byXpath("//div[@text='Population limit']"))

    // The population value text
    val populationValue
        get() = jLabel(byXpath("//div[@text='Population']"))

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

    fun getUIElementLabels(): List<JLabelFixture> {
        return listOf(
            searchBudgetType, searchBudgetValue, initializationTimeout,
            minimisationTimeout, assertionTimeout, jUnitCheckTimeout,
            populationLimit, populationValue
        )
    }

    fun getButtons(): List<JButtonFixture> {
        return listOf(saveButton, resetButton)
    }
}
