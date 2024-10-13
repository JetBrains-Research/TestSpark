package org.jetbrains.research.testspark.display.generatedTests

object GenerateTestsTabHelper {
    /**
     * A helper method to remove a test case from the cache.
     *
     * @param testCaseName the name of the test
     */
    fun removeTestCase(testCaseName: String, generatedTestsTabData: GeneratedTestsTabData) {
        // Remove the test panel
        val panel = generatedTestsTabData.testCaseNameToPanel.remove(testCaseName)

        // Remove the selected checkbox
        val checkbox = generatedTestsTabData.testCaseNameToSelectedCheckbox.remove(testCaseName)

        // Remove the editorTextField
        val editorTextField = generatedTestsTabData.testCaseNameToEditorTextField.remove(testCaseName)

        // Save data for possible restoration
        val deletedTest = DeletedTest(panel!!, checkbox!!, editorTextField!!)
        generatedTestsTabData.testCaseNameToDeletedTestData[testCaseName] = deletedTest
    }

    /**
     * A helper method to restore a previously deleted test case to the cache.
     *
     * @param testCaseName the name of the test
     */
    fun restoreTestCase(testCaseName: String, generatedTestsTabData: GeneratedTestsTabData) {
        // Retrieve information about previously deleted test
        val deletedTest = generatedTestsTabData.testCaseNameToDeletedTestData.remove(testCaseName)!!

        // Re-add the test panel
        generatedTestsTabData.testCaseNameToPanel[testCaseName] = deletedTest.panel

        // Re-add the selected checkbox
        generatedTestsTabData.testCaseNameToSelectedCheckbox[testCaseName] = deletedTest.checkbox

        // Re-add the editorTextField
        generatedTestsTabData.testCaseNameToEditorTextField[testCaseName] = deletedTest.editorTextField
    }

    /**
     * A helper method to remove all test cases from the cache and UI.
     */
    fun clear(generatedTestsTabData: GeneratedTestsTabData) {
        generatedTestsTabData.allTestCasePanel.removeAll()
        generatedTestsTabData.testCaseNameToPanel.clear()
        generatedTestsTabData.testCaseNameToSelectedCheckbox.clear()
        generatedTestsTabData.testCaseNameToEditorTextField.clear()
        generatedTestsTabData.testCaseNameToDeletedTestData.clear()
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
