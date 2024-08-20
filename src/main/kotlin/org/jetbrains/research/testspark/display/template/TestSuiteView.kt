package org.jetbrains.research.testspark.display.template

import org.jetbrains.research.testspark.display.TestCasePanelFactory
import javax.swing.JPanel

interface TestSuiteView {
    /**
     * Updates the labels.
     */
    fun updateTopLabels()

    /**
     * Toggles check boxes so that they are either all selected or all not selected,
     *  depending on the provided parameter.
     *
     *  @param selected whether the checkboxes have to be selected or not
     */
    fun toggleAllCheckboxes(selected: Boolean)

    /**
     * Removes all test cases from the cache and tool window UI.
     */
    fun removeAllTestCases()

    /**
     * Executes all test cases.
     *
     * This method presents a caution message to the user and asks for confirmation before executing the test cases.
     * If the user confirms, it iterates through each test case panel factory and runs the corresponding test.
     */
    fun runAllTestCases()

    /**
     * Sets the array of TestCasePanelFactory objects.
     *
     * @param testCasePanelFactories The ArrayList containing the TestCasePanelFactory objects to be set.
     */
    fun setTestCasePanelFactoriesArray(testCasePanelFactories: ArrayList<TestCasePanelFactory>)
    fun getPanel(): JPanel
    fun clear()
}
