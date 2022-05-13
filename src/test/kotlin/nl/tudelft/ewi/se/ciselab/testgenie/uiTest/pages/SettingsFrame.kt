package nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.ContainerFixture
import com.intellij.remoterobot.fixtures.DefaultXpath
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.utils.waitFor
import java.time.Duration

/**
 * Class to hold the Settings frame.
 *
 * @param remoteRobot the robot used for actions
 * @param remoteComponent the component associated with the class
 */

@FixtureName("Settings Frame")
@DefaultXpath("type", "//div[@class='MyDialog' and @title='Settings']")
class SettingsFrame(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) :
    CommonContainerFixture(remoteRobot, remoteComponent) {

    // Action to close Settings menu
    private val closeSettingsAction
        get() = actionLink(byXpath("//div[@class='DialogHeader']//div[@class='JButton']"))

    // Action for search text filed in Settings menu
    val searchTextBox
        get() = textField(byXpath("//div[@class='SettingsSearch']//div[@class='TextFieldWithProcessing']"))

    // Action to find Settings tree view
    val projectViewTree
        get() = find<ContainerFixture>(byXpath("//div[@class='SettingsTreeView']"))

    // Action for introduction label
    val introLabel
        get() = jLabel(byXpath("//div[@class='JLabel' and @text='<html><body>TestGenie is an external graphical IntelliJ plugin that integrates EvoSuite into the IDE. EvoSuite is a tool that automatically generates test cases with assertions for classes written in Java code. TestGenie makes this much easier, as it provides an intuitive modern interface for EvoSuite – so, no more CLI.']"))

    // Action for coverage checkbox
    val coverageCheckBox
        get() = checkBox(byXpath("//div[@class='JCheckBox' and @text='Show visualised coverage']"))

    // Action for separator of coverage
    val generalSettingsSeparator
        get() = actionLink(byXpath("//div[@class='JXTitledSeparator'][.//div[@text='General settings']]"))

    // Action for search algorithm label
    val searchAlgorithmLabel
        get() = jLabel(byXpath("//div[@text='Select search algorithm']"))

    // Action for search algorithm combo box
    val searchAlgorithmComboBox
        get() = comboBox(byXpath("//div[@class='ComboBox']"))

    // Action for seed label
    val seedLabel
        get() = jLabel(byXpath("//div[@text='Seed(random if left empty) ']"))

    // Action for configuration id label
    val configurationIdLabel
        get() = jLabel(byXpath("//div[@text='Select configuration id (null if left empty) ']"))

    // Action for Execute tests in a sandbox environment checkbox
    val executeTestsCheckbox
        get() = checkBox(byXpath("//div[@class='JCheckBox' and @text='Line coverage']"))

    // Action for Debug mode checkbox
    val debugModeCheckbox
        get() = checkBox(byXpath("//div[@class='JCheckBox' and @text='Line coverage']"))

    // Action for Minimize test suite after generation checkbox
    val minimiseTestSuiteCheckBox
        get() = checkBox(byXpath("//div[@class='JCheckBox' and @text='Line coverage']"))

    // Action for Flaky tests checkbox
    val flakyTestCheckBox
        get() = checkBox(byXpath("//div[@class='JCheckBox' and @text='Line coverage']"))

    // Action for separator of coverage
    val coverageSeparator
        get() = actionLink(byXpath("//div[@class='JXTitledSeparator'][.//div[@text='Criterion selection']]"))

    // Action for Line coverage checkbox
    val lineCoverageCheckBox
        get() = checkBox(byXpath("//div[@class='JCheckBox' and @text='Line coverage']"))

    // Action for Branch coverage checkbox
    val branchCoverageCheckBox
        get() = checkBox(byXpath("//div[@class='JCheckBox' and @text='Branch coverage']"))

    // Action for Exception coverage checkbox
    val exceptionCoverageCheckBox
        get() = checkBox(byXpath("//div[@class='JCheckBox' and @text='Exception coverage']"))

    // Action for Mutation coverage checkbox
    val mutationCoverageCheckBox
        get() = checkBox(byXpath("//div[@class='JCheckBox' and @text='Mutation coverage']"))

    // Action for Output coverage checkbox
    val outputCoverageCheckBox
        get() = checkBox(byXpath("//div[@class='JCheckBox' and @text='Output coverage']"))

    // Action for Method coverage checkbox
    val methodCoverageCheckBox
        get() = checkBox(byXpath("//div[@class='JCheckBox' and @text='Method coverage']"))

    // Action for Method no exception coverage checkbox
    val methodNoExcCoverageCheckBox
        get() = checkBox(byXpath("//div[@class='JCheckBox' and @text='Method no exception coverage']"))

    // Action for CBranch coverage checkbox
    val cBranchCoverageCheckBox
        get() = checkBox(byXpath("//div[@class='JCheckBox' and @text='CBranch coverage']"))

    /**
     * Search for TestGenie in Settings.
     */
    fun findTestGenie() {
        waitFor(Duration.ofSeconds(15)) {
            searchTextBox.text = "TestGenie"
            if (searchTextBox.text == "TestGenie") {
                return@waitFor true
            }
            return@waitFor false
        }
        with(projectViewTree) {
            if (hasText("TestGenie")) {
                findText("TestGenie").click()
            }
        }
    }

    /**
     * Search for EvoSuite in Settings.
     */
    fun findEvoSuite() {
        waitFor(Duration.ofSeconds(15)) {
            searchTextBox.text = "EvoSuite"
            if (searchTextBox.text == "EvoSuite") {
                return@waitFor true
            }
            return@waitFor false
        }
        with(projectViewTree) {
            if (hasText("EvoSuite")) {
                findText("EvoSuite").click()
            }
        }
    }

    /**
     * Method to open the settings of IntelliJ.
     */
    fun closeSettings() {
        closeSettingsAction.click()
    }
}
