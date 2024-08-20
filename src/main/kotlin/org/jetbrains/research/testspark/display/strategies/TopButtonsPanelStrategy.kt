package org.jetbrains.research.testspark.display.strategies

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.bundles.plugin.PluginMessagesBundle
import org.jetbrains.research.testspark.display.TestCasePanelFactory
import org.jetbrains.research.testspark.services.TestCaseDisplayBuilder
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JOptionPane

object TopButtonsPanelStrategy {
    fun toggleAllCheckboxes(selected: Boolean, project: Project) {
        project.service<TestCaseDisplayBuilder>().getTestCasePanels().forEach { (_, jPanel) ->
            val checkBox = jPanel.getComponent(0) as JCheckBox
            checkBox.isSelected = selected
        }
        project.service<TestCaseDisplayBuilder>()
            .setTestsSelected(
                if (selected) project.service<TestCaseDisplayBuilder>().getTestCasePanels().size else 0,
            )
    }

    fun updateTopLabels(
        testCasePanelFactories: ArrayList<TestCasePanelFactory>,
        testsSelectedLabel: JLabel,
        testsSelectedText: String,
        project: Project,
        testsPassedLabel: JLabel,
        testsPassedText: String,
        runAllButton: JButton,
    ) {
        var numberOfPassedTests = 0
        for (testCasePanelFactory in testCasePanelFactories) {
            if (testCasePanelFactory.isRemoved()) continue
            val error = testCasePanelFactory.getError()
            if ((error is String) && error.isEmpty()) {
                numberOfPassedTests++
            }
        }
        testsSelectedLabel.text = String.format(
            testsSelectedText,
            project.service<TestCaseDisplayBuilder>().getTestsSelected(),
            project.service<TestCaseDisplayBuilder>().getTestCasePanels().size,
        )
        testsPassedLabel.text =
            String.format(
                testsPassedText,
                numberOfPassedTests,
                project.service<TestCaseDisplayBuilder>().getTestCasePanels().size,
            )
        runAllButton.isEnabled = false
        for (testCasePanelFactory in testCasePanelFactories) {
            runAllButton.isEnabled = runAllButton.isEnabled || testCasePanelFactory.isRunEnabled()
        }
    }

    fun removeAllTestCases(project: Project) {
        // Ask the user for the confirmation
        val choice = JOptionPane.showConfirmDialog(
            null,
            PluginMessagesBundle.get("removeAllMessage"),
            PluginMessagesBundle.get("confirmationTitle"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
        )

        // Cancel the operation if the user did not press "Yes"
        if (choice == JOptionPane.NO_OPTION) return

        project.service<TestCaseDisplayBuilder>().clear()
    }
}
