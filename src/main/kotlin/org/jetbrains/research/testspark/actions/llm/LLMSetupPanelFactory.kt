package org.jetbrains.research.testspark.actions.llm

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import org.jetbrains.research.testspark.actions.template.ToolPanelFactory
import org.jetbrains.research.testspark.bundles.TestSparkLabelsBundle
import org.jetbrains.research.testspark.bundles.TestSparkToolTipsBundle
import org.jetbrains.research.testspark.helpers.addLLMPanelListeners
import org.jetbrains.research.testspark.helpers.isGrazieClassLoaded
import org.jetbrains.research.testspark.helpers.updateModelSelector
import org.jetbrains.research.testspark.services.SettingsApplicationService
import java.awt.Font
import javax.swing.DefaultComboBoxModel
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

class LLMSetupPanelFactory : ToolPanelFactory {
    private val defaultModulesArray = arrayOf("")
    private var modelSelector = ComboBox(defaultModulesArray)
    private var llmUserTokenField = JTextField(30)
    private var platformSelector = ComboBox(arrayOf(TestSparkLabelsBundle.defaultValue("openAI")))
    private val backLlmButton = JButton(TestSparkLabelsBundle.defaultValue("back"))
    private val okLlmButton = JButton(TestSparkLabelsBundle.defaultValue("next"))

    private val settingsState = SettingsApplicationService.getInstance().state!!

    init {
        addLLMPanelListeners(
            platformSelector,
            modelSelector,
            llmUserTokenField,
            defaultModulesArray,
        )
    }

    /**
     * Retrieves the back button.
     *
     * @return The back button.
     */
    override fun getBackButton() = backLlmButton

    /**
     * Retrieves the reference to the "OK" button.
     *
     * @return The reference to the "OK" button.
     */
    override fun getFinishedButton() = okLlmButton

    /**
     * Retrieves the LLM panel.
     *
     * @return The JPanel object representing the LLM setup panel.
     */
    override fun getPanel(): JPanel {
        val textTitle = JLabel(TestSparkLabelsBundle.defaultValue("llmSetup"))
        textTitle.font = Font("Monochrome", Font.BOLD, 20)

        val titlePanel = JPanel()
        titlePanel.add(textTitle)

        if (isGrazieClassLoaded()) {
            platformSelector.model = DefaultComboBoxModel(arrayOf(TestSparkLabelsBundle.defaultValue("grazie"), TestSparkLabelsBundle.defaultValue("openAI")))
            platformSelector.selectedItem = settingsState.llmPlatform
        } else {
            platformSelector.isEnabled = false
        }

        llmUserTokenField.toolTipText = TestSparkToolTipsBundle.defaultValue("llmToken")
        if (platformSelector.selectedItem!!.toString() == TestSparkLabelsBundle.defaultValue("grazie")) {
            llmUserTokenField.text = settingsState.grazieToken
        } else {
            llmUserTokenField.text = settingsState.openAIToken
        }

        modelSelector.toolTipText = TestSparkToolTipsBundle.defaultValue("model")
        modelSelector.isEnabled = false

        updateModelSelector(
            platformSelector,
            modelSelector,
            llmUserTokenField,
            defaultModulesArray,
        )

        val bottomButtons = JPanel()

        backLlmButton.isOpaque = false
        backLlmButton.isContentAreaFilled = false
        bottomButtons.add(backLlmButton)

        okLlmButton.isOpaque = false
        okLlmButton.isContentAreaFilled = false
        bottomButtons.add(okLlmButton)

        return FormBuilder.createFormBuilder()
            .setFormLeftIndent(10)
            .addVerticalGap(5)
            .addComponent(titlePanel)
            .addLabeledComponent(
                JBLabel(TestSparkLabelsBundle.defaultValue("llmPlatform")),
                platformSelector,
                10,
                false,
            )
            .addLabeledComponent(
                JBLabel(TestSparkLabelsBundle.defaultValue("llmToken")),
                llmUserTokenField,
                10,
                false,
            )
            .addLabeledComponent(
                JBLabel(TestSparkLabelsBundle.defaultValue("model")),
                modelSelector,
                10,
                false,
            )
            .addComponentFillVertically(bottomButtons, 10)
            .panel
    }

    /**
     * Updates the settings state based on the selected values from the UI components.
     *
     * This method sets the `llmPlatform`, `llmUserToken`, and `model` properties of the `settingsState` object
     * based on the currently selected values from the UI components.
     *
     * Note: This method assumes all the required UI components (`platformSelector`, `llmUserTokenField`, and `modelSelector`) are properly initialized and have values selected.
     */
    override fun applyUpdates() {
        settingsState.llmPlatform = platformSelector.selectedItem!!.toString()
        if (platformSelector.selectedItem!!.toString() == TestSparkLabelsBundle.defaultValue("grazie")) {
            settingsState.grazieToken = llmUserTokenField.text
            settingsState.grazieModel = modelSelector.selectedItem!!.toString()
        } else {
            settingsState.openAIToken = llmUserTokenField.text
            settingsState.openAIModel = modelSelector.selectedItem!!.toString()
        }
    }
}
