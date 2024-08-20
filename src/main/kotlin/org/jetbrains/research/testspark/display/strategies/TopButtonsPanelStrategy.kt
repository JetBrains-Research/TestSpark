package org.jetbrains.research.testspark.display.strategies

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.bundles.plugin.PluginMessagesBundle
import org.jetbrains.research.testspark.display.TestCasePanelFactory
import org.jetbrains.research.testspark.services.java.JavaTestCaseDisplayBuilder
import org.jetbrains.research.testspark.services.kotlin.KotlinTestCaseDisplayBuilder
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JOptionPane

object TopButtonsPanelStrategy {
    fun toggleAllJavaCheckboxes(selected: Boolean, project: Project) {
        project.service<JavaTestCaseDisplayBuilder>().getTestCasePanels().forEach { (_, jPanel) ->
            val checkBox = jPanel.getComponent(0) as JCheckBox
            checkBox.isSelected = selected
        }
        project.service<JavaTestCaseDisplayBuilder>()
            .setTestsSelected(
                if (selected) project.service<JavaTestCaseDisplayBuilder>().getTestCasePanels().size else 0,
            )
    }

    fun toggleAllKotlinCheckboxes(selected: Boolean, project: Project) {
        project.service<KotlinTestCaseDisplayBuilder>().getTestCasePanels().forEach { (_, jPanel) ->
            val checkBox = jPanel.getComponent(0) as JCheckBox
            checkBox.isSelected = selected
        }
        project.service<KotlinTestCaseDisplayBuilder>()
            .setTestsSelected(
                if (selected) project.service<KotlinTestCaseDisplayBuilder>().getTestCasePanels().size else 0,
            )
    }

    fun updateTopJavaLabels(
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
            project.service<JavaTestCaseDisplayBuilder>().getTestsSelected(),
            project.service<JavaTestCaseDisplayBuilder>().getTestCasePanels().size,
        )
        testsPassedLabel.text =
            String.format(
                testsPassedText,
                numberOfPassedTests,
                project.service<JavaTestCaseDisplayBuilder>().getTestCasePanels().size,
            )
        runAllButton.isEnabled = false
        for (testCasePanelFactory in testCasePanelFactories) {
            runAllButton.isEnabled = runAllButton.isEnabled || testCasePanelFactory.isRunEnabled()
        }
    }

    fun updateTopKotlinLabels(
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
            project.service<KotlinTestCaseDisplayBuilder>().getTestsSelected(),
            project.service<KotlinTestCaseDisplayBuilder>().getTestCasePanels().size,
        )
        testsPassedLabel.text =
            String.format(
                testsPassedText,
                numberOfPassedTests,
                project.service<KotlinTestCaseDisplayBuilder>().getTestCasePanels().size,
            )
        runAllButton.isEnabled = false
        for (testCasePanelFactory in testCasePanelFactories) {
            runAllButton.isEnabled = runAllButton.isEnabled || testCasePanelFactory.isRunEnabled()
        }
    }

    fun removeAllJavaTestCases(project: Project) {
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

        project.service<JavaTestCaseDisplayBuilder>().clear()
    }

    fun removeAllKotlinTestCases(project: Project) {
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

        project.service<KotlinTestCaseDisplayBuilder>().clear()
    }
}