package org.jetbrains.research.testspark.display.generatedTests

import javax.swing.JPanel

object GenerateTestsTabHelper {
    /**
     * A helper method to remove a test case from the cache and from the UI.
     *
     * @param testCaseName the name of the test
     */
    fun removeTestCase(testCaseName: String, generatedTestsTabData: GeneratedTestsTabData) {
        // Update the number of selected test cases if necessary
        if (generatedTestsTabData.testCaseNameToSelectedCheckbox[testCaseName]!!.isSelected) {
            generatedTestsTabData.testsSelected--
        }

        // Remove the test panel from the UI
        generatedTestsTabData.allTestCasePanel.remove(generatedTestsTabData.testCaseNameToPanel[testCaseName])

        // Remove the test panel
        generatedTestsTabData.testCaseNameToPanel.remove(testCaseName)

        // Remove the selected checkbox
        generatedTestsTabData.testCaseNameToSelectedCheckbox.remove(testCaseName)

        // Remove the editorTextField
        generatedTestsTabData.testCaseNameToEditorTextField.remove(testCaseName)

        // Remove the Panel for
        generatedTestsTabData.testCaseNameToUndoRemovePanel.remove(testCaseName)
    }

    /**
     * A helper method to remove a test case from the UI.
     *
     * @param testCaseName the name of the test
     */
    fun removeUITestCase(testCaseName: String, generatedTestsTabData: GeneratedTestsTabData): Int {
        // Update the number of selected test cases if necessary
        generatedTestsTabData.testCaseNameToSelectedCheckbox[testCaseName]!!.setSelected(false)

        val index: Int =
            generatedTestsTabData.allTestCasePanel.getComponentZOrder(generatedTestsTabData.testCaseNameToPanel[testCaseName])

        // Remove the test panel from the UI
        generatedTestsTabData.allTestCasePanel.remove(generatedTestsTabData.testCaseNameToPanel[testCaseName])

        return index
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
        generatedTestsTabData.topButtonsPanelBuilder.update(generatedTestsTabData)
    }
}
