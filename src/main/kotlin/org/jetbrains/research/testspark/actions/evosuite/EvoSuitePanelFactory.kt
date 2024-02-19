package org.jetbrains.research.testspark.actions.evosuite

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import org.jetbrains.research.testspark.actions.template.PanelFactory
import org.jetbrains.research.testspark.bundles.TestSparkLabelsBundle
import org.jetbrains.research.testspark.bundles.TestSparkToolTipsBundle
import org.jetbrains.research.testspark.data.ContentDigestAlgorithm
import org.jetbrains.research.testspark.services.SettingsApplicationService
import java.awt.Font
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

class EvoSuitePanelFactory : PanelFactory {
    private val settingsState = SettingsApplicationService.getInstance().state!!

    private var javaPathTextField = JTextField(30)
    private var algorithmSelector = ComboBox(ContentDigestAlgorithm.values())
    private val backEvoSuiteButton = JButton(TestSparkLabelsBundle.defaultValue("back"))
    private val okEvoSuiteButton = JButton(TestSparkLabelsBundle.defaultValue("ok"))

    /**
     * Returns the title panel for the component.
     *
     * @return the title panel as a JPanel instance.
     */
    override fun getTitlePanel(): JPanel {
        val textTitle = JLabel(TestSparkLabelsBundle.defaultValue("evosuiteSetup"))
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
        javaPathTextField.toolTipText = TestSparkToolTipsBundle.defaultValue("javaPath")
        javaPathTextField.text = settingsState.javaPath

        algorithmSelector.setMinimumAndPreferredWidth(300)
        algorithmSelector.selectedItem = settingsState.algorithm

        return FormBuilder.createFormBuilder()
            .setFormLeftIndent(10)
            .addLabeledComponent(
                JBLabel(TestSparkLabelsBundle.defaultValue("javaPath")),
                javaPathTextField,
                10,
                false,
            )
            .addLabeledComponent(
                JBLabel(TestSparkLabelsBundle.defaultValue("defaultSearch")),
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
        settingsState.javaPath = javaPathTextField.text
        settingsState.algorithm = algorithmSelector.selectedItem!! as ContentDigestAlgorithm
    }
}
