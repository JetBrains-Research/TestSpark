package org.jetbrains.research.testspark.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.util.ui.FormBuilder
import org.jetbrains.research.testspark.tools.Manager
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

/**
 * Represents an action to be performed in the TestSpark plugin.
 *
 * This class extends the AnAction class and is responsible for handling the action performed event.
 * It creates a dialog wrapper and displays it when the associated action is performed.
 */
class TestSparkAction : AnAction() {
    /**
     * Handles the action performed event.
     *
     * This method is called when the associated action is performed.
     *
     * @param e The AnActionEvent object representing the action event.
     *           It provides information about the event, such as the source of the event and the project context.
     *           This parameter is required.
     */
    override fun actionPerformed(e: AnActionEvent) {
        val dialog = TestSparkActionDialogWrapper(e)
        dialog.showAndGet()
    }

    /**
     * A class representing a custom dialog wrapper.
     *
     * This class extends the DialogWrapper class and provides a dialog for selecting a test generator and code type.
     *
     * @param e The AnActionEvent instance.
     */
    class TestSparkActionDialogWrapper(val e: AnActionEvent) : DialogWrapper(true) {
        private val testGenerators = arrayOf("LLM", "EvoSuite")
        private val codeTypes = getCurrentListOfCodeTypes(e)
        private val testGeneratorComboBox = ComboBox(DefaultComboBoxModel(testGenerators))
        private val scopeComboBox = ComboBox(DefaultComboBoxModel(codeTypes))

        init {
            init()
            title = "TestSpark"
        }

        /**
         * Creates the center panel for the GUI.
         *
         * @return The center panel as a JComponent.
         */
        override fun createCenterPanel(): JComponent {
            return FormBuilder.createFormBuilder()
                .addLabeledComponent(
                    JLabel("Select the test generator:"),
                    testGeneratorComboBox,
                    10,
                    false,
                )
                .addLabeledComponent(
                    JLabel("Select the code type:"),
                    scopeComboBox,
                    10,
                    false,
                )
                .addComponentFillVertically(JPanel(), 0)
                .panel
        }

        /**
         * Executes the action performed when the user clicks the "OK" button.
         * Determines the selected item from the testGeneratorComboBox and scopeComboBox,
         * and calls the appropriate method in the Manager class to generate tests based on the selected options.
         * Finally, it invokes the superclasses doOKAction method.
         */
        override fun doOKAction() {
            when (testGeneratorComboBox.item) {
                testGenerators[0] -> when (scopeComboBox.item) {
                    codeTypes[0] -> Manager.generateTestsForClassByLlm(e)
                    codeTypes[1] -> Manager.generateTestsForMethodByLlm(e)
                    codeTypes[2] -> Manager.generateTestsForLineByLlm(e)
                }
                testGenerators[1] -> when (scopeComboBox.item) {
                    codeTypes[0] -> Manager.generateTestsForClassByEvoSuite(e)
                    codeTypes[1] -> Manager.generateTestsForMethodByEvoSuite(e)
                    codeTypes[2] -> Manager.generateTestsForLineByEvoSuite(e)
                }
            }
            super.doOKAction()
        }
    }
}
