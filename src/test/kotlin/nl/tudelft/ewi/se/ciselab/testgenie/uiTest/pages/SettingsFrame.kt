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

    /**
     * Method to open the settings of IntelliJ.
     */
    fun closeSettings() {
        closeSettingsAction.click()
    }
}
