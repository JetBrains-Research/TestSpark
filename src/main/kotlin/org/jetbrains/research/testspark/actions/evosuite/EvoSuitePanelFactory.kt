package org.jetbrains.research.testspark.actions.evosuite

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import org.jetbrains.research.testspark.actions.template.PanelFactory
import org.jetbrains.research.testspark.bundles.evosuite.EvoSuiteLabelsBundle
import org.jetbrains.research.testspark.bundles.evosuite.EvoSuiteSettingsBundle
import org.jetbrains.research.testspark.bundles.plugin.PluginLabelsBundle
import org.jetbrains.research.testspark.data.evosuite.ContentDigestAlgorithm
import org.jetbrains.research.testspark.services.EvoSuiteSettingsService
import org.jetbrains.research.testspark.settings.evosuite.EvoSuiteSettingsState
import java.awt.Font
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

class EvoSuitePanelFactory(private val project: Project) : PanelFactory {
    private val evoSuiteSettingsState: EvoSuiteSettingsState
        get() = project.getService(EvoSuiteSettingsService::class.java).state

    private var javaPathTextField = JTextField(30)
    private var algorithmSelector = ComboBox(ContentDigestAlgorithm.values())
    private val backEvoSuiteButton = JButton(PluginLabelsBundle.get("back"))
    private val okEvoSuiteButton = JButton(PluginLabelsBundle.get("ok"))

    /**
     * Returns the title panel for the component.
     *
     * @return the title panel as a JPanel instance.
     */
    override fun getTitlePanel(): JPanel {
        val textTitle = JLabel(PluginLabelsBundle.get("evosuiteSetup"))
        textTitle.font = Font("Monochrome", Font.BOLD, 20)

        val titlePanel = JPanel()
        titlePanel.add(textTitle)

        return titlePanel
    }

    /**
     * Returns the middle panel.
     *
     * @return the middle panel as a JPanel.
     */
    override fun getMiddlePanel(): JPanel {
        javaPathTextField.toolTipText = EvoSuiteSettingsBundle.get("javaPath")
        javaPathTextField.text = evoSuiteSettingsState.javaPath

        algorithmSelector.setMinimumAndPreferredWidth(300)
        algorithmSelector.selectedItem = evoSuiteSettingsState.algorithm

        return FormBuilder.createFormBuilder()
            .setFormLeftIndent(10)
            .addLabeledComponent(
                JBLabel(EvoSuiteSettingsBundle.get("javaPath")),
                javaPathTextField,
                10,
                false,
            )
            .addLabeledComponent(
                JBLabel(EvoSuiteLabelsBundle.get("defaultSearch")),
                algorithmSelector,
                10,
                false,
            )
            .panel
    }

    /**
     * Returns the bottom panel for the current view.
     *
     * @return The bottom panel for the current view.
     */
    override fun getBottomPanel(): JPanel {
        val bottomButtons = JPanel()

        backEvoSuiteButton.isOpaque = false
        backEvoSuiteButton.isContentAreaFilled = false
        bottomButtons.add(backEvoSuiteButton)

        okEvoSuiteButton.isOpaque = false
        okEvoSuiteButton.isContentAreaFilled = false
        bottomButtons.add(okEvoSuiteButton)

        return bottomButtons
    }

    /**
     * Retrieves the back button.
     *
     * @return The back button.
     */
    override fun getBackButton() = backEvoSuiteButton

    /**
     * Retrieves the reference to the "OK" button.
     *
     * @return The reference to the "OK" button.
     */
    override fun getFinishedButton() = okEvoSuiteButton

    /**
     * Updates the state of the settings.
     */
    override fun applyUpdates() {
        evoSuiteSettingsState.javaPath = javaPathTextField.text
        evoSuiteSettingsState.algorithm = algorithmSelector.selectedItem!! as ContentDigestAlgorithm
    }
}
