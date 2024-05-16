package org.jetbrains.research.testspark.uiUtils

import com.intellij.ui.EditorTextField
import com.intellij.ui.components.JBScrollPane
import org.jetbrains.research.testspark.display.generatedTestsTab.GeneratedTestsTabData
import javax.swing.JCheckBox
import javax.swing.JPanel

object GenerateTestsTabHelper {
    /**
     * Retrieve the editor corresponding to a particular test case
     *
     * @param testCaseName the name of the test case
     * @return the editor corresponding to the test case, or null if it does not exist
     */
    fun getEditorTextField(testCaseName: String, generatedTestsTabData: GeneratedTestsTabData): EditorTextField? {
        val middlePanelComponent = generatedTestsTabData.testCaseNameToPanels[testCaseName]?.getComponent(2) ?: return null
        val middlePanel = middlePanelComponent as JPanel
        return (middlePanel.getComponent(1) as JBScrollPane).viewport.view as EditorTextField
    }

    /**
     * A helper method to remove a test case from the cache and from the UI.
     *
     * @param testCaseName the name of the test
     */
    fun removeTestCase(testCaseName: String, generatedTestsTabData: GeneratedTestsTabData) {
        // Update the number of selected test cases if necessary
        if ((generatedTestsTabData.testCaseNameToPanels[testCaseName]!!.getComponent(0) as JCheckBox).isSelected) {
            generatedTestsTabData.testsSelected--
        }

        // Remove the test panel from the UI
        generatedTestsTabData.allTestCasePanel.remove(generatedTestsTabData.testCaseNameToPanels[testCaseName])

        // Remove the test panel
        generatedTestsTabData.testCaseNameToPanels.remove(testCaseName)
    }

    /**
     * Updates the user interface of the tool window.
     *
     * This method updates the UI of the tool window tab by calling the updateUI
     * method of the allTestCasePanel object and the updateTopLabels method
     * of the topButtonsPanel object. It also checks if there are no more tests remaining
     * and closes the tool window if that is the case.
     */
    fun update(generatedTestsTabData: GeneratedTestsTabData) {
        generatedTestsTabData.allTestCasePanel.updateUI()
        generatedTestsTabData.topButtonsPanelFactory.update(generatedTestsTabData.testsSelected, generatedTestsTabData.testCasePanelFactories)
    }
}
