package org.jetbrains.research.testspark.display.generatedTests

object GenerateTestsTabHelper {
    /**
     * A helper method to remove a test case from the cache and from the UI.
     *
     * @param testCaseId the id of the test
     */
    fun removeTestCase(
        testCaseId: Int,
        generatedTestsTabData: GeneratedTestsTabData,
    ) {
        // Update the number of selected test cases if necessary
        val testCaseIsSelected = generatedTestsTabData.testCaseIdToSelectedCheckbox[testCaseId]!!.isSelected
        val testCaseIsNotHidden = !generatedTestsTabData.hiddenTestCases.contains(testCaseId)
        generatedTestsTabData.hiddenTestCases.remove(testCaseId)
        if (testCaseIsSelected && testCaseIsNotHidden) {
            generatedTestsTabData.testsSelected--
        }

        // Remove the test panel from the UI
        generatedTestsTabData.allTestCasePanel.remove(generatedTestsTabData.testCaseIdToPanel[testCaseId])

        // Remove the test panel
        generatedTestsTabData.testCaseIdToPanel.remove(testCaseId)

        // Remove the selected checkbox
        generatedTestsTabData.testCaseIdToSelectedCheckbox.remove(testCaseId)

        // Remove the editorTextField
        generatedTestsTabData.testCaseIdToEditorTextField.remove(testCaseId)
    }

    fun showTestCase(
        testCaseId: Int,
        generatedTestsTabData: GeneratedTestsTabData,
    ) {
        generatedTestsTabData.hiddenTestCases.remove(testCaseId)
        if (generatedTestsTabData.testCaseIdToSelectedCheckbox[testCaseId]!!.isSelected) {
            generatedTestsTabData.testsSelected++
        }

        update(generatedTestsTabData)
    }

    fun hideTestCase(testCaseId: Int, generatedTestsTabData: GeneratedTestsTabData) {
        generatedTestsTabData.hiddenTestCases.add(testCaseId)
        if (generatedTestsTabData.testCaseIdToSelectedCheckbox[testCaseId]!!.isSelected) {
            generatedTestsTabData.testsSelected--
        }

        update(generatedTestsTabData)
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
