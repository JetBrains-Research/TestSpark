package nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.fixtures.DefaultXpath
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.fixtures.JButtonFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.utils.keyboard
import com.intellij.remoterobot.utils.waitFor
import java.awt.event.KeyEvent
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

    // Find ProjectViewTree (the thing with files on the left side)
    val projectViewTree
        get() = actionLink(byXpath("//div[@class='ProjectViewTree']"))

    fun openProjectFile(fileName: String, projectName: String) {
        with(projectViewTree) {
            // Wait for file name to be found
            findText(projectName).rightClick()
        }
        actionLink(byXpath("//div[@text='Find in Files...']")).click()
        val fileSearchTextField = textField(byXpath("//div[@class='JBTextAreaWithMixedAccessibleContext']"))
        fileSearchTextField.text = fileName
        remoteRobot.keyboard { hotKey(KeyEvent.VK_ENTER) }
    }

    fun closeProjectFile() {
        actionLink(byXpath("//div[@class='SingleHeightLabel']//div[@class='InplaceButton']")).click()
    }

    fun changeQuickAccess() {
        find(QuickAccessParametersFixtures::class.java, timeout = Duration.ofSeconds(60)).apply {
            searchBudgetTypeComboBox.selectItem("Max time")
            searchBudgetValueSpinnerTextField.text = "2"
            saveButton.click()
        }
        find<JButtonFixture>(byXpath("//div[@text='OK']")).click()
    }

    fun runTestsForClass() {
        actionLink(byXpath("//div[@class='EditorComponentImpl']")).click()
        remoteRobot.keyboard { hotKey(KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT, KeyEvent.VK_ALT, KeyEvent.VK_G) }
        remoteRobot.keyboard { hotKey(KeyEvent.VK_C) }
    }

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
}
