package org.jetbrains.research.testspark.actions.evosuite

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import org.jetbrains.research.testspark.actions.TestSparkAction
import org.jetbrains.research.testspark.actions.template.ToolPanelFactory
import org.jetbrains.research.testspark.bundles.TestSparkLabelsBundle
import org.jetbrains.research.testspark.bundles.TestSparkToolTipsBundle
import org.jetbrains.research.testspark.data.ContentDigestAlgorithm
import org.jetbrains.research.testspark.data.JUnitVersion
import org.jetbrains.research.testspark.services.SettingsApplicationService
import java.awt.Font
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

class EvoSuitePanelFactory : ToolPanelFactory {
    private val settingsState = SettingsApplicationService.getInstance().state!!

    private var javaPathTextField = JTextField(30)
    private var algorithmSelector = ComboBox(ContentDigestAlgorithm.values())
    private val backEvoSuiteButton = JButton("Back")
    private val okEvoSuiteButton = JButton("OK")

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
    override fun getOkButton() = okEvoSuiteButton

    /**
     * Returns the EvoSuite panel for setting up EvoSuite configurations.
     *
     * @return the JPanel containing the EvoSuite setup GUI components
     */
    override fun getPanel(junit: JUnitVersion?): JPanel {
        val textTitle = JLabel("EvoSuite Setup")
        textTitle.font = Font("Monochrome", Font.BOLD, 20)

        val titlePanel = JPanel()
        titlePanel.add(textTitle)

        val bottomButtons = JPanel()

        backEvoSuiteButton.isOpaque = false
        backEvoSuiteButton.isContentAreaFilled = false
        bottomButtons.add(backEvoSuiteButton)

        okEvoSuiteButton.isOpaque = false
        okEvoSuiteButton.isContentAreaFilled = false
        bottomButtons.add(okEvoSuiteButton)

        algorithmSelector.setMinimumAndPreferredWidth(300)
        algorithmSelector.selectedItem = settingsState.algorithm

        javaPathTextField.toolTipText = TestSparkToolTipsBundle.defaultValue("javaPath")
        javaPathTextField.text = settingsState.javaPath

        return FormBuilder.createFormBuilder()
            .setFormLeftIndent(10)
            .addVerticalGap(5)
            .addComponent(titlePanel)
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
            .addComponentFillVertically(JPanel(), 30)
            .addComponentFillVertically(bottomButtons, 10)
            .panel
    }

    /**
     * Updates the state of the settings.
     */
    override fun settingsStateUpdate() {
        settingsState.javaPath = javaPathTextField.text
        settingsState.algorithm = algorithmSelector.selectedItem!! as ContentDigestAlgorithm
    }
}
