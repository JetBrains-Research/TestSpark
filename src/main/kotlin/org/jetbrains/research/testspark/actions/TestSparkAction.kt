package org.jetbrains.research.testspark.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import org.jetbrains.research.testspark.tools.Manager
import org.jetbrains.research.testspark.tools.evosuite.EvoSuite
import org.jetbrains.research.testspark.tools.llm.Llm
import javax.swing.BoxLayout
import javax.swing.ButtonGroup
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton

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
        private val llmButton = JRadioButton(Llm().name)
        private val evoSuiteButton = JRadioButton(EvoSuite().name)
        private val testGeneratorButtonGroup = ButtonGroup()
        private val codeTypes = getCurrentListOfCodeTypes(e)
        private val codesToTestComboBox = ComboBox(DefaultComboBoxModel(getCurrentListOfCodeTypes(e)))

        init {
            init()
            title = "TestSpark"

            testGeneratorButtonGroup.add(llmButton)
            testGeneratorButtonGroup.add(evoSuiteButton)

            isOKActionEnabled = false

            addListeners()
        }

        private fun addListeners() {
            llmButton.addActionListener {
                isOKActionEnabled = true
            }
            evoSuiteButton.addActionListener {
                isOKActionEnabled = true
            }
        }

        /**
         * Creates the center panel for the GUI.
         *
         * @return The center panel as a JComponent.
         */
        override fun createCenterPanel(): JComponent {
            val panel = JPanel()
            panel.setLayout(BoxLayout(panel, BoxLayout.Y_AXIS))

            val testGeneratorPanel = JPanel()
            testGeneratorPanel.add(JLabel("Select the test generator:"))
            testGeneratorPanel.add(llmButton)
            testGeneratorPanel.add(evoSuiteButton)
            panel.add(testGeneratorPanel)

            val codesToTestPanel = JPanel()
            codesToTestPanel.add(JLabel("Select the code type:"))
            codesToTestPanel.add(codesToTestComboBox)
            panel.add(codesToTestPanel)

            return panel
        }

        /**
         * Executes the action performed when the user clicks the "OK" button.
         * Determines the selected item from the testGeneratorComboBox and scopeComboBox,
         * and calls the appropriate method in the Manager class to generate tests based on the selected options.
         * Finally, it invokes the superclasses doOKAction method.
         */
        override fun doOKAction() {
            if (llmButton.isSelected) {
                when (codesToTestComboBox.item) {
                    codeTypes[0] -> Manager.generateTestsForClassByLlm(e)
                    codeTypes[1] -> Manager.generateTestsForMethodByLlm(e)
                    codeTypes[2] -> Manager.generateTestsForLineByLlm(e)
                }
                super.doOKAction()
            }
            if (evoSuiteButton.isSelected) {
                when (codesToTestComboBox.item) {
                    codeTypes[0] -> Manager.generateTestsForClassByEvoSuite(e)
                    codeTypes[1] -> Manager.generateTestsForMethodByEvoSuite(e)
                    codeTypes[2] -> Manager.generateTestsForLineByEvoSuite(e)
                }
                super.doOKAction()
            }
        }
    }
}
