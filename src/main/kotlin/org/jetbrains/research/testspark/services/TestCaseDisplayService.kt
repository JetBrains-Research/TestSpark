package org.jetbrains.research.testspark.services

import com.intellij.psi.PsiFile
import com.intellij.ui.EditorTextField
import org.jetbrains.research.testspark.core.data.Report
import org.jetbrains.research.testspark.core.test.SupportedLanguage
import org.jetbrains.research.testspark.data.UIContext
import org.jetbrains.research.testspark.langwrappers.PsiClassWrapper
import javax.swing.JPanel

interface TestCaseDisplayService {

    /**
     * Fill the panel with the generated test cases. Remove all previously shown test cases.
     * Add Tests and their names to a List of pairs (used for highlighting)
     */
    fun displayTestCases(report: Report, uiContext: UIContext, language: SupportedLanguage)

    /**
     * Adds a separator to the allTestCasePanel.
     */
    fun addSeparator()

    /**
     * Highlight the mini-editor in the tool window whose name corresponds with the name of the test provided
     *
     * @param name name of the test whose editor should be highlighted
     */
    fun highlightTestCase(name: String)

    /**
     * Method to open the toolwindow tab with generated tests if not already open.
     */
    fun openToolWindowTab()

    /**
     * Scrolls to the highlighted panel.
     *
     * @param myPanel the panel to scroll to
     */
    fun scrollToPanel(myPanel: JPanel)

    /**
     * Removes all coverage highlighting from the editor.
     */
    fun removeAllHighlights()

    /**
     * Reset the provided editors color to the default (initial) one after 10 seconds
     * @param editor the editor whose color to change
     */
    fun returnOriginalEditorBackground(editor: EditorTextField)

    /**
     * Highlight a range of editors
     * @param names list of test names to pass to highlight function
     */
    fun highlightCoveredMutants(names: List<String>)

    /**
     * Show a dialog where the user can select what test class the tests should be applied to,
     * and apply the selected tests to the test class.
     */
    fun applyTests()

    fun showErrorWindow(message: String)

    /**
     * Retrieve the editor corresponding to a particular test case
     *
     * @param testCaseName the name of the test case
     * @return the editor corresponding to the test case, or null if it does not exist
     */
    fun getEditor(testCaseName: String): EditorTextField?

    /**
     * Append the provided test cases to the provided class.
     *
     * @param testCaseComponents the test cases to be appended
     * @param selectedClass the class which the test cases should be appended to
     * @param outputFile the output file for tests
     */
    fun appendTestsToClass(testCaseComponents: List<String>, selectedClass: PsiClassWrapper, outputFile: PsiFile)

    /**
     * Utility function that returns the editor for a specific file url,
     * in case it is opened in the IDE
     */
    fun updateEditorForFileUrl(fileUrl: String)

    /**
     * Creates a new toolWindow tab for the coverage visualisation.
     */
    fun createToolWindowTab()

    /**
     * Closes the tool window and destroys the content of the tab.
     */
    fun closeToolWindow()

    /**
     * Removes the selected tests from the cache, removes all the highlights from the editor and closes the tool window.
     * This function is called when the user clicks "Apply to test suite" button,
     *  and it is also called with all test cases as selected when the user clicks "Remove All" button.
     *
     * @param selectedTestCasePanels the panels of the selected tests
     */
    fun removeSelectedTestCases(selectedTestCasePanels: Map<String, JPanel>)

    fun clear()

    /**
     * A helper method to remove a test case from the cache and from the UI.
     *
     * @param testCaseName the name of the test
     */
    fun removeTestCase(testCaseName: String)

    /**
     * Updates the user interface of the tool window.
     *
     * This method updates the UI of the tool window tab by calling the updateUI
     * method of the allTestCasePanel object and the updateTopLabels method
     * of the topButtonsPanel object. It also checks if there are no more tests remaining
     * and closes the tool window if that is the case.
     */
    fun updateUI()

    /**
     * Retrieves the list of test case panels.
     *
     * @return The list of test case panels.
     */
    fun getTestCasePanels(): HashMap<String, JPanel>

    /**
     * Retrieves the currently selected tests.
     *
     * @return The list of tests currently selected.
     */
    fun getTestsSelected(): Int

    /**
     * Sets the number of tests selected.
     *
     * @param testsSelected The number of tests selected.
     */
    fun setTestsSelected(testsSelected: Int)
}
