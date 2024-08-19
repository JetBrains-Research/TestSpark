package org.jetbrains.research.testspark.actions.llm

import com.intellij.openapi.project.Project
import com.intellij.util.ui.FormBuilder
import org.jetbrains.research.testspark.actions.template.PanelFactory
import org.jetbrains.research.testspark.bundles.plugin.PluginLabelsBundle
import org.jetbrains.research.testspark.core.test.SupportedLanguage
import org.jetbrains.research.testspark.helpers.LLMTestSampleHelper
import java.awt.Font
import javax.swing.ButtonGroup
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton

class LLMSampleSelectorFactory(private val project: Project, private val language: SupportedLanguage) : PanelFactory {
    // init components
    private val selectionTypeButtons: MutableList<JRadioButton> = mutableListOf(
        JRadioButton(PluginLabelsBundle.get("provideTestSample")),
        JRadioButton(PluginLabelsBundle.get("noTestSample")),
    )
    private val selectionTypeButtonGroup = ButtonGroup()
    private val radioButtonsPanel = JPanel()

    private val defaultTestName = "<html>provide manually</html>"
    private val defaultTestCode = "// provide test method code here"
    private val testNames = mutableListOf(defaultTestName)
    private val initialTestCodes = mutableListOf(LLMTestSampleHelper.createTestSampleClass("", defaultTestCode))
    private val testSamplePanelFactories: MutableList<TestSamplePanelFactory> = mutableListOf()
    private var testSamplesCode: String = ""

    private val addButtonPanel = JPanel()
    private val addButton = JButton(PluginLabelsBundle.get("addTestSample"))

    private val nextButton = JButton(PluginLabelsBundle.get("ok"))
    private val backLlmButton = JButton(PluginLabelsBundle.get("back"))

    private var formBuilder = FormBuilder.createFormBuilder()
        .setFormLeftIndent(10)
        .addComponent(JPanel(), 0)
        .addComponent(radioButtonsPanel, 10)
        .addComponent(addButtonPanel, 10)

    private var middlePanel = formBuilder.panel

    init {
        addListeners()

        LLMTestSampleHelper.collectTestSamples(project, testNames, initialTestCodes)
    }

    override fun getTitlePanel(): JPanel {
        val textTitle = JLabel(PluginLabelsBundle.get("llmSampleSelectorFactory"))
        textTitle.font = Font("Monochrome", Font.BOLD, 20)

        val titlePanel = JPanel()
        titlePanel.add(textTitle)

        return titlePanel
    }

    override fun getMiddlePanel(): JPanel {
        for (button in selectionTypeButtons) {
            selectionTypeButtonGroup.add(button)
            radioButtonsPanel.add(button)
        }

        selectionTypeButtons[1].isSelected = true

        addButtonPanel.add(addButton)

        enabledComponents(false)

        middlePanel.revalidate()

        return middlePanel
    }

    override fun getBottomPanel(): JPanel {
        val bottomPanel = JPanel()
        backLlmButton.isOpaque = false
        backLlmButton.isContentAreaFilled = false
        bottomPanel.add(backLlmButton)
        nextButton.isOpaque = false
        nextButton.isContentAreaFilled = false
        bottomPanel.add(nextButton)

        return bottomPanel
    }

    override fun getBackButton() = backLlmButton

    override fun getFinishedButton() = nextButton

    override fun applyUpdates() {
        if (selectionTypeButtons[0].isSelected) {
            for (index in testSamplePanelFactories.indices) {
                testSamplesCode += "Test sample number ${index + 1}\n```\n${testSamplePanelFactories[index].getCode()}\n```\n"
            }
        }
    }

    /**
     * Retrieves the add button.
     *
     * @return The add button.
     */
    fun getAddButton(): JButton = addButton

    /**
     * Retrieves the test samples code.
     *
     * @return The test samples code.
     */
    fun getTestSamplesCode(): String = testSamplesCode

    /**
     * Adds action listeners to the selectionTypeButtons array to enable the nextButton if any button is selected.
     */
    private fun addListeners() {
        selectionTypeButtons[0].addActionListener {
            updateNextButton()
            enabledComponents(true)
        }

        selectionTypeButtons[1].addActionListener {
            updateNextButton()
            enabledComponents(false)
        }

        addButton.addActionListener {
            val testSamplePanelFactory = TestSamplePanelFactory(project, middlePanel, testNames, initialTestCodes, language)
            testSamplePanelFactories.add(testSamplePanelFactory)
            val testSamplePanel = testSamplePanelFactory.getTestSamplePanel()
            val codeScrollPanel = testSamplePanelFactory.getCodeScrollPanel()
            formBuilder = formBuilder
                .addComponent(testSamplePanel, 10)
                .addComponent(codeScrollPanel, 10)
            middlePanel = formBuilder.panel
            middlePanel.revalidate()

            testSamplePanelFactory.getRemoveButton().addActionListener {
                testSamplePanelFactories.remove(testSamplePanelFactory)
                middlePanel.remove(testSamplePanel)
                middlePanel.remove(codeScrollPanel)
                middlePanel.revalidate()

                updateNextButton()
            }

            updateNextButton()
        }
    }

    /**
     * Updates next button.
     */
    private fun updateNextButton() {
        if (selectionTypeButtons[0].isSelected) {
            nextButton.isEnabled = testSamplePanelFactories.isNotEmpty()
        } else {
            nextButton.isEnabled = true
        }
    }

    /**
     * Enables and disables the components in the panel in case of type button selection.
     */
    private fun enabledComponents(isEnabled: Boolean) {
        addButton.isEnabled = isEnabled

        for (testSamplePanelFactory in testSamplePanelFactories) {
            testSamplePanelFactory.enabledComponents(isEnabled)
        }
    }
}
