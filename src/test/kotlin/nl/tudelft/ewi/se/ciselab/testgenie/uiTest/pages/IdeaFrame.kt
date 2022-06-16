package nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.fixtures.DefaultXpath
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.fixtures.JButtonFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.utils.WaitForConditionTimeoutException
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
    private val projectViewTree
        get() = actionLink(byXpath("//div[@class='ProjectViewTree']"))

    private val projectTab
        get() = actionLink(byXpath("//div[@tooltiptext='Project']"))

    // Action to find coverage visualisation tab in toolWindow
    val coverageVisualisationTab
        get() = actionLink(byXpath("//div[@class='ContentTabLabel' and @text='Coverage']"))

    // Action to find generated tests tab in toolWindow
    val generatedTestsTab
        get() = actionLink(byXpath("//div[@class='ContentTabLabel' and @text='Generated Tests']"))

    val validatedTests
        get() = actionLink(byXpath("//div[@text='Validate tests']"))

    val selectAll
        get() = actionLink(byXpath("//div[@text='Select All']"))

    val deselectAll
        get() = actionLink(byXpath("//div[@text='Deselect All']"))

    val applyToTestSuite
        get() = actionLink(byXpath("//div[@text='Apply to test suite']"))

    // Action to find "Find in Files..." menu
    private val findInFilesAction
        get() = actionLink(byXpath("//div[@text='Find in Files...']"))

    // Action to find "File Name" textField
    private val findFileFileNameAction
        get() = textField(byXpath("//div[@class='JBTextAreaWithMixedAccessibleContext']"))

    // Action to find "File path" textField
    private val findFilePathNameAction
        get() = textField(byXpath("//div[@class='FindPopupDirectoryChooser']//div[@class='BorderlessTextField']"))

    val searchForTestSuite
        get() = textField(byXpath("//div[@class='MyTextField']"))

    /**
     * Open file inside project.
     *
     * @param fileName name of file to open
     * @param projectName name of project
     */
    fun openProjectFile(fileName: String, projectName: String) {

        // Wait for file name to be found
        try {
            projectViewTree.findText(projectName).rightClick()
        } catch (e: WaitForConditionTimeoutException) {
            projectTab.click()
            projectViewTree.findText(projectName).rightClick()
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
     * Creates a class and closes the tab after its creation.
     * @param testFileName - the name of the file that you want to create.
     */
    fun createTestClass(testFileName: String) {
        // Click on the editor to ensure that we have access to the different functions
        actionLink(byXpath("//div[@class='EditorComponentImpl']")).click()
        // Do the double shift to trigger search everywhere/action window
        remoteRobot.keyboard { hotKey(KeyEvent.VK_SHIFT) }
        remoteRobot.keyboard { hotKey(KeyEvent.VK_SHIFT) }
        Thread.sleep(1000) // Some time to ensure that everything is loaded
        // Type Java Class in the search filed to create a new class
        textField(byXpath("//div[@class='SearchField']")).text = "Java Class"
        Thread.sleep(1000)
        // Select the option to create the new class by pressing "enter"
        remoteRobot.keyboard { hotKey(KeyEvent.VK_ENTER) }
        // Menu for creating a class will appear, enter the name of the file you want to create
        textField(byXpath("//div[@class='ExtendableTextField']")).text = testFileName
        Thread.sleep(1000)
        // Create the class
        remoteRobot.keyboard { hotKey(KeyEvent.VK_ENTER) }
        Thread.sleep(1000)
        // The git message pops up, escape it to ignore it. If message does not appear, this should not have any effect.
        remoteRobot.keyboard { hotKey(KeyEvent.VK_ESCAPE) }
        Thread.sleep(1000)
        // Close the newly created file tab
        remoteRobot.keyboard { hotKey(KeyEvent.VK_CONTROL, KeyEvent.VK_F4) }
    }

    /**
     * Deletes the specified file.
     * @param testFileName - the name of the file that you want to delete.
     * @param projectName - the name of the project in which the file is contained.
     */
    fun deleteProject(testFileName: String, projectName: String) {
        // Open the correct file
        openProjectFile(testFileName, projectName)
        // Function to trigger safe delete option
        remoteRobot.keyboard { hotKey(KeyEvent.VK_ALT, KeyEvent.VK_DELETE) }
        // Confirm the deletion of a file by pressing "OK"
        actionLink(byXpath("//div[@text='OK']")).click()
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

    /**
     * Opens the project by clicking on the file tabs in the projectViewTree.
     *
     * @param pathToMainFile an array of file tabs that the robot has to (double) click on to open the main file
     */
    fun openMainFileFromProjectTree(pathToMainFile: List<String>, mainClass: String) {
        projectTab.click()
        pathToMainFile.forEach {
            if (!projectViewTree.hasText(mainClass) || mainClass.startsWith(it)) {
                projectViewTree.findText(it).doubleClick()
            }
        }
    }

    /**
     * Closes the project by closing the file and clicking on the file tabs in the projectViewTree.
     *
     * @param pathFromMainFile an array of file tabs that the robot has to (double) click on to close them
     */
    fun closeMainFileFromProjectTree(pathFromMainFile: List<String>) {
        button(byXpath("//div[@class='InplaceButton']")).click()
        pathFromMainFile.forEach { projectViewTree.findText(it).doubleClick() }
        projectTab.click()
    }

    /**
     * Enter full screen mode via SHIFT-SHIFT shortcut.
     */
    fun goFullScreen() {
        remoteRobot.keyboard { hotKey(KeyEvent.VK_SHIFT) }
        remoteRobot.keyboard { hotKey(KeyEvent.VK_SHIFT) }
        val actionSearchBox = textField(byXpath("//div[@class='SearchField']"))
        actionSearchBox.text = "Enter Full Screen"
        Thread.sleep(2000L)
        remoteRobot.keyboard { hotKey(KeyEvent.VK_ENTER) }
    }

    /**
     * Exit full screen mode via SHIFT-SHIFT shortcut.
     */
    fun quitFullScreen() {
        remoteRobot.keyboard { hotKey(KeyEvent.VK_SHIFT) }
        remoteRobot.keyboard { hotKey(KeyEvent.VK_SHIFT) }
        val actionSearchBox = textField(byXpath("//div[@class='SearchField']"))
        actionSearchBox.text = "Exit Full Screen"
        Thread.sleep(2000L)
        remoteRobot.keyboard { hotKey(KeyEvent.VK_ENTER) }
    }

    /**
     * After the tests were generated and the tool window is open on the Generated tab,
     * the function enables the UI robot to press "Deselect All" button to select all the generated tests.
     * Clicks on the "Apply to test suite" button.
     * Specifying to which class the tests should be appended and applies the tests to that file.
     *
     * @param testFileName - the name of the file to which the tests should be added.
     */
    fun deselectAllApplyTestsToTestSuite(testFileName: String) {
        deselectAll.click()
        applyTestsToTestSuite(testFileName)
    }

    /**
     * After the tests were generated and the tool window is open on the Generated tab,
     * the function enables the UI robot to press "Select All" button to select all the generated tests.
     * Clicks on the "Apply to test suite" button.
     * Specifying to which class the tests should be appended and applies the tests to that file.
     *
     * @param testFileName - the name of the file to which the tests should be added.
     */
    fun selectAllApplyTestsToTestSuite(testFileName: String) {
        selectAll.click()
        applyTestsToTestSuite(testFileName)
    }

    /**
     * After the tests were generated and the tool window is open on the Generated tab,
     * the function enables the UI robot to click on the "Apply to test suite" button.
     * Specifying to which class the tests should be appended and applies the tests to that file.
     *
     * @param testFileName - the name of the file to which the tests should be added.
     */
    private fun applyTestsToTestSuite(testFileName: String) {
        applyToTestSuite.click()
        searchForTestSuite.text = testFileName
        Thread.sleep(1000)
        remoteRobot.keyboard { hotKey(KeyEvent.VK_ENTER) }
    }
}
