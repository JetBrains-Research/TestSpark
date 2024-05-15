package org.jetbrains.research.testspark.display.generatedTestsTab

import org.jetbrains.research.testspark.bundles.plugin.PluginLabelsBundle
import org.jetbrains.research.testspark.uiUtils.IconButtonCreator
import org.jetbrains.research.testspark.uiUtils.TestSparkIcons
import java.awt.Dimension
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel

class TopButtonsPanelFactory {
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

    private var numberOfPassedTests = 0

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

        return panel
    }

    /**
     * Updates the labels.
     */
    fun update(testCasePanels: HashMap<String, JPanel>, testsSelected: Int, testCasePanelFactories: ArrayList<TestCasePanelFactory>) {
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
            testsSelected,
            testCasePanels.size,
        )
        testsPassedLabel.text =
            String.format(
                testsPassedText,
                numberOfPassedTests,
                testCasePanels.size,
            )
        runAllButton.isEnabled = false
        for (testCasePanelFactory in testCasePanelFactories) {
            runAllButton.isEnabled = runAllButton.isEnabled || testCasePanelFactory.isRunEnabled()
        }

        this.numberOfPassedTests = numberOfPassedTests
    }

    fun getRunAllButton() = runAllButton

    fun getSelectAllButton() = selectAllButton

    fun getUnselectAllButton() = unselectAllButton

    fun getRemoveAllButton() = removeAllButton

    /**
     * Creates a JButton for running all tests.
     *
     * @return a JButton for running all tests
     */
    private fun createRunAllTestButton(): JButton {
        val runTestButton = JButton(PluginLabelsBundle.get("runAll"), TestSparkIcons.runTest)
        runTestButton.isOpaque = false
        runTestButton.isContentAreaFilled = false
        runTestButton.isBorderPainted = true
        return runTestButton
    }
}
