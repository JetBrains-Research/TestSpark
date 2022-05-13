package nl.tudelft.ewi.se.ciselab.testgenie.uiTest

import com.intellij.remoterobot.RemoteRobot
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.IdeaFrame
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.ToolWindowFrame
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.WelcomeFrame
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.utils.RemoteRobotExtension
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterAll
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

    /**
     * Opens an untitled project from the IntelliJ welcome screen.
     * Then opens the TestGenie sidebar on the right.
     */
    @BeforeAll
    fun setUpAll(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        // Open the 'untitled' project
        find(WelcomeFrame::class.java, timeout = Duration.ofSeconds(15)).apply {
            open("untitled")
        }

        // Open the TestGenie tool window
        find(IdeaFrame::class.java, timeout = Duration.ofSeconds(15)).apply {
            clickOnToolWindow()
        }
    }

    @Order(1)
    @Test
    fun checkTestGenieToolWindowPanel(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        // Open the tool window frame
        val toolWindowFrame = find(ToolWindowFrame::class.java, timeout = Duration.ofSeconds(15))
        // Open the "Quick Access Parameters" tab
        toolWindowFrame.openQuickAccessParametersTab()
        // toolWindowFrame.quickAccessParametersContent.click()

        // Assert text is visible
        Assertions.assertThat(toolWindowFrame.title.isVisible()).isTrue
        Assertions.assertThat(toolWindowFrame.searchBudgetSeparator.isVisible()).isTrue
        Assertions.assertThat(toolWindowFrame.searchBudgetType.isVisible()).isTrue
        Assertions.assertThat(toolWindowFrame.searchBudgetValue.isVisible()).isTrue
        Assertions.assertThat(toolWindowFrame.timeoutsSeparator.isVisible()).isTrue
        Assertions.assertThat(toolWindowFrame.initializationTimeout.isVisible()).isTrue
        Assertions.assertThat(toolWindowFrame.minimisationTimeout.isVisible()).isTrue
        Assertions.assertThat(toolWindowFrame.assertionTimeout.isVisible()).isTrue
        Assertions.assertThat(toolWindowFrame.jUnitCheckTimeout.isVisible()).isTrue
        Assertions.assertThat(toolWindowFrame.geneticAlgorithmSeparator.isVisible()).isTrue
        Assertions.assertThat(toolWindowFrame.populationLimit.isVisible()).isTrue
        Assertions.assertThat(toolWindowFrame.populationValue.isVisible()).isTrue

        // Assert buttons are visible
        Assertions.assertThat(toolWindowFrame.actionLink.isShowing).isTrue
        Assertions.assertThat(toolWindowFrame.saveButton.isShowing).isTrue
        Assertions.assertThat(toolWindowFrame.resetButton.isShowing).isTrue

        // Assert labels have the text
        Assertions.assertThat(toolWindowFrame.title.value).isEqualTo("Quick Access Parameters")
        Assertions.assertThat(toolWindowFrame.searchBudgetSeparator.value).isEqualTo("Search budget")
        Assertions.assertThat(toolWindowFrame.searchBudgetType.value).isEqualTo("Search budget type")
        Assertions.assertThat(toolWindowFrame.searchBudgetValue.value).isEqualTo("Search budget")
        Assertions.assertThat(toolWindowFrame.timeoutsSeparator.value).isEqualTo("Timeouts")
        Assertions.assertThat(toolWindowFrame.initializationTimeout.value).isEqualTo("Initialization timeout")
        Assertions.assertThat(toolWindowFrame.minimisationTimeout.value).isEqualTo("Minimisation timeout")
        Assertions.assertThat(toolWindowFrame.assertionTimeout.value).isEqualTo("Assertion timeout")
        Assertions.assertThat(toolWindowFrame.jUnitCheckTimeout.value).isEqualTo("JUnit check timeout")
        Assertions.assertThat(toolWindowFrame.geneticAlgorithmSeparator.value).isEqualTo("Genetic Algorithm")
        Assertions.assertThat(toolWindowFrame.populationLimit.value).isEqualTo("Population limit")
        Assertions.assertThat(toolWindowFrame.populationValue.value).isEqualTo("Population")
    }

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
