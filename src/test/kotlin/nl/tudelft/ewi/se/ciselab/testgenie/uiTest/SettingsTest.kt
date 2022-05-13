package nl.tudelft.ewi.se.ciselab.testgenie.uiTest

import com.intellij.remoterobot.RemoteRobot
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.IdeaFrame
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.SettingsFrame
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages.WelcomeFrame
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.utils.RemoteRobotExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
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
    fun checkTestGenieInSettings(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        val settingsFrame = find(SettingsFrame::class.java, timeout = Duration.ofSeconds(15))
        settingsFrame.findTestGenie()
        assertThat(settingsFrame.introLabel.isShowing).isTrue
        assertThat(settingsFrame.coverageCheckBox.isShowing).isTrue
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
    fun checkEvoSuiteInSettings(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        val settingsFrame = find(SettingsFrame::class.java, timeout = Duration.ofSeconds(15))
        settingsFrame.findEvoSuite()
        assertThat(settingsFrame.generalSettingsSeparator.isShowing).isTrue
        assertThat(settingsFrame.searchAlgorithmLabel.isShowing).isTrue
        assertThat(settingsFrame.searchAlgorithmComboBox.isShowing).isTrue
        assertThat(settingsFrame.seedLabel.isShowing).isTrue
        assertThat(settingsFrame.configurationIdLabel.isShowing).isTrue
        assertThat(settingsFrame.executeTestsCheckbox.isShowing).isTrue
        assertThat(settingsFrame.debugModeCheckbox.isShowing).isTrue
        assertThat(settingsFrame.minimiseTestSuiteCheckBox.isShowing).isTrue
        assertThat(settingsFrame.flakyTestCheckBox.isShowing).isTrue

        assertThat(settingsFrame.coverageSeparator.isShowing).isTrue
        assertThat(settingsFrame.lineCoverageCheckBox.isShowing).isTrue
        assertThat(settingsFrame.branchCoverageCheckBox.isShowing).isTrue
        assertThat(settingsFrame.exceptionCoverageCheckBox.isShowing).isTrue
        assertThat(settingsFrame.mutationCoverageCheckBox.isShowing).isTrue
        assertThat(settingsFrame.outputCoverageCheckBox.isShowing).isTrue
        assertThat(settingsFrame.methodCoverageCheckBox.isShowing).isTrue
        assertThat(settingsFrame.methodNoExcCoverageCheckBox.isShowing).isTrue
        assertThat(settingsFrame.cBranchCoverageCheckBox.isShowing).isTrue
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
