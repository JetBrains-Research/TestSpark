package nl.tudelft.ewi.se.ciselab.testgenie.uiTest

import com.intellij.remoterobot.RemoteRobot
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.IdeaFrame
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.ToolWindowFrame
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.WelcomeFrame
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.utils.RemoteRobotExtension
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
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
    // Open the tool window frame
    private lateinit var toolWindowFrame: ToolWindowFrame

    /**
     * Opens an untitled project from the IntelliJ welcome screen.
     * Then opens the TestGenie sidebar on the right.
     */
    @BeforeAll
    fun setUpAll(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        // Open an 'untitled' projectLabel
        find(WelcomeFrame::class.java, timeout = Duration.ofSeconds(15)).apply {
            open("untitled")
        }

        // Open the TestGenie tool window
        find(IdeaFrame::class.java, timeout = Duration.ofSeconds(15)).apply {
            clickOnToolWindow()
        }
    }

    @BeforeEach
    fun setUp(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        // Open the tool window frame
        toolWindowFrame = find(ToolWindowFrame::class.java, timeout = Duration.ofSeconds(15))
        // Open the "Quick Access Parameters" tab
        toolWindowFrame.openQuickAccessParametersTab()
    }

    @Order(1)
    @Test
    fun testEverythingIsVisible(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        // Assert text is visible
        toolWindowFrame.getTitles().forEach { Assertions.assertThat(it.isVisible()) }
        toolWindowFrame.getUIElementLabels().forEach { Assertions.assertThat(it.isVisible()) }

        // Assert buttons are visible
        Assertions.assertThat(toolWindowFrame.advancedSettingsButton.isShowing).isTrue
        toolWindowFrame.getButtons().forEach { Assertions.assertThat(it.isShowing) }
    }

    @Order(2)
    @Test
    fun testLabelsAreCorrect(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        val labelTexts = listOf(
            "Search budget type", "Search budget", "Initialization timeout",
            "Minimisation timeout", "Assertion timeout", "JUnit check timeout",
            "Population limit", "Population"
        )
        // Assert labels have the text
        toolWindowFrame.getUIElementLabels().zip(labelTexts).forEach { Assertions.assertThat(it.first.value).isEqualTo(it.second) }
    }

    @Order(3)
    @Test
    fun testTitlesAreCorrect(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        val titleTexts = listOf("Quick Access Parameters", "Search budget", "Timeouts", "Genetic Algorithm")
        // Assert labels have the text
        toolWindowFrame.getTitles().zip(titleTexts).forEach { Assertions.assertThat(it.first.value).isEqualTo(it.second) }
    }

    @Order(4)
    @Test
    fun testToolTipsOnUIElementLabels(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        // TODO: add other default tooltips
        val tooltipTexts = listOf(
            "Default: Max time", "Default: 120 seconds", "Default: Individuals"
        )
        toolWindowFrame.getDefaultTooltips().zip(tooltipTexts).forEach { Assertions.assertThat(it.first.value).isEqualTo(it.second) }
    }

    // TODO: spinners + modifications

    // TODO: combo-boxes + modifications

    // TODO: advanced settings button

    // TODO: save button

    // TODO: reset button

    // TODO (Optional): tooltips on hovering

    /**
     * First closes the TestGenie sidebar by clicking on the stripe button again.
     * Secondly, closes the project itself and returns the sandbox to IntelliJ welcome state.
     */
    @AfterAll
    fun closeAll(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {

        find(IdeaFrame::class.java, timeout = Duration.ofSeconds(15)).apply {
            clickOnToolWindow()
        }

        find(IdeaFrame::class.java, timeout = Duration.ofSeconds(15)).apply {
            closeProject()
        }
    }
}
