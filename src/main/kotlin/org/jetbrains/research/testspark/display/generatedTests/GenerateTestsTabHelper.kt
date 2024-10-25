package org.jetbrains.research.testspark.display.generatedTests

import javax.swing.JPanel

object GenerateTestsTabHelper {
    /**
     * A helper method to remove a test case from the cache and from the UI.
     *
     * @param testCaseName the name of the test
     */
    fun removeTestCase(testCaseName: String, generatedTestsTabData: GeneratedTestsTabData) {
        // Remove the test from the cache if it is already removed from the UI
        if (generatedTestsTabData.cacheIndexToDeletedTestCaseName.containsValue(testCaseName)) {
            removeTestFromCache(generatedTestsTabData, testCaseName)
        } else {
            // Update the number of selected test cases if necessary
            if (generatedTestsTabData.testCaseNameToSelectedCheckbox[testCaseName]!!.isSelected) {
                generatedTestsTabData.testsSelected--
            }

            // Remove the test panel from the UI
            generatedTestsTabData.allTestCasePanel.remove(generatedTestsTabData.testCaseNameToPanel[testCaseName])

            // Remove the selected checkbox
            generatedTestsTabData.testCaseNameToSelectedCheckbox.remove(testCaseName)

            // Remove the editorTextField
            generatedTestsTabData.testCaseNameToEditorTextField.remove(testCaseName)
        }

        // Remove the test panel
        generatedTestsTabData.testCaseNameToPanel.remove(testCaseName)
    }

    /**
     * Helper function that removes a test from the cache if it stored.
     *
     * @param testCaseName the name of the test
     */
    private fun removeTestFromCache(
        generatedTestsTabData: GeneratedTestsTabData,
        testCaseName: String
    ) {
        if (generatedTestsTabData.cacheTestCaseNameToSelectedCheckbox.containsKey(testCaseName)) {
            generatedTestsTabData.cacheTestCaseNameToSelectedCheckbox.remove(testCaseName)
        }
        if (generatedTestsTabData.cacheTestCaseNameToEditorTextField.containsKey(testCaseName)) {
            generatedTestsTabData.cacheTestCaseNameToEditorTextField.remove(testCaseName)
        }
        if (generatedTestsTabData.cacheIndexToDeletedTestCaseName.containsValue(testCaseName)) {
            generatedTestsTabData.cacheIndexToDeletedTestCaseName.remove(
                generatedTestsTabData.allTestCasePanel.getComponentZOrder(
                    generatedTestsTabData.testCaseNameToPanel[testCaseName]
                )
            )
        }
    }

    /**
     * A helper method to remove one test case from the UI and store it in the cache.
     *
     * @param testCaseName the name of the test
     */
    fun removeTestCaseFromUI(testCaseName: String, generatedTestsTabData: GeneratedTestsTabData): Int {
        // Update the number of selected test cases if necessary
        if (generatedTestsTabData.testCaseNameToSelectedCheckbox[testCaseName]!!.isSelected) {
            generatedTestsTabData.testsSelected--
        }

        // Find the index at which the test is
        val index = generatedTestsTabData.allTestCasePanel
            .getComponentZOrder(generatedTestsTabData.testCaseNameToPanel[testCaseName])

        // Remove the test from the UI
        generatedTestsTabData.allTestCasePanel.remove(index)

        // Add test name to the deleted list
        generatedTestsTabData.cacheIndexToDeletedTestCaseName[index] = testCaseName

        // Remove the selected checkbox, but save it in the cache
        generatedTestsTabData.cacheTestCaseNameToSelectedCheckbox[testCaseName] =
            generatedTestsTabData.testCaseNameToSelectedCheckbox[testCaseName]!!
        generatedTestsTabData.testCaseNameToSelectedCheckbox.remove(testCaseName)

        // Remove the editorTextField, but save it in the cache
        generatedTestsTabData.cacheTestCaseNameToEditorTextField[testCaseName] =
            generatedTestsTabData.testCaseNameToEditorTextField[testCaseName]!!
        generatedTestsTabData.testCaseNameToEditorTextField.remove(testCaseName)

        return index
    }

    fun restoreTestCaseToUI(testCaseName: String, generatedTestsTabData: GeneratedTestsTabData, undoPanel: JPanel) {
        // Remove the Undo panel
        val index: Int = generatedTestsTabData.allTestCasePanel.getComponentZOrder(undoPanel)
        generatedTestsTabData.allTestCasePanel.remove(index)

        // Add the test back to the original position
        val testPanel =
            generatedTestsTabData.testCaseNameToPanel[generatedTestsTabData.cacheIndexToDeletedTestCaseName[index]]
        generatedTestsTabData.allTestCasePanel.add(testPanel, index)

        // Restore the checkbox
        generatedTestsTabData.testCaseNameToSelectedCheckbox[testCaseName] =
            generatedTestsTabData.cacheTestCaseNameToSelectedCheckbox[testCaseName]!!

        // Restore the editorTextField
        generatedTestsTabData.testCaseNameToEditorTextField[testCaseName] =
            generatedTestsTabData.cacheTestCaseNameToEditorTextField[testCaseName]!!

        // Update the number of selected test cases if necessary
        if (generatedTestsTabData.testCaseNameToSelectedCheckbox[testCaseName]!!.isSelected) {
            generatedTestsTabData.testsSelected++
        }

        // Remove the re-added test from the cache
        removeTestFromCache(generatedTestsTabData, generatedTestsTabData.cacheIndexToDeletedTestCaseName[index]!!)
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
