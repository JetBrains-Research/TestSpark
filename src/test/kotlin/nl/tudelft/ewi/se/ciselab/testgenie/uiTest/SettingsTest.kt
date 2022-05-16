package nl.tudelft.ewi.se.ciselab.testgenie.uiTest

import com.automation.remarks.junit5.Video
import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.utils.waitFor
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
        find(WelcomeFrame::class.java, timeout = Duration.ofSeconds(60)).apply {
            open("untitled")
        }

        Thread.sleep(10000)

        find(IdeaFrame::class.java, timeout = Duration.ofSeconds(60)).apply {
            waitForBackgroundTasks()
            openSettings()
        }
    }

    @Order(1)
    @Test
    @Video
    fun checkTestGenieTabExists(remoteRobot: RemoteRobot) = with(remoteRobot) {
        val settingsFrame = find(SettingsFrame::class.java, timeout = Duration.ofSeconds(60))
        settingsFrame.searchTextBox.text = "TestGenie"

        with(settingsFrame.projectViewTree) {
            waitFor(Duration.ofSeconds(5)) {
                hasText("TestGenie")
            }
            assert(hasText("TestGenie"))
        }
    }

    @Order(2)
    @Test
    @Video
    fun checkTestGenieInSettings(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        val settingsFrame = find(SettingsFrame::class.java, timeout = Duration.ofSeconds(60))
        settingsFrame.findTestGenie()
        assertThat(settingsFrame.introLabel.isShowing).isTrue
        assertThat(settingsFrame.coverageCheckBox.isShowing).isTrue
    }

    @Order(3)
    @Test
    fun changeTestGenieTabValues(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        var settingsFrame = find(SettingsFrame::class.java, timeout = Duration.ofSeconds(15))
        val prevCoverageCheckBoxValue = settingsFrame.coverageCheckBox.isSelected()

        // Change checkbox value and apply the settings
        settingsFrame.coverageCheckBox.setValue(!prevCoverageCheckBoxValue)
        settingsFrame.okSettings()

        // Open settings again
        val ideaFrame = find(IdeaFrame::class.java, timeout = Duration.ofSeconds(15))
        ideaFrame.openSettings()

        // Find again TestGenie
        settingsFrame = find(SettingsFrame::class.java, timeout = Duration.ofSeconds(15))
        settingsFrame.findTestGenie()

        // Change the checkbox to previous state and close settings
        assertThat(settingsFrame.coverageCheckBox.isSelected()).isNotEqualTo(prevCoverageCheckBoxValue)
        settingsFrame.coverageCheckBox.setValue(prevCoverageCheckBoxValue)
        settingsFrame.okSettings()

        // Open settings again
        ideaFrame.openSettings()
    }

    @Order(4)
    @Test
    @Video
    fun checkEvoSuiteTabExists(remoteRobot: RemoteRobot) = with(remoteRobot) {
        val settingsFrame = find(SettingsFrame::class.java, timeout = Duration.ofSeconds(60))
        settingsFrame.searchTextBox.text = "EvoSuite"

        with(settingsFrame.projectViewTree) {
            waitFor(Duration.ofSeconds(5)) {
                hasText("EvoSuite")
            }
            assert(hasText("EvoSuite"))
        }
    }

    @Order(5)
    @Test
    @Video
    fun checkEvoSuiteInSettings(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        val settingsFrame = find(SettingsFrame::class.java, timeout = Duration.ofSeconds(60))
        settingsFrame.findEvoSuite()
        assertThat(settingsFrame.generalSettingsSeparator.isShowing).isTrue
        assertThat(settingsFrame.searchAlgorithmLabel.isShowing).isTrue
        assertThat(settingsFrame.searchAlgorithmComboBox.isShowing).isTrue
        assertThat(settingsFrame.seedLabel.isShowing).isTrue
        assertThat(settingsFrame.seedTextField.isShowing).isTrue
        assertThat(settingsFrame.configurationIdLabel.isShowing).isTrue
        assertThat(settingsFrame.configurationIdField.isShowing).isTrue
        assertThat(settingsFrame.executeTestsCheckbox.isShowing).isTrue
        assertThat(settingsFrame.createAssertionsCheckBox.isShowing).isTrue
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

    @Order(6)
    @Test
    fun changeEvoSuiteTabCoverageSectionValues(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        // Get previous values
        var settingsFrame = find(SettingsFrame::class.java, timeout = Duration.ofSeconds(15))
        val prevLineCoverageCheckBoxValue = settingsFrame.lineCoverageCheckBox.isSelected()
        val prevBranchCoverageCheckBoxValue = settingsFrame.branchCoverageCheckBox.isSelected()
        val prevExceptionCoverageCheckBoxValue = settingsFrame.exceptionCoverageCheckBox.isSelected()
        val prevMutationCoverageCheckBoxValue = settingsFrame.mutationCoverageCheckBox.isSelected()
        val prevOutputCoverageCheckBoxValue = settingsFrame.outputCoverageCheckBox.isSelected()
        val prevMethodCoverageCheckBoxValue = settingsFrame.methodCoverageCheckBox.isSelected()
        val prevMethodNoExcCoverageCheckBoxValue = settingsFrame.methodNoExcCoverageCheckBox.isSelected()
        val prevCBranchCoverageCheckBoxValue = settingsFrame.cBranchCoverageCheckBox.isSelected()

        // Change checkbox values and apply the settings
        settingsFrame.lineCoverageCheckBox.setValue(!prevLineCoverageCheckBoxValue)
        settingsFrame.branchCoverageCheckBox.setValue(!prevBranchCoverageCheckBoxValue)
        settingsFrame.exceptionCoverageCheckBox.setValue(!prevExceptionCoverageCheckBoxValue)
        settingsFrame.mutationCoverageCheckBox.setValue(!prevMutationCoverageCheckBoxValue)
        settingsFrame.outputCoverageCheckBox.setValue(!prevOutputCoverageCheckBoxValue)
        settingsFrame.methodCoverageCheckBox.setValue(!prevMethodCoverageCheckBoxValue)
        settingsFrame.methodNoExcCoverageCheckBox.setValue(!prevMethodNoExcCoverageCheckBoxValue)
        settingsFrame.cBranchCoverageCheckBox.setValue(!prevCBranchCoverageCheckBoxValue)
        settingsFrame.okSettings()

        // Open settings again
        val ideaFrame = find(IdeaFrame::class.java, timeout = Duration.ofSeconds(15))
        ideaFrame.openSettings()

        // Find again EvoSuite
        settingsFrame = find(SettingsFrame::class.java, timeout = Duration.ofSeconds(15))
        settingsFrame.findEvoSuite()

        // Verify the values changed
        assertThat(settingsFrame.lineCoverageCheckBox.isSelected()).isNotEqualTo(prevLineCoverageCheckBoxValue)
        assertThat(settingsFrame.branchCoverageCheckBox.isSelected()).isNotEqualTo(prevBranchCoverageCheckBoxValue)
        assertThat(settingsFrame.exceptionCoverageCheckBox.isSelected()).isNotEqualTo(prevExceptionCoverageCheckBoxValue)
        assertThat(settingsFrame.mutationCoverageCheckBox.isSelected()).isNotEqualTo(prevMutationCoverageCheckBoxValue)
        assertThat(settingsFrame.outputCoverageCheckBox.isSelected()).isNotEqualTo(prevOutputCoverageCheckBoxValue)
        assertThat(settingsFrame.methodCoverageCheckBox.isSelected()).isNotEqualTo(prevMethodCoverageCheckBoxValue)
        assertThat(settingsFrame.methodNoExcCoverageCheckBox.isSelected()).isNotEqualTo(prevMethodNoExcCoverageCheckBoxValue)
        assertThat(settingsFrame.cBranchCoverageCheckBox.isSelected()).isNotEqualTo(prevCBranchCoverageCheckBoxValue)

        // Change the checkbox value to previous state and close settings
        settingsFrame.lineCoverageCheckBox.setValue(prevLineCoverageCheckBoxValue)
        settingsFrame.branchCoverageCheckBox.setValue(prevBranchCoverageCheckBoxValue)
        settingsFrame.exceptionCoverageCheckBox.setValue(prevExceptionCoverageCheckBoxValue)
        settingsFrame.mutationCoverageCheckBox.setValue(prevMutationCoverageCheckBoxValue)
        settingsFrame.outputCoverageCheckBox.setValue(prevOutputCoverageCheckBoxValue)
        settingsFrame.methodCoverageCheckBox.setValue(prevMethodCoverageCheckBoxValue)
        settingsFrame.methodNoExcCoverageCheckBox.setValue(prevMethodNoExcCoverageCheckBoxValue)
        settingsFrame.cBranchCoverageCheckBox.setValue(prevCBranchCoverageCheckBoxValue)
        settingsFrame.okSettings()

        // Open settings again
        ideaFrame.openSettings()
    }

    @Order(7)
    @Test
    fun changeEvoSuiteTabGeneralSectionValues(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        // Get previous values
        var settingsFrame = find(SettingsFrame::class.java, timeout = Duration.ofSeconds(15))
        val prevExecuteTestsCheckboxValue = settingsFrame.executeTestsCheckbox.isSelected()
        val prevCreateAssertionsCheckBoxValue = settingsFrame.createAssertionsCheckBox.isSelected()
        val prevDebugModeCheckboxValue = settingsFrame.debugModeCheckbox.isSelected()
        val prevMinimiseTestSuiteCheckBoxValue = settingsFrame.minimiseTestSuiteCheckBox.isSelected()
        val prevFlakyTestCheckBoxValue = settingsFrame.flakyTestCheckBox.isSelected()
        val prevSearchAlgorithmComboBoxValue = settingsFrame.searchAlgorithmComboBox.selectedText()
        val prevSeedTextFieldValue = settingsFrame.seedTextField.text
        val prevConfigurationIdFieldValue = settingsFrame.configurationIdField.text

        // Change values and apply the settings
        settingsFrame.executeTestsCheckbox.setValue(!prevExecuteTestsCheckboxValue)
        settingsFrame.createAssertionsCheckBox.setValue(!prevCreateAssertionsCheckBoxValue)
        settingsFrame.debugModeCheckbox.setValue(!prevDebugModeCheckboxValue)
        settingsFrame.minimiseTestSuiteCheckBox.setValue(!prevMinimiseTestSuiteCheckBoxValue)
        settingsFrame.flakyTestCheckBox.setValue(!prevFlakyTestCheckBoxValue)
        settingsFrame.searchAlgorithmComboBox.selectItem("BREEDER_GA")
        settingsFrame.seedTextField.text = "3"
        settingsFrame.configurationIdField.text = "configuration id here"
        settingsFrame.okSettings()

        // Open settings again
        val ideaFrame = find(IdeaFrame::class.java, timeout = Duration.ofSeconds(15))
        ideaFrame.openSettings()

        // Find again EvoSuite
        settingsFrame = find(SettingsFrame::class.java, timeout = Duration.ofSeconds(15))
        settingsFrame.findEvoSuite()

        // Verify the values changed
        assertThat(settingsFrame.executeTestsCheckbox.isSelected()).isNotEqualTo(prevExecuteTestsCheckboxValue)
        assertThat(settingsFrame.createAssertionsCheckBox.isSelected()).isNotEqualTo(prevCreateAssertionsCheckBoxValue)
        assertThat(settingsFrame.debugModeCheckbox.isSelected()).isNotEqualTo(prevDebugModeCheckboxValue)
        assertThat(settingsFrame.minimiseTestSuiteCheckBox.isSelected()).isNotEqualTo(prevMinimiseTestSuiteCheckBoxValue)
        assertThat(settingsFrame.flakyTestCheckBox.isSelected()).isNotEqualTo(prevFlakyTestCheckBoxValue)
        assertThat(settingsFrame.searchAlgorithmComboBox.selectedText()).isEqualTo("BREEDER_GA")
        assertThat(settingsFrame.seedTextField.text).isEqualTo("3")
        assertThat(settingsFrame.configurationIdField.text).isEqualTo("configuration id here")

        // Change the values to previous state and close settings
        settingsFrame.executeTestsCheckbox.setValue(prevExecuteTestsCheckboxValue)
        settingsFrame.createAssertionsCheckBox.setValue(prevCreateAssertionsCheckBoxValue)
        settingsFrame.debugModeCheckbox.setValue(prevDebugModeCheckboxValue)
        settingsFrame.minimiseTestSuiteCheckBox.setValue(prevMinimiseTestSuiteCheckBoxValue)
        settingsFrame.flakyTestCheckBox.setValue(prevFlakyTestCheckBoxValue)
        settingsFrame.searchAlgorithmComboBox.selectItem(prevSearchAlgorithmComboBoxValue)
        settingsFrame.configurationIdField.text = prevConfigurationIdFieldValue
        settingsFrame.seedTextField.text = prevSeedTextFieldValue
        settingsFrame.okSettings()

        // Open settings again
        ideaFrame.openSettings()
    }

    @AfterAll
    fun closeAll(remoteRobot: RemoteRobot): Unit = with(remoteRobot) {
        find(SettingsFrame::class.java, timeout = Duration.ofSeconds(60)).apply {
            closeSettings()
        }

        find(IdeaFrame::class.java, timeout = Duration.ofSeconds(60)).apply {
            closeProject()
        }
    }
}
