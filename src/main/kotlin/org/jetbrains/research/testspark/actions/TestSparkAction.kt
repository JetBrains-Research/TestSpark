package org.jetbrains.research.testspark.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import java.awt.CardLayout
import java.awt.Dimension
import java.awt.Toolkit
import javax.swing.BoxLayout
import javax.swing.ButtonGroup
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton
import org.jetbrains.research.testspark.tools.Manager
import org.jetbrains.research.testspark.tools.evosuite.EvoSuite
import org.jetbrains.research.testspark.tools.llm.Llm

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
        TestSparkActionWindow(e)
    }

    /**
     * Class representing the TestSparkActionWindow.
     *
     * @property e The AnActionEvent object.
     */
    class TestSparkActionWindow(val e: AnActionEvent) : JFrame("TestSpark") {
        private val llmButton = JRadioButton("<html><b>${Llm().name}</b></html>")
        private val evoSuiteButton = JRadioButton("<html><b>${EvoSuite().name}</b></html>")
        private val testGeneratorButtonGroup = ButtonGroup()
        private val codeTypes = getCurrentListOfCodeTypes(e)
        private val codeTypeButtons: MutableList<JRadioButton> = mutableListOf()
        private val codeTypeButtonGroup = ButtonGroup()

        private val nextButton = JButton("Next")
        private val backButton = JButton("Back")
        private val okButton = JButton("OK")

        private val cardLayout = CardLayout()

        init {
            val panel = JPanel(cardLayout)

            panel.add(getMainPanel(), "1")
            panel.add(getLlmPanel(), "2")

            addListeners(panel)

            add(panel)

            pack()

            val dimension: Dimension = Toolkit.getDefaultToolkit().screenSize
            val x = (dimension.width - size.width) / 2
            val y = (dimension.height - size.height) / 2
            setLocation(x, y)

            isVisible = true
        }

        private fun getMainPanel(): JPanel {
            val mainPanel = JPanel()
            mainPanel.setLayout(BoxLayout(mainPanel, BoxLayout.Y_AXIS))

            testGeneratorButtonGroup.add(llmButton)
            testGeneratorButtonGroup.add(evoSuiteButton)

            val testGeneratorPanel = JPanel()
            testGeneratorPanel.add(JLabel("Select the test generator:"))
            testGeneratorPanel.add(llmButton)
            testGeneratorPanel.add(evoSuiteButton)
            mainPanel.add(testGeneratorPanel)

            for (codeType in codeTypes) {
                val button = JRadioButton(codeType as String)
                codeTypeButtons.add(button)
                codeTypeButtonGroup.add(button)
            }

            val codesToTestPanel = JPanel()
            codesToTestPanel.add(JLabel("Select the code type:"))
            for (button in codeTypeButtons) codesToTestPanel.add(button)
            mainPanel.add(codesToTestPanel)

            nextButton.isEnabled = false
            mainPanel.add(nextButton)

            return mainPanel
        }

        private fun getLlmPanel(): JPanel {
            val llmPanel = JPanel()

            llmPanel.add(backButton)
            llmPanel.add(okButton)

            return llmPanel
        }

        private fun addListeners(panel: JPanel) {
            llmButton.addActionListener {
                update()
            }
            evoSuiteButton.addActionListener {
                update()
            }
            for (button in codeTypeButtons) {
                button.addActionListener { update() }
            }
            nextButton.addActionListener {
                cardLayout.next(panel)
                pack()
            }
            backButton.addActionListener {
                cardLayout.previous(panel)
                pack()
            }
            okButton.addActionListener {
                if (llmButton.isSelected) {
                    if (codeTypeButtons[0].isSelected) Manager.generateTestsForClassByLlm(e)
                    if (codeTypeButtons[1].isSelected) Manager.generateTestsForMethodByLlm(e)
                    if (codeTypeButtons[2].isSelected) Manager.generateTestsForLineByLlm(e)
                }
                if (evoSuiteButton.isSelected) {
                    if (codeTypeButtons[0].isSelected) Manager.generateTestsForClassByEvoSuite(e)
                    if (codeTypeButtons[1].isSelected) Manager.generateTestsForMethodByEvoSuite(e)
                    if (codeTypeButtons[2].isSelected) Manager.generateTestsForLineByEvoSuite(e)
                }
                dispose()
            }
        }

        private fun update() {
            val isTestGeneratorButtonGroupSelected = llmButton.isSelected || evoSuiteButton.isSelected
            var isCodeTypeButtonGroupSelected = false
            for (button in codeTypeButtons) {
                isCodeTypeButtonGroupSelected = isCodeTypeButtonGroupSelected || button.isSelected
            }
            nextButton.isEnabled = isTestGeneratorButtonGroupSelected && isCodeTypeButtonGroupSelected
        }
    }
}
