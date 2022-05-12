package nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.DefaultXpath
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.search.locators.byXpath

/**
 * Class to hold the Main Idea frame.
 *
 * @param remoteRobot the robot used for actions
 * @param remoteComponent the component associated with the class
 */
@FixtureName("Idea Frame")
@DefaultXpath("type", "//div[@class='IdeFrameImpl']")
class IdeaFrame(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) :
    CommonContainerFixture(remoteRobot, remoteComponent) {

    // Action to open file menu
    private val fileMenu
        get() = actionLink(byXpath("File", "//div[@class='ActionMenu' and @text='File']"))

    // Action to press "Close Projects"
    private val closeProjectAction
        get() = actionLink(byXpath("Close Project", "//div[@text='File']//div[@text='Close Project']"))

    /**
     * Method to close the current project.
     */
    fun close() {
        fileMenu.click()
        closeProjectAction.click()
    }
}
