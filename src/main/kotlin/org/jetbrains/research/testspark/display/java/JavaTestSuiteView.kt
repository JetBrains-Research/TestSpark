package org.jetbrains.research.testspark.display

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.bundles.plugin.PluginLabelsBundle
import org.jetbrains.research.testspark.bundles.plugin.PluginMessagesBundle
import org.jetbrains.research.testspark.core.progress.CustomProgressIndicator
import org.jetbrains.research.testspark.display.custom.IJProgressIndicator
import org.jetbrains.research.testspark.display.strategies.TopButtonsPanelStrategy
import java.awt.Dimension
import java.util.LinkedList
import java.util.Queue
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel

class JavaTestSuiteView(private val project: Project) : TestSuiteView {
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

    override fun updateTopLabels() {
        TopButtonsPanelStrategy.updateTopJavaLabels(
            testCasePanelFactories,
            testsSelectedLabel,
            testsSelectedText,
            project,
            testsPassedLabel,
            testsPassedText,
            runAllButton,
        )
    }

    override fun toggleAllCheckboxes(selected: Boolean) {
        TopButtonsPanelStrategy.toggleAllJavaCheckboxes(selected, project)
    }

    override fun removeAllTestCases() {
        TopButtonsPanelStrategy.removeAllJavaTestCases(project)
    }

    override fun runAllTestCases() {
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

    override fun setTestCasePanelFactoriesArray(testCasePanelFactories: ArrayList<TestCasePanelFactory>) {
        this.testCasePanelFactories.addAll(testCasePanelFactories)
    }

    override fun getPanel(): JPanel {
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

    override fun clear() {
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