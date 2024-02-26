package org.jetbrains.research.testspark.actions.llm

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import org.jetbrains.research.testspark.actions.template.PanelFactory
import org.jetbrains.research.testspark.bundles.TestSparkDefaultsBundle
import org.jetbrains.research.testspark.bundles.TestSparkLabelsBundle
import org.jetbrains.research.testspark.data.JUnitVersion
import org.jetbrains.research.testspark.display.JUnitCombobox
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

class LLMSetupPanelFactory : PanelFactory {
    private val defaultModulesArray = arrayOf("")
    private var modelSelector = ComboBox(defaultModulesArray)
    private var llmUserTokenField = JTextField(30)
    private var platformSelector = ComboBox(arrayOf(TestSparkDefaultsBundle.defaultValue("openAI")))
    private val backLlmButton = JButton(TestSparkLabelsBundle.defaultValue("back"))
    private val okLlmButton = JButton(TestSparkLabelsBundle.defaultValue("next"))
    private val junitSelector = JUnitCombobox()

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
     * Returns the title panel for the setup.
     *
     * @return the title panel containing the setup title label.
     */
    override fun getTitlePanel(): JPanel {
        val textTitle = JLabel(TestSparkLabelsBundle.defaultValue("llmSetup"))
        textTitle.font = Font("Monochrome", Font.BOLD, 20)

        val titlePanel = JPanel()
        titlePanel.add(textTitle)

        return titlePanel
    }

    /**
     * Retrieves the middle panel of the UI.
     *
     * This method returns a JPanel object that represents the middle panel of the user interface.
     * The middle panel contains several components including a platform selector, a model selector,
     * and a user token field. These components are stylized using the `stylizeMainComponents` method.
     * The UI labels for the platform, token, and model components are retrieved using the
     * `TestSpark*/
    override fun getMiddlePanel(junit: JUnitVersion?): JPanel {
        stylizeMainComponents(platformSelector, modelSelector, llmUserTokenField, llmPlatforms)

        junitSelector.detected = junit

        return FormBuilder.createFormBuilder()
            .setFormLeftIndent(10)
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
            .addLabeledComponent(
                JBLabel(TestSparkLabelsBundle.defaultValue("junitVersion")),
                junitSelector,
                10,
                false,
            )
            .panel
    }

    /**
     * Returns the bottom panel for the UI.
     *
     * @return The JPanel representing the bottom panel of the UI.
     */
    override fun getBottomPanel(): JPanel {
        val bottomPanel = JPanel()

        backLlmButton.isOpaque = false
        backLlmButton.isContentAreaFilled = false
        bottomPanel.add(backLlmButton)

        okLlmButton.isOpaque = false
        okLlmButton.isContentAreaFilled = false
        if (!SettingsApplicationService.getInstance().state!!.provideTestSamplesCheckBoxSelected) {
            okLlmButton.text = TestSparkLabelsBundle.defaultValue("ok")
        }
        bottomPanel.add(okLlmButton)

        return bottomPanel
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
     * Updates the settings state based on the selected values from the UI components.
     *
     * This method sets the `llmPlatform`, `llmUserToken`, and `model` properties of the `settingsState` object
     * based on the currently selected values from the UI components.
     *
     * Note: This method assumes all the required UI components (`platformSelector`, `llmUserTokenField`, and `modelSelector`) are properly initialized and have values selected.
     */
    override fun applyUpdates() {
        settingsState.currentLLMPlatformName = platformSelector.selectedItem!!.toString()
        for (index in llmPlatforms.indices) {
            if (llmPlatforms[index].name == TestSparkDefaultsBundle.defaultValue("openAI")) {
                settingsState.openAIToken = llmPlatforms[index].token
                settingsState.openAIModel = llmPlatforms[index].model
            }
            if (llmPlatforms[index].name == TestSparkDefaultsBundle.defaultValue("grazie")) {
                settingsState.grazieToken = llmPlatforms[index].token
                settingsState.grazieModel = llmPlatforms[index].model
            }
        }
        settingsState.junitVersion = (junitSelector.selectedItem!! as JUnitVersion)
    }
}
