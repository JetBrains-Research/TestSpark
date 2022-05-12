package nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.DefaultXpath
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.search.locators.byXpath

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
    private val searchTextBox
        get() = textField(byXpath("//div[@class='SettingsSearch']//div[@class='TextFieldWithProcessing']"))

    // Action for introduction label
    val introLabel
        get() = jLabel(byXpath("//div[@accessiblename='TestGenie is an external graphical IntelliJ plugin that integrates EvoSuite into the IDE. EvoSuite is a tool that automatically generates test cases with assertions for classes written in Java code. TestGenie makes this much easier, as it provides an intuitive modern interface for EvoSuite – so, no more CLI.' and @class='JLabel' and @text='<html><body>TestGenie is an external graphical IntelliJ plugin that integrates EvoSuite into the IDE. EvoSuite is a tool that automatically generates test cases with assertions for classes written in Java code. TestGenie makes this much easier, as it provides an intuitive modern interface for EvoSuite – so, no more CLI.']"))

    /**
     * Search for TestGenie in Settings.
     */
    fun findTestGenie() {
        searchTextBox.text = "TestGenie"
    }

    /**
     * Method to open the settings of IntelliJ.
     */
    fun closeSettings() {
        closeSettingsAction.click()
    }
}
