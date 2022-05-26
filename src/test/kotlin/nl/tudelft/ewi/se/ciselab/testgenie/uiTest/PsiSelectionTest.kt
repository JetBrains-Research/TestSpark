package nl.tudelft.ewi.se.ciselab.testgenie.uiTest

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.fixtures.ContainerFixture
import com.intellij.remoterobot.search.locators.byXpath
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

    private fun assertGenerateTestsActionVisible(title: String) {
        with(remoteRobot) {
            find<ComponentFixture>(byXpath("//div[@accessiblename='TestGenie' and @class='ActionMenu' and @text='TestGenie']")).click()
            assertThat(find<ComponentFixture>(byXpath("//div[@text='TestGenie']//div[contains(@text, '$title')]"), Duration.ofSeconds(5)).isShowing).isTrue
        }
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
        assertGenerateTestsActionVisible("Class PizzaClasse")
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
