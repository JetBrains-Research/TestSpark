package nl.tudelft.ewi.se.ciselab.testgenie.uiTest

import com.intellij.remoterobot.RemoteRobot
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.IdeaFrame
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.SettingsFrame
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.WelcomeFrame
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.utils.RemoteRobotExtension
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(RemoteRobotExtension::class)
class SettingsTest {

    @BeforeAll
    fun setUpAll(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        find(WelcomeFrame::class.java, timeout = Duration.ofSeconds(15)).apply {
            open("untitled")
        }

        find(IdeaFrame::class.java, timeout = Duration.ofSeconds(15)).apply {
            openSettings()
        }
    }

    @Test
    fun checkHelloMessage(remoteRobot: RemoteRobot) = with(remoteRobot) {
        assertTrue(true)
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
