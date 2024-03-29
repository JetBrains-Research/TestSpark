package org.jetbrains.research.testspark.actions

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.FormBuilder
import org.jetbrains.research.testspark.actions.evosuite.EvoSuitePanelFactory
import org.jetbrains.research.testspark.actions.llm.LLMSampleSelectorFactory
import org.jetbrains.research.testspark.actions.llm.LLMSetupPanelFactory
import org.jetbrains.research.testspark.actions.template.PanelFactory
import org.jetbrains.research.testspark.bundles.TestSparkBundle
import org.jetbrains.research.testspark.bundles.TestSparkLabelsBundle
import org.jetbrains.research.testspark.display.TestSparkIcons
import org.jetbrains.research.testspark.helpers.getCurrentListOfCodeTypes
import org.jetbrains.research.testspark.services.SettingsApplicationService
import org.jetbrains.research.testspark.settings.SettingsApplicationState
import org.jetbrains.research.testspark.tools.Manager
import org.jetbrains.research.testspark.tools.evosuite.EvoSuite
import org.jetbrains.research.testspark.tools.llm.Llm
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.Toolkit
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.ButtonGroup
import javax.swing.JButton
import javax.swing.JFrame
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
    class VisibilityController {
        var isVisible = false
    }

    private val visibilityController = VisibilityController()

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
        TestSparkActionWindow(e, visibilityController)
    }

    /**
     * Updates the state of the action based on the provided event.
     *
     * @param e the AnActionEvent object representing the event
     */
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = getCurrentListOfCodeTypes(e) != null
    }

    /**
     * Class representing the TestSparkActionWindow.
     *
     * @property e The AnActionEvent object.
     */
    class TestSparkActionWindow(e: AnActionEvent, private val visibilityController: VisibilityController) :
        JFrame("TestSpark") {
        private val project: Project = e.project!!
        private val settingsState: SettingsApplicationState
            get() = project.getService(SettingsApplicationService::class.java).state

        private val llmButton = JRadioButton("<html><b>${Llm().name}</b></html>")
        private val evoSuiteButton = JRadioButton("<html><b>${EvoSuite().name}</b></html>")
        private val testGeneratorButtonGroup = ButtonGroup()
        private val codeTypes = getCurrentListOfCodeTypes(e)!!
        private val psiFile: PsiFile = e.dataContext.getData(CommonDataKeys.PSI_FILE)!!
        private val caretOffset: Int = e.dataContext.getData(CommonDataKeys.CARET)?.caretModel?.primaryCaret!!.offset
        private val fileUrl = e.dataContext.getData(CommonDataKeys.VIRTUAL_FILE)!!.presentableUrl
        private val codeTypeButtons: MutableList<JRadioButton> = mutableListOf()
        private val codeTypeButtonGroup = ButtonGroup()

        private val nextButton = JButton(TestSparkLabelsBundle.defaultValue("next"))

        private val cardLayout = CardLayout()

        private val llmSetupPanelFactory = LLMSetupPanelFactory(e, project)
        private val llmSampleSelectorFactory = LLMSampleSelectorFactory(project)
        private val evoSuitePanelFactory = EvoSuitePanelFactory(project)

        init {
            if (!visibilityController.isVisible) {
                visibilityController.isVisible = true
                isVisible = true

                val panel = JPanel(cardLayout)

                panel.add(getMainPanel(), "1")
                panel.add(createCardPanel(evoSuitePanelFactory), "2")
                panel.add(createCardPanel(llmSetupPanelFactory), "3")

                panel.add(
                    JBScrollPane(
                        createCardPanel(llmSampleSelectorFactory),
                        JBScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                        JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER,
                    ),
                    "4",
                )

                addListeners(panel)

                add(panel)

                pack()

                val dimension: Dimension = Toolkit.getDefaultToolkit().screenSize
                val x = (dimension.width - size.width) / 2
                val y = (dimension.height - size.height) / 2
                setLocation(x, y)
            } else {
                NotificationGroupManager.getInstance()
                    .getNotificationGroup("Generation Error")
                    .createNotification(
                        TestSparkBundle.message("generationWindowWarningTitle"),
                        TestSparkBundle.message("generationWindowWarningMessage"),
                        NotificationType.WARNING,
                    )
                    .notify(e.project)
            }
        }

        private fun createCardPanel(toolPanelFactory: PanelFactory): JPanel {
            val cardPanel = JPanel(BorderLayout())
            cardPanel.add(toolPanelFactory.getTitlePanel(), BorderLayout.NORTH)
            cardPanel.add(toolPanelFactory.getMiddlePanel(), BorderLayout.CENTER)
            cardPanel.add(toolPanelFactory.getBottomPanel(), BorderLayout.SOUTH)

            return cardPanel
        }

        /**
         * Returns the main panel for the test generator UI.
         * This panel contains options for selecting the test generator and the code type.
         * It also includes a button for proceeding to the next step.
         *
         * @return the main panel for the test generator UI
         */
        private fun getMainPanel(): JPanel {
            val panelTitle = JPanel()
            val textTitle = JLabel("Welcome to TestSpark!")
            textTitle.font = Font("Monochrome", Font.BOLD, 20)
            panelTitle.add(JLabel(TestSparkIcons.pluginIcon))
            panelTitle.add(textTitle)

            testGeneratorButtonGroup.add(llmButton)
            testGeneratorButtonGroup.add(evoSuiteButton)

            val testGeneratorPanel = JPanel()
            testGeneratorPanel.add(JLabel("Select the test generator:"))
            testGeneratorPanel.add(llmButton)
            testGeneratorPanel.add(evoSuiteButton)

            for (codeType in codeTypes) {
                val button = JRadioButton(codeType as String)
                codeTypeButtons.add(button)
                codeTypeButtonGroup.add(button)
            }

            val codesToTestPanel = JPanel()
            codesToTestPanel.add(JLabel("Select the code type:"))
            if (codeTypeButtons.size == 1) codeTypeButtons[0].isSelected = true
            for (button in codeTypeButtons) codesToTestPanel.add(button)

            val middlePanel = FormBuilder.createFormBuilder()
                .setFormLeftIndent(10)
                .addComponent(
                    testGeneratorPanel,
                    10,
                )
                .addComponent(
                    codesToTestPanel,
                    10,
                )
                .panel

            val nextButtonPanel = JPanel()
            nextButton.isEnabled = false
            nextButtonPanel.add(nextButton)

            val cardPanel = JPanel(BorderLayout())
            cardPanel.add(panelTitle, BorderLayout.NORTH)
            cardPanel.add(middlePanel, BorderLayout.CENTER)
            cardPanel.add(nextButtonPanel, BorderLayout.SOUTH)

            return cardPanel
        }

        /**
         * Adds listeners to various components in the given panel.
         *
         * @param panel the JPanel to add listeners to
         */
        private fun addListeners(panel: JPanel) {
            addWindowListener(object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent?) {
                    visibilityController.isVisible = false
                }
            })

            llmButton.addActionListener {
                updateNextButton()
            }

            evoSuiteButton.addActionListener {
                updateNextButton()
            }

            for (button in codeTypeButtons) {
                button.addActionListener {
                    llmSetupPanelFactory.setPromptEditorType(button.text)
                    updateNextButton()
                }
            }

            nextButton.addActionListener {
                if (llmButton.isSelected && !settingsState.llmSetupCheckBoxSelected && !settingsState.provideTestSamplesCheckBoxSelected) {
                    startLLMGeneration()
                } else if (llmButton.isSelected && !settingsState.llmSetupCheckBoxSelected) {
                    cardLayout.next(panel)
                    cardLayout.next(panel)
                    cardLayout.next(panel)
                    pack()
                } else if (llmButton.isSelected) {
                    cardLayout.next(panel)
                    cardLayout.next(panel)
                    pack()
                } else if (evoSuiteButton.isSelected && !settingsState.evosuiteSetupCheckBoxSelected) {
                    startEvoSuiteGeneration()
                } else {
                    cardLayout.next(panel)
                    pack()
                }
            }

            evoSuitePanelFactory.getBackButton().addActionListener {
                cardLayout.previous(panel)
                pack()
            }

            llmSetupPanelFactory.getBackButton().addActionListener {
                cardLayout.previous(panel)
                cardLayout.previous(panel)
                pack()
            }

            llmSetupPanelFactory.getFinishedButton().addActionListener {
                llmSetupPanelFactory.applyUpdates()
                if (settingsState.provideTestSamplesCheckBoxSelected) {
                    cardLayout.next(panel)
                } else {
                    startLLMGeneration()
                }
            }

            llmSampleSelectorFactory.getAddButton().addActionListener {
                size = Dimension(width, 500)
            }

            llmSampleSelectorFactory.getBackButton().addActionListener {
                if (settingsState.llmSetupCheckBoxSelected) {
                    cardLayout.previous(panel)
                } else {
                    cardLayout.previous(panel)
                    cardLayout.previous(panel)
                    cardLayout.previous(panel)
                }
                pack()
            }

            llmSampleSelectorFactory.getFinishedButton().addActionListener {
                llmSampleSelectorFactory.applyUpdates()
                startLLMGeneration()
            }

            evoSuitePanelFactory.getFinishedButton().addActionListener {
                evoSuitePanelFactory.applyUpdates()
                startEvoSuiteGeneration()
            }
        }

        private fun startEvoSuiteGeneration() {
            val testSamplesCode = llmSampleSelectorFactory.getTestSamplesCode()

            if (codeTypeButtons[0].isSelected) {
                Manager.generateTestsForClassByEvoSuite(project, psiFile, caretOffset, fileUrl, testSamplesCode)
            } else if (codeTypeButtons[1].isSelected) {
                Manager.generateTestsForMethodByEvoSuite(project, psiFile, caretOffset, fileUrl, testSamplesCode)
            } else if (codeTypeButtons[2].isSelected) {
                Manager.generateTestsForLineByEvoSuite(project, psiFile, caretOffset, fileUrl, testSamplesCode)
            }

            visibilityController.isVisible = false
            dispose()
        }

        private fun startLLMGeneration() {
            val testSamplesCode = llmSampleSelectorFactory.getTestSamplesCode()

            if (codeTypeButtons[0].isSelected) {
                Manager.generateTestsForClassByLlm(project, psiFile, caretOffset, fileUrl, testSamplesCode)
            } else if (codeTypeButtons[1].isSelected) {
                Manager.generateTestsForMethodByLlm(project, psiFile, caretOffset, fileUrl, testSamplesCode)
            } else if (codeTypeButtons[2].isSelected) {
                Manager.generateTestsForLineByLlm(project, psiFile, caretOffset, fileUrl, testSamplesCode)
            }

            visibilityController.isVisible = false
            dispose()
        }

        /**
         * Updates the state of the "Next" button based on the selected options.
         * The "Next" button is enabled only if a test generator button (llmButton or evoSuiteButton) and at least one
         * code type button (from codeTypeButtons) are selected.
         *
         * This method should be called whenever the mentioned above buttons are clicked.
         */
        private fun updateNextButton() {
            val isTestGeneratorButtonGroupSelected = llmButton.isSelected || evoSuiteButton.isSelected
            var isCodeTypeButtonGroupSelected = false
            for (button in codeTypeButtons) {
                isCodeTypeButtonGroupSelected = isCodeTypeButtonGroupSelected || button.isSelected
            }
            nextButton.isEnabled = isTestGeneratorButtonGroupSelected && isCodeTypeButtonGroupSelected

            if ((llmButton.isSelected && !settingsState.llmSetupCheckBoxSelected && !settingsState.provideTestSamplesCheckBoxSelected) ||
                (evoSuiteButton.isSelected && !settingsState.evosuiteSetupCheckBoxSelected)
            ) {
                nextButton.text = TestSparkLabelsBundle.defaultValue("ok")
            } else {
                nextButton.text = TestSparkLabelsBundle.defaultValue("next")
            }
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}
