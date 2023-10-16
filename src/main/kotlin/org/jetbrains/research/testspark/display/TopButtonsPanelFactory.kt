package org.jetbrains.research.testspark.display

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.TestSparkBundle
import org.jetbrains.research.testspark.TestSparkLabelsBundle
import org.jetbrains.research.testspark.services.TestCaseDisplayService
import org.jetbrains.research.testspark.services.TestsExecutionResultService
import java.awt.Dimension
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel

class TopButtonsPanelFactory(private val project: Project) {
    private var selectAllButton: JButton =
        createButton(TestSparkIcons.selectAll, TestSparkLabelsBundle.defaultValue("selectAllTip"))
    private var unselectAllButton: JButton =
        createButton(TestSparkIcons.unselectAll, TestSparkLabelsBundle.defaultValue("unselectAllTip"))
    private var removeAllButton: JButton =
        createButton(TestSparkIcons.removeAll, TestSparkLabelsBundle.defaultValue("removeAllTip"))

    private var testsSelectedText: String = "${TestSparkLabelsBundle.defaultValue("testsSelected")}: %d/%d"
    private var testsSelectedLabel: JLabel = JLabel(testsSelectedText)

    private val testsPassedText: String = "${TestSparkLabelsBundle.defaultValue("testsPassed")}: %d/%d"
    private var testsPassedLabel: JLabel = JLabel(testsPassedText)

    fun getPanel(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.X_AXIS)
        panel.preferredSize = Dimension(0, 30)
        panel.add(Box.createRigidArea(Dimension(10, 0)))
        panel.add(testsPassedLabel)
        panel.add(Box.createRigidArea(Dimension(10, 0)))
        panel.add(testsSelectedLabel)
        panel.add(Box.createHorizontalGlue())
        panel.add(selectAllButton)
        panel.add(unselectAllButton)
        panel.add(removeAllButton)

        selectAllButton.addActionListener { toggleAllCheckboxes(true) }
        unselectAllButton.addActionListener { toggleAllCheckboxes(false) }
        removeAllButton.addActionListener { removeAllTestCases() }

        return panel
    }

    /**
     * Updates the labels.
     */
    fun updateTopLabels() {
        testsSelectedLabel.text = String.format(
            testsSelectedText,
            project.service<TestCaseDisplayService>().getTestsSelected(),
            project.service<TestCaseDisplayService>().getTestCasePanels().size,
        )
        testsPassedLabel.text =
            String.format(
                testsPassedText,
                project.service<TestCaseDisplayService>()
                    .getTestCasePanels().size - project.service<TestsExecutionResultService>().size(),
                project.service<TestCaseDisplayService>().getTestCasePanels().size,
            )
    }

    /**
     * Toggles check boxes so that they are either all selected or all not selected,
     *  depending on the provided parameter.
     *
     *  @param selected whether the check boxes have to be selected or not
     */
    private fun toggleAllCheckboxes(selected: Boolean) {
        project.service<TestCaseDisplayService>().getTestCasePanels().forEach { (_, jPanel) ->
            val checkBox = jPanel.getComponent(0) as JCheckBox
            checkBox.isSelected = selected
        }
        project.service<TestCaseDisplayService>()
            .setTestsSelected(if (selected) project.service<TestCaseDisplayService>().getTestCasePanels().size else 0)
    }

    /**
     * Removes all test cases from the cache and tool window UI.
     */
    private fun removeAllTestCases() {
        // Ask the user for the confirmation
        val choice = JOptionPane.showConfirmDialog(
            null,
            TestSparkBundle.message("removeAllMessage"),
            TestSparkBundle.message("confirmationTitle"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
        )

        // Cancel the operation if the user did not press "Yes"
        if (choice == JOptionPane.NO_OPTION) return

        project.service<TestCaseDisplayService>().clear()
    }
}