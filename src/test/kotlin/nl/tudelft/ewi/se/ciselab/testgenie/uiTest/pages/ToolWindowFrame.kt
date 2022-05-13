package nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.DefaultXpath
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.search.locators.byXpath

@FixtureName("Tool Window Frame")
@DefaultXpath(
    "type", "//div[@accessiblename='Generated Tests Tool Window']"
) // TODO: rename to "Parameters Tool Window Frame" when we remove "Generated Tests" tab
class ToolWindowFrame(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) :
    CommonContainerFixture(remoteRobot, remoteComponent) {

    // The tab label "Parameters"
    val parametersTab
        get() = actionLink(byXpath("//div[@text='Parameters']"))

    // The actual panel
    val quickAccessParametersContent
        get() = actionLink(byXpath("//div[@class='DumbUnawareHider']"))

    // The title "Quick Access Parameters"
    val title
        get() = jLabel(byXpath("//div[@javaclass='javax.swing.JLabel']"))
    val searchBudgetSeparator
        get() = jLabel(byXpath("//div[@accessiblename='Search budget' and @class='JLabel' and @text='Search budget']"))
    val searchBudgetType
        get() = jLabel(byXpath("//div[@text='Search budget type']"))
    val searchBudgetValue
        get() = jLabel(byXpath("//div[@accessiblename='Search budget' and @class='JBLabel' and @text='Search budget']"))
    val timeoutsSeparator
        get() = jLabel(byXpath("//div[@text='Timeouts']"))
    val initializationTimeout
        get() = jLabel(byXpath("//div[@text='Initialization timeout']"))
    val minimisationTimeout
        get() = jLabel(byXpath("//div[@text='Minimisation timeout']"))
    val assertionTimeout
        get() = jLabel(byXpath("//div[@text='Assertion timeout']"))
    val jUnitCheckTimeout
        get() = jLabel(byXpath("//div[@text='JUnit check timeout']"))
    val geneticAlgorithmSeparator
        get() = jLabel(byXpath("//div[@text='Genetic Algorithm']"))
    val populationLimit
        get() = jLabel(byXpath("//div[@text='Population limit']"))
    val populationValue
        get() = jLabel(byXpath("//div[@text='Population']"))
    val actionLink
        get() = actionLink(byXpath("//div[@class='ActionLink']"))
    val saveButton
        get() = button(byXpath("//div[@text='Save']"))
    val resetButton
        get() = button(byXpath("//div[@text='Reset']"))

    /**
     * Clicks on the "Parameters" tab to open the panel with parameters.
     */
    fun openQuickAccessParametersTab() {
        parametersTab.click()
    }
}
