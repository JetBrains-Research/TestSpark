package nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.fixtures.ContainerFixture
import com.intellij.remoterobot.fixtures.DefaultXpath
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.utils.waitFor
import java.time.Duration

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
    private val openFileMenu
        get() = actionLink(byXpath("File", "//div[@class='ActionMenu' and @text='File']"))

    // Action to press "Close Projects"
    private val closeProjectAction
        get() = actionLink(byXpath("Close Project", "//div[@text='File']//div[@text='Close Project']"))

    // Action to press "Settings..."
    private val openSettingsAction
        get() = actionLink(byXpath("Close Project", "//div[@text='File']//div[@text='Settings...']"))

    private val inlineProgressPanel
        get() = find<ComponentFixture>(byXpath("//div[@class='InlineProgressPanel']"), Duration.ofSeconds(60))

    // Find TestGenie on the right sidebar
    private val openToolWindow
        get() = actionLink(byXpath("//div[@tooltiptext='TestGenie']"))

    private val projectTab
        get() = actionLink(byXpath("//div[@tooltiptext='Project']"))

    private val projectViewTree
        get() = find<ContainerFixture>(byXpath("//div[@class='ProjectViewTree']"))

    /**
     * Method to close the current project.
     */
    fun closeProject() {
        openFileMenu.click()
        closeProjectAction.click()
    }

    /**
     * Method to open the settings of IntelliJ.
     */
    fun openSettings() {
        openFileMenu.click()
        openSettingsAction.click()
    }

    /**
     * Method to click on TestGenie stripe button on the right sidebar.
     * First click opens the tool window, second click closes tool window.
     */
    fun clickOnToolWindow() {
        openToolWindow.click()
    }

    /**
     * Method to make the tests wait for the background tasks to finish.
     */
    fun waitForBackgroundTasks() {
        waitFor(Duration.ofMinutes(5), Duration.ofSeconds(10)) {
            val inlineProgressContents = inlineProgressPanel.findAllText()
            if (inlineProgressContents.isNotEmpty()) {
                return@waitFor false
            }
            return@waitFor true
        }
    }

    fun openProjectFromProjectTree() {
        projectTab.click()
        // projectViewTree.find()
        // projectViewTree.click()
        projectTab.click()
    }
}
