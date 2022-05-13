package nl.tudelft.ewi.se.ciselab.testgenie.uiTest

import com.intellij.remoterobot.RemoteRobot
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.IdeaFrame
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.QuickAccessParametersFrame
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.SettingsFrame
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.WelcomeFrame
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.utils.RemoteRobotExtension
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(RemoteRobotExtension::class)
class QuickAccessParametersTest {
    @BeforeAll
    fun setUpAll(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        find(WelcomeFrame::class.java, timeout = Duration.ofSeconds(15)).apply {
            open("untitled")
        }

        find(IdeaFrame::class.java, timeout = Duration.ofSeconds(15)).apply {
            waitForBackgroundTasks()
            openToolWindow()
        }
    }

    @Order(1)
    @Test
    fun checkTestGenieToolWindowPanel(remoteRobot: RemoteRobot) = with(remoteRobot) {
        val quickAccessParametersFrame = find(QuickAccessParametersFrame::class.java, timeout = Duration.ofSeconds(15))
        quickAccessParametersFrame.openQuickAccessParameters()
        Assertions.assertTrue(quickAccessParametersFrame.titleGetFindJLabel.isShowing)
    }

    @AfterAll
    fun closeAll(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        find(SettingsFrame::class.java, timeout = Duration.ofSeconds(15)).apply {
            closeSettings()
        }

        find(IdeaFrame::class.java, timeout = Duration.ofSeconds(15)).apply {
            closeProject()
        }
    }
}
