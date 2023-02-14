package org.jetbrains.research.testgenie.uiTest

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.fixtures.ContainerFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.utils.WaitForConditionTimeoutException
import com.intellij.remoterobot.utils.keyboard
import com.intellij.remoterobot.utils.waitFor
import org.jetbrains.research.testgenie.uiTest.pages.IdeaFrame
import org.jetbrains.research.testgenie.uiTest.pages.WelcomeFrame
import org.jetbrains.research.testgenie.uiTest.utils.RemoteRobotExtension
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
    private val mainClass: String = "PizzaClasse.java"
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
     * Opens a 'pizzeria' project from the IntelliJ welcome screen.
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
            openMainFileFromProjectTree(pathToMainFile, mainClass)
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
        waitFor(Duration.ofSeconds(7)) {
            if (editor.hasText(textToClick)) {
                editor.findText(textToClick).rightClick()
                return@waitFor true
            }
            return@waitFor false
        }

        // If the action group must not be visible, assert that and quit (no need to check anything further)
        if (!actionGroupIsVisible) {
            assertThatTestGenieActionGroupIsNotVisible()
            remoteRobot.keyboard { hotKey(KeyEvent.VK_ESCAPE) }
            Thread.sleep(1000L)
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
        Thread.sleep(1000L)
    }

    private fun valueGenerator(): Stream<Arguments> = Stream.of(
        // Line 2
        Arguments.of("Classe", actionClassText.plus("PizzaClasse"), actionMethodText, actionLineText, true, true, false, false),
        // Line 3
        Arguments.of("Base", actionClassText.plus("PizzaClasse"), actionMethodText, actionLineText, true, true, false, false),
        // Line 4
        Arguments.of("main", actionClassText.plus("PizzaClasse"), actionMethodText.plus("main"), actionLineText, true, true, true, false),
        // Line 6
        Arguments.of("256", actionClassText.plus("PizzaClasse"), actionMethodText.plus("main"), actionLineText.plus("6"), true, true, true, true),
        // Line 9
        Arguments.of("BuonaPizza", actionClassText.plus("PizzaClasse"), actionMethodText.plus("main"), actionLineText.plus("9"), true, true, true, true),
        // Line 1
        Arguments.of("util", actionClassText, actionMethodText, actionLineText, false, false, false, false),
        // Line 14
        Arguments.of("italiana", actionClassText.plus("PizzaClasse"), actionMethodText.plus("pizzaMetodo"), actionLineText.plus("14"), true, true, true, true),
        // Line 15
        Arguments.of("commento", actionClassText.plus("PizzaClasse"), actionMethodText, actionLineText, true, true, false, false),
        // Line 17
        Arguments.of("anche", actionClassText.plus("PizzaClasse"), actionMethodText.plus("pizzaMetodo"), actionLineText.plus("17"), true, true, true, true),
        // Line 19
        Arguments.of("interface", "Interface PizzaServizio", actionMethodText, actionLineText, true, true, false, false),
        // Line 20
        Arguments.of("default", "Interface PizzaServizio", "Default Method saluto", actionLineText.plus("20"), true, true, true, true),
        // Line 21
        Arguments.of("quantita", "Interface PizzaServizio", actionMethodText.plus("pizzaMetodo"), actionLineText, true, true, false, false),
        // Line 23
        Arguments.of("abstract", "Abstract Class PizzaAstratta", actionMethodText, actionLineText, true, true, false, false),
        // Line 24
        Arguments.of("Saluto", "Abstract Class PizzaAstratta", actionMethodText.plus("salutoSaluto"), actionLineText.plus("24"), true, true, true, true),
        // Line 25
        Arguments.of("Ingredienti", "Abstract Class PizzaAstratta", actionMethodText, actionLineText, true, true, false, false),
        // Line 27
        Arguments.of("extends", actionClassText.plus("BuonaPizza"), actionMethodText, actionLineText, true, true, false, false),
        // Line 28
        Arguments.of("Overriden", actionClassText.plus("BuonaPizza"), actionMethodText.plus("selencaGliIngredienti"), actionLineText, true, true, true, false),
        // Line 29
        Arguments.of("Salame", actionClassText.plus("BuonaPizza"), actionMethodText.plus("selencaGliIngredienti"), actionLineText.plus(29), true, true, true, true),
        // Line 31
        Arguments.of("Appetito", actionClassText, actionMethodText, actionLineText, false, false, false, false)
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
