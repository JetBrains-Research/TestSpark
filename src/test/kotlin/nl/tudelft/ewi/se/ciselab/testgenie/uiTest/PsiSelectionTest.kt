package nl.tudelft.ewi.se.ciselab.testgenie.uiTest

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.fixtures.ContainerFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.utils.WaitForConditionTimeoutException
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.IdeaFrame
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.WelcomeFrame
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.utils.RemoteRobotExtension
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
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
    private val actionClassText: String = "Generate Tests For Class "
    private val actionMethodText: String = "Generate Tests For Method "
    private val actionLineText: String = "Generate Tests For Line "

    /**
     * Makes the robot click on TestGenie action group.
     */
    private fun clickOnTestGenieActionGroup() {
        with(remoteRobot) {
            find<ComponentFixture>(byXpath("//div[@accessiblename='TestGenie' and @class='ActionMenu' and @text='TestGenie']")).click()
        }
    }

    /**
     * Asserts that the action menu item is visible.
     *
     * @param text the display text of the action
     */
    private fun assertThatActionIsVisible(text: String) {
        with(remoteRobot) {
            assertThat(
                find<ComponentFixture>(
                    byXpath("//div[@text='TestGenie']//div[contains(@text, '$text')]"),
                    Duration.ofSeconds(3)
                ).isShowing
            ).isTrue
        }
    }

    /**
     * Asserts that the action menu item is not visible.
     *
     * @param text the display text of the action
     */
    private fun assertThatActionIsNotVisible(text: String) {
        assertThatThrownBy { assertThatActionIsVisible(text) }.isInstanceOf(WaitForConditionTimeoutException::class.java)
    }

    /**
     * Opens an untitled project from the IntelliJ welcome screen.
     */
    @BeforeAll
    fun setUpAll(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        // Open 'pizzeria' project
        find(WelcomeFrame::class.java, timeout = Duration.ofSeconds(15)).apply {
            open("pizzeria")
        }

        Thread.sleep(3000L)

        // Open the main file of the project and enter full screen mode
        find(IdeaFrame::class.java, timeout = Duration.ofSeconds(15)).apply {
            openMainFileFromProjectTree(pathToMainFile)
            goFullScreen()
        }
    }

    @BeforeEach
    fun setUp(_remoteRobot: RemoteRobot) {
        remoteRobot = _remoteRobot
    }

    @Test
    fun testPossibleToGenerateTestsForClassPizzaClasse(): Unit = with(remoteRobot) {
        val editor = find<ContainerFixture>(byXpath("//div[@class='EditorComponentImpl']"))
        println(editor.findAllText().map { it.text })
        editor.findText("Classe").rightClick()

        clickOnTestGenieActionGroup()
        assertThatActionIsVisible(actionClassText.plus("PizzaClasse"))
        assertThatActionIsNotVisible(actionMethodText)
        assertThatActionIsNotVisible(actionLineText)
    }

    @AfterAll
    fun closeAll(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        // Exit full screen mode and close the project
        find(IdeaFrame::class.java, timeout = Duration.ofSeconds(15)).apply {
            closeMainFileFromProjectTree(pathToMainFile.dropLast(1).reversed())
            quitFullScreen()
            closeProject()
        }
    }
}
