package com.github.mitchellolsthoorn.testgenie.services;

import com.intellij.openapi.project.Project
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.*

class TestCaseDisplayService(project: Project) {

    val panel: JPanel = JPanel()

    init {
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
    }

    /**
     * Fill the panel with the generated test cases. Remove all previously shown test cases.
     *
     * @param testCases The test cases to display
     */
    fun displayTestCases(testCases: List<String>) {
        panel.removeAll()
        testCases.forEach {
            val testCasePanel = JPanel()
            testCasePanel.layout = BorderLayout()

            testCasePanel.add(JCheckBox(), BorderLayout.WEST)

            val editor = JTextArea(it)
            editor.isEditable = false

            testCasePanel.add(editor, BorderLayout.CENTER)

            testCasePanel.maximumSize= Dimension(Short.MAX_VALUE.toInt(), testCasePanel.preferredSize.height)
            panel.add(testCasePanel)
            panel.add(Box.createRigidArea(Dimension(0, 5)))
        }
    }
}
