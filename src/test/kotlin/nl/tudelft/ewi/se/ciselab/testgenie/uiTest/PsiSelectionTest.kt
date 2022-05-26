package nl.tudelft.ewi.se.ciselab.testgenie.uiTest

import com.intellij.remoterobot.RemoteRobot
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.IdeaFrame
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.WelcomeFrame
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.utils.RemoteRobotExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(RemoteRobotExtension::class)
class PsiSelectionTest {
    private lateinit var remoteRobot: RemoteRobot
    private val pathToMainFile: List<String> = listOf("pizzeria", "src", "main", "java", "PizzaClasse")

    /**
     * Opens an untitled project from the IntelliJ welcome screen.
     */
    @BeforeAll
    fun setUpAll(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        // Open an 'untitled' projectLabel
        find(WelcomeFrame::class.java, timeout = Duration.ofSeconds(15)).apply {
            open("pizzeria")
        }

        find(IdeaFrame::class.java, timeout = Duration.ofSeconds(15)).apply {
            openProjectFromProjectTree(pathToMainFile)
        }
    }

    @BeforeEach
    fun setUp(_remoteRobot: RemoteRobot): Unit = with(_remoteRobot) {
        remoteRobot = _remoteRobot
    }

    @Test
    fun suspiciousTest() {
        assertThat("Marti").isEqualTo("Marti")
    }

    @AfterAll
    fun closeAll(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        // Close the project
        find(IdeaFrame::class.java, timeout = Duration.ofSeconds(15)).apply {
            closeProjectFromProjectTree(pathToMainFile.dropLast(1).reversed())
            closeProject()
        }
    }
}
