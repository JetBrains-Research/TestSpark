package org.jetbrains.research.testspark.display.generatedTests

import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import java.awt.Dimension
import java.util.*
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import kotlin.collections.ArrayList
import org.jetbrains.research.testspark.bundles.plugin.PluginLabelsBundle
import org.jetbrains.research.testspark.bundles.plugin.PluginMessagesBundle
import org.jetbrains.research.testspark.core.progress.CustomProgressIndicator
import org.jetbrains.research.testspark.display.TestSparkIcons
import org.jetbrains.research.testspark.display.custom.IJProgressIndicator
import org.jetbrains.research.testspark.display.utils.IconButtonCreator
import org.jetbrains.research.testspark.services.TestCaseDisplayBuilder

class TopButtonsPanelFactory(private val project: Project) {
    private val testCasePanelFactories = arrayListOf<TestCasePanelFactory>()

    private var runAllButton: JButton = createRunAllTestButton()
    private var selectAllButton: JButton =
        IconButtonCreator.getButton(TestSparkIcons.selectAll, PluginLabelsBundle.get("selectAllTip"))
    private var unselectAllButton: JButton =
        IconButtonCreator.getButton(TestSparkIcons.unselectAll, PluginLabelsBundle.get("unselectAllTip"))
    private var removeAllButton: JButton =
        IconButtonCreator.getButton(TestSparkIcons.removeAll, PluginLabelsBundle.get("removeAllTip"))

    private var testsSelectedText: String = "${PluginLabelsBundle.get("testsSelected")}: %d/%d"
    private var testsSelectedLabel: JLabel = JLabel(testsSelectedText)

    private val testsPassedText: String = "${PluginLabelsBundle.get("testsPassed")}: %d/%d"
    private var testsPassedLabel: JLabel = JLabel(testsPassedText)

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

    /**
     * Toggles check boxes so that they are either all selected or all not selected,
     *  depending on the provided parameter.
     *
     *  @param selected whether the checkboxes have to be selected or not
     */
    private fun toggleAllCheckboxes(selected: Boolean) {
        project.service<TestCaseDisplayBuilder>().getTestCasePanels().forEach { (_, jPanel) ->
            val checkBox = jPanel.getComponent(0) as JCheckBox
            checkBox.isSelected = selected
        }
        project.service<TestCaseDisplayBuilder>()
            .setTestsSelected(
                if (selected) project.service<TestCaseDisplayBuilder>().getTestCasePanels().size else 0,
            )
    }

    /**
     * Removes all test cases from the cache and tool window UI.
     */
    private fun removeAllTestCases() {
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

    /**
     * Executes all test cases.
     *
     * This method presents a caution message to the user and asks for confirmation before executing the test cases.
     * If the user confirms, it iterates through each test case panel factory and runs the corresponding test.
     */
    private fun runAllTestCases() {
        val choice = JOptionPane.showConfirmDialog(
            null,
            PluginMessagesBundle.get("runCautionMessage"),
            PluginMessagesBundle.get("confirmationTitle"),
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE,
        )

        if (choice == JOptionPane.CANCEL_OPTION) return

        runAllButton.isEnabled = false

        // add each test generation task to queue
        val tasks: Queue<(CustomProgressIndicator) -> Unit> = LinkedList()

        for (testCasePanelFactory in testCasePanelFactories) {
            testCasePanelFactory.addTask(tasks)
        }
        // run tasks one after each other
        executeTasks(tasks)
    }

    private fun executeTasks(tasks: Queue<(CustomProgressIndicator) -> Unit>) {
        val nextTask = tasks.poll()

        nextTask?.let { task ->
            ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Test execution") {
                override fun run(indicator: ProgressIndicator) {
                    task(IJProgressIndicator(indicator))
                }

                override fun onFinished() {
                    super.onFinished()
                    executeTasks(tasks)
                }
            })
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

    fun clear() {
        testCasePanelFactories.clear()
    }

    private fun createRunAllTestButton(): JButton {
        val runTestButton = JButton(PluginLabelsBundle.get("runAll"), TestSparkIcons.runTest)
        runTestButton.isOpaque = false
        runTestButton.isContentAreaFilled = false
        runTestButton.isBorderPainted = true
        return runTestButton
    }
}
