package org.jetbrains.research.testspark.display.generatedTests

object GenerateTestsTabHelper {
    /**
     * A helper method to remove a test case from the cache and from the UI.
     *
     * @param testCaseName the name of the test
     */
    fun removeTestCase(testCaseName: String, generatedTestsTabData: GeneratedTestsTabData) {
        // Uncheck the selected checkbox
        generatedTestsTabData.testCaseNameToSelectedCheckbox[testCaseName]!!.isSelected = false

        // Disable the selected checkbox
        generatedTestsTabData.testCaseNameToSelectedCheckbox[testCaseName]!!.isEnabled = false

        // Update status
        generatedTestsTabData.testCaseNameToEnabled[testCaseName] = false
    }

    fun undoRemoveTestCase(testCaseName: String, generatedTestsTabData: GeneratedTestsTabData) {
        // Check the selected checkbox
        generatedTestsTabData.testCaseNameToSelectedCheckbox[testCaseName]!!.isSelected = true

        // Enable the selected checkbox
        generatedTestsTabData.testCaseNameToSelectedCheckbox[testCaseName]!!.isEnabled = true

        // Update status
        generatedTestsTabData.testCaseNameToEnabled[testCaseName] = true
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
