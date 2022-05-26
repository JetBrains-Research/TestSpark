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

    // The action link text in the quick access parameters of sidebar tool window
    private val advancedSettingsButton
        get() = actionLink(byXpath("//div[@class='ActionLink']"))
        
    // Find ProjectViewTree (the thing with files on the left side)
    val projectViewTree
        get() = actionLink(byXpath("//div[@class='ProjectViewTree']"))

    // Action to find coverage visualisation tab in toolWindow
    val coverageVisualisationTab
        get() = actionLink(byXpath("//div[@class='ContentTabLabel' and @text='Coverage Visualisation']"))

    // Action to find "Find in Files..." menu
    val findInFilesAction
        get() = actionLink(byXpath("//div[@text='Find in Files...']"))

    // Action to find "File Name" textField
    val findFileFileNameAction
        get() = textField(byXpath("//div[@class='JBTextAreaWithMixedAccessibleContext']"))

    // Action to find "File path" textField
    val findFilePathNameAction
        get() = textField(byXpath("//div[@class='FindPopupDirectoryChooser']//div[@class='BorderlessTextField']"))

    /**
     * Open file inside project.
     *
     * @param fileName name of file to open
     * @param projectName name of project
     */
    fun openProjectFile(fileName: String, projectName: String) {
        with(projectViewTree) {
            // Wait for file name to be found
            findText(projectName).rightClick()
        }
        findInFilesAction.click()
        findFileFileNameAction.text = fileName
        findFilePathNameAction.text = findFilePathNameAction.text + "\\src\\main"
        remoteRobot.keyboard { hotKey(KeyEvent.VK_ENTER) }
    }

    /**
     * Closes opened project file.
     */
    fun closeProjectFile() {
        actionLink(byXpath("//div[@class='SingleHeightLabel']//div[@class='InplaceButton']")).click()
    }

    /**
     * Change the quick access params to have max search time to 2 seconds.
     */
    fun changeQuickAccess() {
        find(QuickAccessParametersFixtures::class.java, timeout = Duration.ofSeconds(60)).apply {
            searchBudgetTypeComboBox.selectItem("Max time")
            searchBudgetValueSpinnerTextField.text = "2"
            saveButton.click()
        }
        find<JButtonFixture>(byXpath("//div[@text='OK']")).click()
    }

    /**
     * Run EvoSuite using a shortcut to generate tests for class
     */
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
    fun openSettings(toolWindowOpen: Boolean) {
        // Check if operating system is Mac
        if (remoteRobot.isMac()) { // If so then we need another way to open settings
            if (!toolWindowOpen) {
                openToolWindow.click() // Open sidebar tool window if it is not already open
                openToolWindow.click() // Open and close the tool window to get rid of...
                openToolWindow.click() // ... horizontal scroll bar that right on top of advanceSettingsButton
            }
            advancedSettingsButton.click() // Use action link in quick access parameters to open settings
        } else {
            openFileMenu.click()
            openSettingsAction.click()
        }
    }

    /**
     * Method to open the settings on IntelliJ
     *  through the action link in the quick access parameters in the sidebar tool window.
     */
    fun advancedSettingsButton() {
        advancedSettingsButton.click()
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
