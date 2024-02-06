package org.jetbrains.research.testspark.display

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.bundles.TestSparkBundle
import org.jetbrains.research.testspark.bundles.TestSparkLabelsBundle
import org.jetbrains.research.testspark.services.TestCaseDisplayService
import java.awt.Dimension
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel

class TopButtonsPanelFactory(private val project: Project) {
    private var runAllButton: JButton = createRunAllTestButton()
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

    private val testCasePanelFactories = arrayListOf<TestCasePanelFactory>()

    fun getPanel(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.X_AXIS)
        panel.preferredSize = Dimension(0, 30)
        panel.add(Box.createRigidArea(Dimension(10, 0)))
        panel.add(testsPassedLabel)
        panel.add(Box.createRigidArea(Dimension(10, 0)))
        panel.add(testsSelectedLabel)
        panel.add(Box.createHorizontalGlue())
        panel.add(runAllButton)
        panel.add(selectAllButton)
        panel.add(unselectAllButton)
        panel.add(removeAllButton)

        selectAllButton.addActionListener { toggleAllCheckboxes(true) }
        unselectAllButton.addActionListener { toggleAllCheckboxes(false) }
        removeAllButton.addActionListener { removeAllTestCases() }
        runAllButton.addActionListener { runAllTestCases() }

        return panel
    }

    /**
     * Updates the labels.
     */
    fun updateTopLabels() {
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
            project.service<TestCaseDisplayService>().getTestsSelected(),
            project.service<TestCaseDisplayService>().getTestCasePanels().size,
        )
        testsPassedLabel.text =
            String.format(
                testsPassedText,
                numberOfPassedTests,
                project.service<TestCaseDisplayService>().getTestCasePanels().size,
            )
        runAllButton.isEnabled = false
        for (testCasePanelFactory in testCasePanelFactories) {
            runAllButton.isEnabled = runAllButton.isEnabled || testCasePanelFactory.isRunEnabled()
        }
    }

    /**
     * Sets the array of TestCasePanelFactory objects.
     *
     * @param testCasePanelFactories The ArrayList containing the TestCasePanelFactory objects to be set.
     */
    fun setTestCasePanelFactoriesArray(testCasePanelFactories: ArrayList<TestCasePanelFactory>) {
        this.testCasePanelFactories.addAll(testCasePanelFactories)
    }

    /**
     * Toggles check boxes so that they are either all selected or all not selected,
     *  depending on the provided parameter.
     *
     *  @param selected whether the checkboxes have to be selected or not
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

    /**
     * Executes all test cases.
     *
     * This method presents a caution message to the user and asks for confirmation before executing the test cases.
     * If the user confirms, it iterates through each test case panel factory and runs the corresponding test.
     */
    private fun runAllTestCases() {
        val choice = JOptionPane.showConfirmDialog(
            null,
            TestSparkBundle.message("runCautionMessage"),
            TestSparkBundle.message("confirmationTitle"),
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE,
        )

        if (choice == JOptionPane.CANCEL_OPTION) return

        runAllButton.isEnabled = false

        for (testCasePanelFactory in testCasePanelFactories) {
            // todo use doClick() function after removing JOptionPane
            testCasePanelFactory.runTest()
        }
    }

    /**
     * Creates a JButton for running all tests.
     *
     * @return a JButton for running all tests
     */
    private fun createRunAllTestButton(): JButton {
        val runTestButton = JButton(TestSparkLabelsBundle.defaultValue("runAll"), TestSparkIcons.runTest)
        runTestButton.isOpaque = false
        runTestButton.isContentAreaFilled = false
        runTestButton.isBorderPainted = true
        return runTestButton
    }

    fun clear() {
        testCasePanelFactories.clear()
    }
}
