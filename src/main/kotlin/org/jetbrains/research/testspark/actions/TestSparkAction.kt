package org.jetbrains.research.testspark.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogWrapper
import org.jetbrains.research.testspark.tools.Manager
import org.jetbrains.research.testspark.tools.evosuite.EvoSuite
import org.jetbrains.research.testspark.tools.llm.Llm
import javax.swing.BoxLayout
import javax.swing.ButtonGroup
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
        private val llmButton = JRadioButton("<html><b>${Llm().name}</b></html>")
        private val evoSuiteButton = JRadioButton("<html><b>${EvoSuite().name}</b></html>")
        private val testGeneratorButtonGroup = ButtonGroup()
        private val codeTypes = getCurrentListOfCodeTypes(e)
        private val codeTypeButtons: MutableList<JRadioButton> = mutableListOf()
        private val codeTypeButtonGroup = ButtonGroup()

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
            isOKActionEnabled = false

            val panel = JPanel()
            panel.setLayout(BoxLayout(panel, BoxLayout.Y_AXIS))

            testGeneratorButtonGroup.add(llmButton)
            testGeneratorButtonGroup.add(evoSuiteButton)

            val testGeneratorPanel = JPanel()
            testGeneratorPanel.add(JLabel("Select the test generator:"))
            testGeneratorPanel.add(llmButton)
            testGeneratorPanel.add(evoSuiteButton)
            panel.add(testGeneratorPanel)

            for (codeType in codeTypes) {
                val button = JRadioButton(codeType as String)
                codeTypeButtons.add(button)
                codeTypeButtonGroup.add(button)
            }

            val codesToTestPanel = JPanel()
            codesToTestPanel.add(JLabel("Select the code type:"))
            for (button in codeTypeButtons) codesToTestPanel.add(button)
            panel.add(codesToTestPanel)

            addListeners()

            return panel
        }

        private fun addListeners() {
            llmButton.addActionListener {
                update()
            }
            evoSuiteButton.addActionListener {
                update()
            }
            for (button in codeTypeButtons) {
                button.addActionListener { update() }
            }
        }

        private fun update() {
            val isTestGeneratorButtonGroupSelected = llmButton.isSelected || evoSuiteButton.isSelected
            var isCodeTypeButtonGroupSelected = false
            for (button in codeTypeButtons) {
                isCodeTypeButtonGroupSelected = isCodeTypeButtonGroupSelected || button.isSelected
            }
            isOKActionEnabled = isTestGeneratorButtonGroupSelected && isCodeTypeButtonGroupSelected
        }

        /**
         * Executes the action performed when the user clicks the "OK" button.
         * Determines the selected item from the testGeneratorComboBox and scopeComboBox,
         * and calls the appropriate method in the Manager class to generate tests based on the selected options.
         * Finally, it invokes the superclasses doOKAction method.
         */
        override fun doOKAction() {
            if (llmButton.isSelected) {
                if (codeTypeButtons[0].isSelected) Manager.generateTestsForClassByLlm(e)
                if (codeTypeButtons[1].isSelected) Manager.generateTestsForMethodByLlm(e)
                if (codeTypeButtons[2].isSelected) Manager.generateTestsForLineByLlm(e)
                super.doOKAction()
            }
            if (evoSuiteButton.isSelected) {
                if (codeTypeButtons[0].isSelected) Manager.generateTestsForClassByEvoSuite(e)
                if (codeTypeButtons[1].isSelected) Manager.generateTestsForMethodByEvoSuite(e)
                if (codeTypeButtons[2].isSelected) Manager.generateTestsForLineByEvoSuite(e)
                super.doOKAction()
            }
        }
    }
}
