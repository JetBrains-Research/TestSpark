package org.jetbrains.research.testspark.actions.evosuite

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import org.jetbrains.research.testspark.actions.template.PanelFactory
import org.jetbrains.research.testspark.bundles.TestSparkLabelsBundle
import org.jetbrains.research.testspark.bundles.TestSparkToolTipsBundle
import org.jetbrains.research.testspark.data.ContentDigestAlgorithm
import org.jetbrains.research.testspark.services.SettingsApplicationService
import org.jetbrains.research.testspark.settings.SettingsApplicationState
import java.awt.Font
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

class EvoSuitePanelFactory(private val project: Project) : PanelFactory {
    private val settingsState: SettingsApplicationState
        get() = project.getService(SettingsApplicationService::class.java).state

    // init components
    private var javaPathTextField = JTextField(30)
    private var algorithmSelector = ComboBox(ContentDigestAlgorithm.entries.toTypedArray())
    private val backEvoSuiteButton = JButton(TestSparkLabelsBundle.defaultValue("back"))
    private val okEvoSuiteButton = JButton(TestSparkLabelsBundle.defaultValue("ok"))

    override fun getTitlePanel(): JPanel {
        val textTitle = JLabel(TestSparkLabelsBundle.defaultValue("evosuiteSetup"))
        textTitle.font = Font("Monochrome", Font.BOLD, 20)

        val titlePanel = JPanel()
        titlePanel.add(textTitle)

        return titlePanel
    }

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

    override fun getBackButton() = backEvoSuiteButton

    override fun getFinishedButton() = okEvoSuiteButton

    override fun applyUpdates() {
        settingsState.javaPath = javaPathTextField.text
        settingsState.algorithm = algorithmSelector.selectedItem!! as ContentDigestAlgorithm
    }
}
