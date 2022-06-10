package nl.tudelft.ewi.se.ciselab.testgenie.uiTest

import com.automation.remarks.junit5.Video
import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.EditorFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.utils.keyboard
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.IdeaFrame
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.WelcomeFrame
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.utils.RemoteRobotExtension
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.extension.ExtendWith
import java.awt.event.KeyEvent
import java.time.Duration

private const val testFileName = "ArrayUtilsTest"

private const val projectName = "untitled"

private const val emptyClass = """public class ArrayUtilsTest {
}
"""

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(RemoteRobotExtension::class)
class GeneratedTestsToolWindowTest {

    private lateinit var editor: EditorFixture

    @BeforeAll
    fun setUpAll(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        find(WelcomeFrame::class.java, timeout = Duration.ofSeconds(60)).apply {
            open(projectName)
        }

        Thread.sleep(10000)

        find(IdeaFrame::class.java, timeout = Duration.ofSeconds(60)).apply {
            waitForBackgroundTasks()
            clickOnToolWindow()

            // Open file ArrayUtils in project untitled
            openProjectFile("ArrayUtils", projectName)

            // Change quick access params
            changeQuickAccess()
            // Create the test class
            createTestClass(testFileName)
            // Run EvoSuite on entire class
            runTestsForClass()
            // Wait for background tasks to finish
            Thread.sleep(5000)
            waitForBackgroundTasks()
        }
    }

    @Order(1)
    @Video
    @Test
    fun testGeneratedTestTab(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        val ideaFrame = find(IdeaFrame::class.java, timeout = Duration.ofSeconds(15))
        ideaFrame.apply {
            Assertions.assertThat(generatedTestsTab.hasText("Generated Tests")).isTrue
            Assertions.assertThat(validatedTests.hasText("Validate tests")).isTrue
            Assertions.assertThat(selectAll.hasText("Select All")).isTrue
            Assertions.assertThat(deselectAll.hasText("Deselect All")).isTrue
            Assertions.assertThat(applyToTestSuite.hasText("Apply to test suite")).isTrue
        }
    }

    @Order(2)
    @Video
    @Test
    fun testApplyTestsButtonDeselectAll(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        val ideaFrame = find(IdeaFrame::class.java, timeout = Duration.ofSeconds(15))
        ideaFrame.apply {
            // Deselect all and apply to testSuite.
            deselectAllApplyTestsToTestSuite(testFileName)
            // Open the correct file.
            openProjectFile(testFileName, projectName)
            // Close tool window to decrease the number of editors.
            clickOnToolWindow()
            editor = find(byXpath("//div[@class='EditorComponentImpl']"))
            // Assert that the test file only has the class signature and nothing else.
            Assertions.assertThat(editor.text).isEqualTo(emptyClass)
            // Close the ArrayUtilsTest file tab.
            remoteRobot.keyboard { hotKey(KeyEvent.VK_CONTROL, KeyEvent.VK_F4) }
            // Restore the state as before this test by opening the tool window again.
            clickOnToolWindow()
        }
    }

    @Order(3)
    @Video
    @Test
    fun testApplyTestsButtonSelectAll(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        val ideaFrame = find(IdeaFrame::class.java, timeout = Duration.ofSeconds(15))
        ideaFrame.apply {
            // Select all and apply to testSuite.
            selectAllApplyTestsToTestSuite(testFileName)
            // Open the correct file.
            openProjectFile(testFileName, projectName)
            // Close tool window to decrease the number of editors.
            clickOnToolWindow()
            editor = find(byXpath("//div[@class='EditorComponentImpl']"))
            // Assert that tests were appended by checking the length of empty class and the new class.
            Assertions.assertThat(editor.text.length).isGreaterThan(emptyClass.length)
            // Close the ArrayUtilsTest file tab.
            remoteRobot.keyboard { hotKey(KeyEvent.VK_CONTROL, KeyEvent.VK_F4) }
            // Restore the state as before this test by opening the tool window again.
            clickOnToolWindow()
        }
    }

    @AfterAll
    fun closeAll(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        find(IdeaFrame::class.java, timeout = Duration.ofSeconds(60)).apply {
            deleteProject(testFileName, projectName)
            clickOnToolWindow()
            closeProjectFile()
            closeProject()
        }
    }
}
