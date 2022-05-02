package com.github.mitchellolsthoorn.testgenie.services;

import com.intellij.openapi.project.Project
import javax.swing.JPanel
import javax.swing.JTextArea

class TestCaseDisplayService(project: Project) {

    val panel: JPanel = JPanel()

    /**
     * Fill the panel with the generated test cases. Remove all previously shown test cases.
     *
     * @param testCases The test cases to display
     */
    fun displayTestCases(testCases: List<String>) {
        panel.removeAll()
        testCases.forEach {
            panel.add(JTextArea(it))
        }
    }
}
