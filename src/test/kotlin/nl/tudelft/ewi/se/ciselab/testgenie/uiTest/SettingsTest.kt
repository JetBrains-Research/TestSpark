package nl.tudelft.ewi.se.ciselab.testgenie.uiTest

import com.intellij.remoterobot.RemoteRobot
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.IdeaFrame
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.SettingsFrame
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.WelcomeFrame
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.utils.RemoteRobotExtension
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration

@TestMethodOrder(OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(RemoteRobotExtension::class)
class SettingsTest {

    @BeforeAll
    fun setUpAll(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        find(WelcomeFrame::class.java, timeout = Duration.ofSeconds(15)).apply {
            open("untitled")
        }

        Thread.sleep(10000)

        find(IdeaFrame::class.java, timeout = Duration.ofSeconds(15)).apply {
            waitForBackgroundTasks()
            openSettings()
        }
    }

    @Order(1)
    @Test
    fun checkTestGenieTabExists(remoteRobot: RemoteRobot) = with(remoteRobot) {
        val settingsFrame = find(SettingsFrame::class.java, timeout = Duration.ofSeconds(15))
        settingsFrame.searchTextBox.text = "TestGenie"

        Thread.sleep(2000)

        with(settingsFrame.projectViewTree) {
            assert(hasText("TestGenie"))
        }
    }

    @Order(2)
    @Test
    fun checkTestGenieInSettings(remoteRobot: RemoteRobot) = with(remoteRobot) {
        val settingsFrame = find(SettingsFrame::class.java, timeout = Duration.ofSeconds(15))
        settingsFrame.findTestGenie()
        assertTrue(settingsFrame.introLabel.isShowing)
        assertTrue(settingsFrame.coverageCheckBox.isShowing)
    }

    @Order(3)
    @Test
    fun checkEvoSuiteTabExists(remoteRobot: RemoteRobot) = with(remoteRobot) {
        val settingsFrame = find(SettingsFrame::class.java, timeout = Duration.ofSeconds(15))
        settingsFrame.searchTextBox.text = "EvoSuite"

        Thread.sleep(2000)

        with(settingsFrame.projectViewTree) {
            assert(hasText("EvoSuite"))
        }
    }

    @Order(4)
    @Test
    fun checkEvoSuiteInSettings(remoteRobot: RemoteRobot) = with(remoteRobot) {
        val settingsFrame = find(SettingsFrame::class.java, timeout = Duration.ofSeconds(15))
        settingsFrame.findEvoSuite()
        assertTrue(settingsFrame.generalSettingsSeparator.isShowing)
        assertTrue(settingsFrame.searchAlgorithmLabel.isShowing)
        assertTrue(settingsFrame.searchAlgorithmComboBox.isShowing)
        assertTrue(settingsFrame.seedLabel.isShowing)
        assertTrue(settingsFrame.configurationIdLabel.isShowing)
        assertTrue(settingsFrame.executeTestsCheckbox.isShowing)
        assertTrue(settingsFrame.debugModeCheckbox.isShowing)
        assertTrue(settingsFrame.minimiseTestSuiteCheckBox.isShowing)
        assertTrue(settingsFrame.flakyTestCheckBox.isShowing)

        assertTrue(settingsFrame.coverageSeparator.isShowing)
        assertTrue(settingsFrame.lineCoverageCheckBox.isShowing)
        assertTrue(settingsFrame.branchCoverageCheckBox.isShowing)
        assertTrue(settingsFrame.exceptionCoverageCheckBox.isShowing)
        assertTrue(settingsFrame.mutationCoverageCheckBox.isShowing)
        assertTrue(settingsFrame.outputCoverageCheckBox.isShowing)
        assertTrue(settingsFrame.methodCoverageCheckBox.isShowing)
        assertTrue(settingsFrame.methodNoExcCoverageCheckBox.isShowing)
        assertTrue(settingsFrame.cBranchCoverageCheckBox.isShowing)
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
