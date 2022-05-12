package nl.tudelft.ewi.se.ciselab.testgenie.uiTest

import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.WelcomeFrame
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.utils.RemoteRobotExtension
import com.intellij.remoterobot.RemoteRobot
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.IdeaFrame
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration

// The code here was copied from JetBrains/intellij-ui-test-robot library, in order to experiment with the UI testing.
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(RemoteRobotExtension::class)
class SayHelloKotlinTest {

    @BeforeAll
    fun setUpAll(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        find(WelcomeFrame::class.java).apply {
            open("untitled")
            Thread.sleep(30000)
        }
    }

    @Test
    fun checkHelloMessage(remoteRobot: RemoteRobot) = with(remoteRobot) {
        find(WelcomeFrame::class.java, timeout = Duration.ofSeconds(10)).apply {
            if (hasText("Welcome to IntelliJ IDEA'")) {
                findText("Welcome to IntelliJ IDEA'").click()
            } else {
                moreActions.click()
                heavyWeightPopup.findText("Say Hello").click()
            }
        }

        val helloDialog = find(HelloWorldDialog::class.java)

        assert(helloDialog.textPane.hasText("Hello World!"))
        helloDialog.ok.click()
    }

    @AfterAll
    fun closeAll(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        find(IdeaFrame::class.java, timeout = Duration.ofSeconds(30)).apply {
            close()
        }
    }
}
