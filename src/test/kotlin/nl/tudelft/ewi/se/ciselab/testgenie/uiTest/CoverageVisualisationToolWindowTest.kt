package nl.tudelft.ewi.se.ciselab.testgenie.uiTest

import com.automation.remarks.junit5.Video
import com.intellij.remoterobot.RemoteRobot
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.IdeaFrame
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.WelcomeFrame
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.utils.RemoteRobotExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration

@TestMethodOrder(OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(RemoteRobotExtension::class)
class CoverageVisualisationToolWindowTest {

    @BeforeAll
    fun setUpAll(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        find(WelcomeFrame::class.java, timeout = Duration.ofSeconds(60)).apply {
            open("untitled")
        }

        Thread.sleep(10000)

        find(IdeaFrame::class.java, timeout = Duration.ofSeconds(60)).apply {
            waitForBackgroundTasks()
            clickOnToolWindow()

            // Open file ArrayUtils in project untitled
            openProjectFile("ArrayUtils", "untitled")

            // Change quick access params
            changeQuickAccess()
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
    fun testCoverageVisualisationTab(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        val ideaFrame = find(IdeaFrame::class.java, timeout = Duration.ofSeconds(15))
        ideaFrame.apply {
            coverageVisualisationTab.click()
            assertThat(coverageVisualisationTab.hasText("Coverage")).isTrue
        }
    }

    @AfterAll
    fun closeAll(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        find(IdeaFrame::class.java, timeout = Duration.ofSeconds(60)).apply {
            clickOnToolWindow()
            closeProjectFile()
            closeProject()
        }
    }
}
