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

    // Locator to Dialog for wrong seed
    val seedDialogLocator
        get() = byXpath("//div[@accessiblename='Incorrect Numeric Type For Seed' and @class='MyDialog']")

    // Action to close Settings menu and save changes
    private val okSettingsAction
        get() = button(byXpath("//div[@text='OK']"))

    // Action to save Settings changes without closing
    private val applySettingsAction
        get() = button(byXpath("//div[@text='Apply']"))

    // Action to cancel Settings menu changes
    private val cancelSettingsAction
        get() = button(byXpath("//div[@text='Cancel']"))

    // Action for search text filed in Settings menu
    val searchTextBox
        get() = textField(
            byXpath("//div[@class='SettingsSearch']//div[@class='TextFieldWithProcessing']"),
            Duration.ofSeconds(60)
        )

    // Action to find Settings tree view
    val projectViewTree
        get() = find<ContainerFixture>(byXpath("//div[@class='SettingsTreeView']"), Duration.ofSeconds(60))

    // Action for introduction label
    val introLabel
        get() = jLabel(byXpath("//div[@class='JLabel' and @text='<html><body>TestGenie is an external graphical IntelliJ plugin that integrates EvoSuite into the IDE. EvoSuite is a tool that automatically generates test cases with assertions for classes written in Java code. TestGenie makes this much easier, as it provides an intuitive modern interface for EvoSuite – so, no more CLI.']"))

    // Action for coverage checkbox
    val coverageCheckBox
        get() = checkBox(byXpath("//div[@class='JCheckBox' and @text='Show visualised coverage']"))

    // Action for separator "Environment settings"
    val environmentSettingsSeparator
        get() = actionLink(byXpath("//div[@class='JXTitledSeparator'][.//div[@text='Environment settings']]//div[@class='JSeparator']"))

    // Action for java path label
    val javaPathLabel
        get() = jLabel(byXpath("//div[@text='Java 11 path:']"))

    // Action for java path text field
    val javaPathField
        get() = textField(byXpath("//div[@tooltiptext='Path to a java binary']"))

    // Action for separator "Accessibility settings"
    val accessibilitySettingsSeparator
        get() = actionLink(byXpath("//div[@class='JXTitledSeparator'][.//div[@text='Accessibility settings']]//div[@class='JSeparator']"))

    // Action for color picker
    val colorPicker
        get() = actionLink(byXpath("//div[@class='JColorChooser']"))

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

    // Action for seed text field
    val seedTextField
        get() = textField(byXpath("//div[@tooltiptext='Leave empty if you want random seed']"))

    // Action for configuration id label
    val configurationIdLabel
        get() = jLabel(byXpath("//div[@text='Select configuration id (null if left empty) ']"))

    // Action for configuration id text field
    val configurationIdField
        get() = textField(byXpath("//div[@tooltiptext='Label that identifies the used configuration of EvoSuite. This is only done when running experiments.']"))

    // Action for Execute tests in a sandbox environment checkbox
    val executeTestsCheckbox
        get() = checkBox(byXpath("//div[@class='JCheckBox' and @text='Execute tests in a sandbox environment']"))

    // Action for Create assertions checkbox
    val createAssertionsCheckBox
        get() = checkBox(byXpath("//div[@class='JCheckBox' and @text='Create assertions']"))

    // Action for Debug mode checkbox
    val debugModeCheckbox
        get() = checkBox(byXpath("//div[@class='JCheckBox' and @text='Debug mode']"))

    // Action for Minimize test suite after generation checkbox
    val minimiseTestSuiteCheckBox
        get() = checkBox(byXpath("//div[@class='JCheckBox' and @text='Minimize test suite after generation']"))

    // Action for Flaky tests checkbox
    val flakyTestCheckBox
        get() = checkBox(byXpath("//div[@class='JCheckBox' and @text='Flaky tests']"))

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
        waitFor(Duration.ofSeconds(60)) {
            searchTextBox.text = "TestGenie"
            if (searchTextBox.text == "TestGenie") {
                return@waitFor true
            }
            return@waitFor false
        }

        waitFor(Duration.ofSeconds(5)) {
            with(projectViewTree) {
                if (hasText("TestGenie")) {
                    findText("TestGenie").click()
                    return@waitFor true
                } else {
                    return@waitFor false
                }
            }
        }
    }

    /**
     * Search for EvoSuite in Settings.
     */
    fun findEvoSuite() {
        waitFor(Duration.ofSeconds(60)) {
            searchTextBox.text = "EvoSuite"
            if (searchTextBox.text == "EvoSuite") {
                return@waitFor true
            }
            return@waitFor false
        }

        waitFor(Duration.ofSeconds(5)) {
            with(projectViewTree) {
                if (hasText("EvoSuite")) {
                    findText("EvoSuite").click()
                    return@waitFor true
                } else {
                    return@waitFor false
                }
            }
        }
    }

    /**
     * Method to press ok on settings.
     * This will apply the changes and close settings.
     */
    fun okSettings() {
        okSettingsAction.click()
    }

    /**
     * Method to apply settings changes without closing the window.
     */
    fun applySettings() {
        if (!applySettingsAction.isEnabled()) {
            applySettingsAction.click()
        }
        // Wait for apply button to be available
        waitFor(Duration.ofSeconds(10)) {
            applySettingsAction.isEnabled()
        }
        applySettingsAction.click()
    }

    /**
     * Method to open the settings of IntelliJ.
     */
    fun closeSettings() {
        cancelSettingsAction.click()
    }
}
