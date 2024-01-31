package org.jetbrains.research.testspark.actions.llm

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import org.jetbrains.research.testspark.actions.template.ToolPanelFactory
import org.jetbrains.research.testspark.bundles.TestSparkDefaultsBundle
import org.jetbrains.research.testspark.bundles.TestSparkLabelsBundle
import org.jetbrains.research.testspark.helpers.addLLMPanelListeners
import org.jetbrains.research.testspark.helpers.getLLLMPlatforms
import org.jetbrains.research.testspark.helpers.stylizeMainComponents
import org.jetbrains.research.testspark.services.SettingsApplicationService
import org.jetbrains.research.testspark.tools.llm.generation.LLMPlatform
import java.awt.Font
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

class LLMPanelFactory : ToolPanelFactory {
    private var modelSelector = ComboBox(arrayOf(""))
    private var llmUserTokenField = JTextField(30)
    private var platformSelector = ComboBox(arrayOf(TestSparkDefaultsBundle.defaultValue("openAI")))
    private val backLlmButton = JButton("Back")
    private val okLlmButton = JButton("OK")

    private val settingsState = SettingsApplicationService.getInstance().state!!

    private val llmPlatforms: List<LLMPlatform> = getLLLMPlatforms()

    init {
        addLLMPanelListeners(
            platformSelector,
            modelSelector,
            llmUserTokenField,
            llmPlatforms,
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
    override fun getOkButton() = okLlmButton

    /**
     * Retrieves the LLM panel.
     *
     * @return The JPanel object representing the LLM setup panel.
     */
    override fun getPanel(): JPanel {
        val textTitle = JLabel("LLM Setup")
        textTitle.font = Font("Monochrome", Font.BOLD, 20)

        val titlePanel = JPanel()
        titlePanel.add(textTitle)

        stylizeMainComponents(platformSelector, modelSelector, llmUserTokenField, llmPlatforms)

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
    override fun settingsStateUpdate() {
        settingsState.currentLLMPlatformName = platformSelector.selectedItem!!.toString()
        for (index in llmPlatforms.indices) {
            settingsState.llmPlatforms[index].token = llmPlatforms[index].token
            settingsState.llmPlatforms[index].model = llmPlatforms[index].model
        }
    }
}
