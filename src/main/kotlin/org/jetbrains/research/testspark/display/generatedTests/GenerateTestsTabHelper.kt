package org.jetbrains.research.testspark.display.generatedTests

object GenerateTestsTabHelper {
    /**
     * A helper method to remove a test case from the cache and from the UI.
     *
     * @param testCaseName the name of the test
     */
    fun removeTestCase(testCaseName: String, generatedTestsTabData: GeneratedTestsTabData) {
        // Update the number of selected test cases if necessary
        val testCaseIsSelected = generatedTestsTabData.testCaseNameToSelectedCheckbox[testCaseName]!!.isSelected
        val testCaseIsNotHidden = !generatedTestsTabData.hiddenTestCases.contains(testCaseName)
        generatedTestsTabData.hiddenTestCases.remove(testCaseName)
        if (testCaseIsSelected && testCaseIsNotHidden) {
            generatedTestsTabData.testsSelected--
        }

        // Remove the test panel
        generatedTestsTabData.testCaseNameToPanel.remove(testCaseName)

        // Remove the selected checkbox
        generatedTestsTabData.testCaseNameToSelectedCheckbox.remove(testCaseName)

        // Remove the editorTextField
        generatedTestsTabData.testCaseNameToEditorTextField.remove(testCaseName)
    }

    fun showTestCase(
        testCaseName: String,
        generatedTestsTabData: GeneratedTestsTabData,
    ) {
        generatedTestsTabData.hiddenTestCases.remove(testCaseName)
        if (generatedTestsTabData.testCaseNameToSelectedCheckbox[testCaseName]!!.isSelected) {
            generatedTestsTabData.testsSelected++
        }

        update(generatedTestsTabData)
    }

    fun hideTestCase(testCaseName: String, generatedTestsTabData: GeneratedTestsTabData) {
        generatedTestsTabData.hiddenTestCases.add(testCaseName)
        if (generatedTestsTabData.testCaseNameToSelectedCheckbox[testCaseName]!!.isSelected) {
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
