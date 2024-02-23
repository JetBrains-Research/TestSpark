package org.jetbrains.research.testspark.actions.llm

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.ui.FormBuilder
import org.jetbrains.research.testspark.actions.template.PanelFactory
import org.jetbrains.research.testspark.bundles.TestSparkLabelsBundle
import org.jetbrains.research.testspark.data.JUnitVersion
import org.jetbrains.research.testspark.services.LLMTestSampleService
import java.awt.Font
import javax.swing.ButtonGroup
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton

class LLMSampleSelectorFactory(private val project: Project) : PanelFactory {
    private val selectionTypeButtons: MutableList<JRadioButton> = mutableListOf(
        JRadioButton(TestSparkLabelsBundle.defaultValue("provideTestSample")),
        JRadioButton(TestSparkLabelsBundle.defaultValue("noTestSample")),
    )
    private val selectionTypeButtonGroup = ButtonGroup()
    private val radioButtonsPanel = JPanel()

    private val addButtonPanel = JPanel()
    private val addButton = JButton(TestSparkLabelsBundle.defaultValue("addTestSample"))

    private val backLlmButton = JButton(TestSparkLabelsBundle.defaultValue("back"))
    private val nextButton = JButton(TestSparkLabelsBundle.defaultValue("ok"))

    private val defaultTestName = "<html>provide manually</html>"
    private val defaultTestCode = "// provide test method code here"

    private val testNames = mutableListOf(defaultTestName)
    private val initialTestCodes =
        mutableListOf(project.service<LLMTestSampleService>().createTestSampleClass("", defaultTestCode))

    private val testSamplePanelFactories: MutableList<TestSamplePanelFactory> = mutableListOf()

    private var numberOfTestSamples = 1

    private var formBuilder = FormBuilder.createFormBuilder()
        .setFormLeftIndent(10)
        .addComponent(JPanel(), 0)
        .addComponent(radioButtonsPanel, 10)
        .addComponent(addButtonPanel, 10)

    private var middlePanel = formBuilder.panel

    init {
        addListeners()

        project.service<LLMTestSampleService>().collectTestSamples(project, testNames, initialTestCodes)
    }

    /**
     * Adds action listeners to the selectionTypeButtons array to enable the nextButton if any button is selected.
     */
    private fun addListeners() {
        selectionTypeButtons[0].addActionListener {
            nextButton.isEnabled = true
            enabledComponents(true)
        }

        selectionTypeButtons[1].addActionListener {
            nextButton.isEnabled = true
            enabledComponents(false)
        }

        addButton.addActionListener {
            val testSamplePanelFactory = TestSamplePanelFactory(project, middlePanel, testNames, initialTestCodes, numberOfTestSamples++)
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
            }
        }
    }

    /**
     * Returns a JPanel object representing the title panel.
     * The panel contains a JLabel with the text "llmSampleSelectorFactory",
     * rendered in a bold 20pt Monochrome font.
     *
     * @return a JPanel object representing the title panel
     */
    override fun getTitlePanel(): JPanel {
        val textTitle = JLabel(TestSparkLabelsBundle.defaultValue("llmSampleSelectorFactory"))
        textTitle.font = Font("Monochrome", Font.BOLD, 20)

        val titlePanel = JPanel()
        titlePanel.add(textTitle)

        return titlePanel
    }

    /**
     * Returns the middle panel containing radio buttons, a test samples selector, and a language text field scroll pane.
     *
     * @return the middle panel as a JPanel
     */
    override fun getMiddlePanel(junit: JUnitVersion?): JPanel {
        for (button in selectionTypeButtons) {
            selectionTypeButtonGroup.add(button)
            radioButtonsPanel.add(button)
        }

        addButtonPanel.add(addButton)

        enabledComponents(false)

        middlePanel.revalidate()

        return middlePanel
    }

    /**
     * Retrieves the bottom panel containing the back and next buttons.
     *
     * @return The JPanel containing the back and next buttons.
     */
    override fun getBottomPanel(): JPanel {
        val bottomPanel = JPanel()
        backLlmButton.isOpaque = false
        backLlmButton.isContentAreaFilled = false
        bottomPanel.add(backLlmButton)
        nextButton.isOpaque = false
        nextButton.isContentAreaFilled = false
        nextButton.isEnabled = false
        bottomPanel.add(nextButton)

        return bottomPanel
    }

    /**
     * Retrieves the back button.
     *
     * @return The back button.
     */
    override fun getBackButton() = backLlmButton

    /**
     * Retrieves the add button.
     *
     * @return The add button.
     */
    fun getAddButton() = addButton

    /**
     * Retrieves the reference to the "OK" button.
     *
     * @return The reference to the "OK" button.
     */
    override fun getFinishedButton() = nextButton

    override fun applyUpdates() {
        if (selectionTypeButtons[0].isSelected) {
//            TODO uncomment
//            project.service<LLMTestSampleService>().setTestSample(languageTextField.text)
        } else {
            project.service<LLMTestSampleService>().setTestSample(null)
        }
    }

    private fun enabledComponents(isEnabled: Boolean) {
        addButton.isEnabled = isEnabled

        for (testSamplePanelFactory in testSamplePanelFactories) {
            testSamplePanelFactory.enabledComponents(isEnabled)
        }
    }
}
