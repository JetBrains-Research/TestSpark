package nl.tudelft.ewi.se.ciselab.testgenie.uiTest

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.fixtures.ContainerFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.utils.WaitForConditionTimeoutException
import com.intellij.remoterobot.utils.keyboard
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.IdeaFrame
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.WelcomeFrame
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.utils.RemoteRobotExtension
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.awt.event.KeyEvent
import java.time.Duration
import java.util.stream.Stream

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(RemoteRobotExtension::class)
class PsiSelectionTest {
    private lateinit var remoteRobot: RemoteRobot
    private val pathToMainFile: List<String> = listOf("pizzeria", "src", "main", "java", "PizzaClasse")
    private val pathToMainFileWindows: List<String> = listOf("pizzeria", "src", "PizzaClasse")
    private val actionClassText: String = "Generate Tests For Class "
    private val actionMethodText: String = "Generate Tests For Method "
    private val actionLineText: String = "Generate Tests For Line "

    private lateinit var editor: ContainerFixture

    /**
     * Makes the robot click on TestGenie action group.
     */
    private fun clickOnTestGenieActionGroup() {
        with(remoteRobot) {
            find<ComponentFixture>(byXpath("//div[@accessiblename='TestGenie' and @class='ActionMenu' and @text='TestGenie']")).click()
        }
    }

    /**
     * Asserts that the action menu item is not visible.
     */
    private fun assertThatTestGenieActionGroupIsNotVisible() {
        assertThatThrownBy { clickOnTestGenieActionGroup() }.isInstanceOf(WaitForConditionTimeoutException::class.java)
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

        // Wait for background tasks
        Thread.sleep(5000L)

        // Open the main file of the project and enter full screen mode
        find(IdeaFrame::class.java, timeout = Duration.ofSeconds(15)).apply {
            openMainFileFromProjectTree(if (remoteRobot.isWin()) pathToMainFileWindows else pathToMainFile)
            // Wait for the file to load
            Thread.sleep(8000L)
            goFullScreen()
        }

        // Wait a little bit more
        Thread.sleep(3000L)

        editor = find(byXpath("//div[@class='EditorComponentImpl']"))
    }

    @BeforeEach
    fun setUp(_remoteRobot: RemoteRobot) {
        remoteRobot = _remoteRobot
    }

    @ParameterizedTest
    @MethodSource("valueGenerator")
    fun testPsiElementsVisible(
        textToClick: String,
        actionClassText: String,
        actionMethodText: String,
        actionLineText: String,
        actionGroupIsVisible: Boolean,
        classIsVisible: Boolean,
        methodIsVisible: Boolean,
        lineIsVisible: Boolean
    ) {
        // Click on the PSI element
        // println(editor.retrieveData().textDataList.forEach { println(it.text) })
        editor.findText(textToClick).rightClick()

        // If the action group must not be visible, assert that and quit (no need to check anything further)
        if (!actionGroupIsVisible) {
            assertThatTestGenieActionGroupIsNotVisible()
            return
        }
        // Click on the action group so that sub actions appear on the screen
        clickOnTestGenieActionGroup()

        // Assert that the class action is visible/invisible
        if (classIsVisible) {
            assertThatActionIsVisible(actionClassText)
        } else {
            assertThatActionIsNotVisible(actionClassText)
        }

        if (methodIsVisible) {
            assertThatActionIsVisible(actionMethodText)
        } else {
            assertThatActionIsNotVisible(actionMethodText)
        }

        if (lineIsVisible) {
            assertThatActionIsVisible(actionLineText)
        } else {
            assertThatActionIsNotVisible(actionLineText)
        }

        remoteRobot.keyboard { hotKey(KeyEvent.VK_ESCAPE) }
    }

    private fun valueGenerator(): Stream<Arguments> = Stream.of(
//        Arguments.of("PizzaClasse", actionClassText.plus("PizzaClasse"), actionMethodText, actionLineText, true, true, false, false),
//        Arguments.of("prezzoBase", actionClassText.plus("PizzaClasse"), actionMethodText, actionLineText, true, true, false, false),
//        Arguments.of("main", actionClassText.plus("PizzaClasse"), actionMethodText.plus("main"), actionLineText, true, true, true, false),
//        Arguments.of("BuonaPizza", actionClassText.plus("PizzaClasse"), actionMethodText.plus("main"), actionLineText.plus("9"), true, true, true, true),
//        Arguments.of("util", actionClassText, actionMethodText, actionLineText, false, false, false, false),
//        Arguments.of("\"Io sono una buona pizza! Mi costa \"", actionClassText.plus("PizzaClasse"), actionMethodText.plus("pizzaMetodo"), actionLineText.plus("14"), true, true, true, true),
//        Arguments.of("\"Io sono anche una buona pizza! Mi costa \"", actionClassText.plus("PizzaClasse"), actionMethodText.plus("pizzaMetodo"), actionLineText.plus("17"), true, true, true, true),
//        Arguments.of("// sono un commento", actionClassText.plus("PizzaClasse"), actionMethodText, actionLineText, true, true, false, false),
//        Arguments.of("// Overriden function", actionClassText.plus("BuonaPizza"), actionMethodText.plus("selencaGliIngredienti"), actionLineText, true, true, true, false)
        Arguments.of("Classe", actionClassText.plus("PizzaClasse"), actionMethodText, actionLineText, true, true, false, false),
        Arguments.of("Base", actionClassText.plus("PizzaClasse"), actionMethodText, actionLineText, true, true, false, false),
        Arguments.of("main", actionClassText.plus("PizzaClasse"), actionMethodText.plus("main"), actionLineText, true, true, true, false),
        Arguments.of("BuonaPizza", actionClassText.plus("PizzaClasse"), actionMethodText.plus("main"), actionLineText.plus("9"), true, true, true, true),
        Arguments.of("util", actionClassText, actionMethodText, actionLineText, false, false, false, false),
        Arguments.of("Io", actionClassText.plus("PizzaClasse"), actionMethodText.plus("pizzaMetodo"), actionLineText.plus("14"), true, true, true, true),
        Arguments.of("anche", actionClassText.plus("PizzaClasse"), actionMethodText.plus("pizzaMetodo"), actionLineText.plus("17"), true, true, true, true),
        Arguments.of("commento", actionClassText.plus("PizzaClasse"), actionMethodText, actionLineText, true, true, false, false),
        // More arguments are to follow
    )

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
