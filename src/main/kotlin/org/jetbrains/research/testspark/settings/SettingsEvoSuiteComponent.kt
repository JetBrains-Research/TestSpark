package org.jetbrains.research.testspark.settings

import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import org.jdesktop.swingx.JXTitledSeparator
import org.jetbrains.research.testspark.TestSparkLabelsBundle
import org.jetbrains.research.testspark.TestSparkToolTipsBundle
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * This class displays and captures changes to the values of the Settings entries.
 */
class SettingsEvoSuiteComponent {
    var panel: JPanel? = null

    // EvoSuite "input" options (e.g. text, number)
    private var configurationIdTextField = JTextField()
    private var seedTextField = JTextField()

    // EvoSuite checkboxes options
    private var sandboxCheckBox = JCheckBox(TestSparkLabelsBundle.defaultValue("sandbox"))
    private var assertionsCheckBox = JCheckBox(TestSparkLabelsBundle.defaultValue("assertionCreation"))
    private var clientOnThreadCheckBox = JCheckBox(TestSparkLabelsBundle.defaultValue("debug"))
    private var junitCheckCheckBox = JCheckBox(TestSparkLabelsBundle.defaultValue("junitCheck"))
    private var minimizeCheckBox = JCheckBox(TestSparkLabelsBundle.defaultValue("minimize"))

    // Criterion selection checkboxes
    // There is a limited amount of criteria, but multiple can be selected at once.
    // Effectively, this requires its own section (or a checkboxed combobox of sorts)
    private var criterionSeparator = JXTitledSeparator(TestSparkLabelsBundle.defaultValue("criterionSeparator"))
    private var criterionLineCheckBox = JCheckBox(TestSparkLabelsBundle.defaultValue("criterionLine"))
    private var criterionBranchCheckBox = JCheckBox(TestSparkLabelsBundle.defaultValue("criterionBranch"))
    private var criterionExceptionCheckBox = JCheckBox(TestSparkLabelsBundle.defaultValue("criterionException"))
    private var criterionWeakMutationCheckBox = JCheckBox(TestSparkLabelsBundle.defaultValue("criterionWeakMutation"))
    private var criterionOutputCheckBox = JCheckBox(TestSparkLabelsBundle.defaultValue("criterionOutput"))
    private var criterionMethodCheckBox = JCheckBox(TestSparkLabelsBundle.defaultValue("criterionMethod"))
    private var criterionMethodNoExceptionCheckBox = JCheckBox(TestSparkLabelsBundle.defaultValue("criterionMethodNoExc"))
    private var criterionCBranchCheckBox = JCheckBox(TestSparkLabelsBundle.defaultValue("criterionCBranch"))

    init {

        // Adds the panel components
        createSettingsPanel()

        // Adds additional style (width, tooltips)
        stylizePanel()
    }

    /**
     * Create the main panel for EvoSuite settings page
     */
    private fun createSettingsPanel() {
        panel = FormBuilder.createFormBuilder()
            .addComponent(JXTitledSeparator(TestSparkLabelsBundle.defaultValue("generalSettings")))
            // EvoSuite "input" options (e.g. text, number)
            // Important settings like algorithm selection, seed selection
            .addLabeledComponent(JBLabel(TestSparkLabelsBundle.defaultValue("seed")), seedTextField, 10, false)
            .addLabeledComponent(JBLabel(TestSparkLabelsBundle.defaultValue("configId")), configurationIdTextField, 5, false)
            // Checkboxes settings
            .addComponent(sandboxCheckBox, 10)
            .addComponent(assertionsCheckBox, 10)
            .addComponent(clientOnThreadCheckBox, 10)
            .addComponent(minimizeCheckBox, 10)
            .addComponent(junitCheckCheckBox, 10)
            // Criterion selection checkboxes
            .addComponent(criterionSeparator, 15)
            .addComponent(criterionLineCheckBox, 5)
            .addComponent(criterionBranchCheckBox, 5)
            .addComponent(criterionExceptionCheckBox, 5)
            .addComponent(criterionWeakMutationCheckBox, 5)
            .addComponent(criterionOutputCheckBox, 5)
            .addComponent(criterionMethodCheckBox, 5)
            .addComponent(criterionMethodNoExceptionCheckBox, 5)
            .addComponent(criterionCBranchCheckBox, 5)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    /**
     * Add stylistic additions to elements of EvoSuite settings panel (e.g. tooltips)
     */
    private fun stylizePanel() {
        // Tooltips
        seedTextField.toolTipText = TestSparkToolTipsBundle.defaultValue("seed")
        configurationIdTextField.toolTipText = TestSparkToolTipsBundle.defaultValue("configId")
        clientOnThreadCheckBox.toolTipText = TestSparkToolTipsBundle.defaultValue("debug")
        junitCheckCheckBox.toolTipText = TestSparkToolTipsBundle.defaultValue("junit")
        criterionSeparator.toolTipText = TestSparkToolTipsBundle.defaultValue("criterion")
    }

    var sandbox: Boolean
        get() = sandboxCheckBox.isSelected
        set(newStatus) {
            sandboxCheckBox.isSelected = newStatus
        }

    var assertions: Boolean
        get() = assertionsCheckBox.isSelected
        set(newStatus) {
            assertionsCheckBox.isSelected = newStatus
        }

    var seed: String
        get() = seedTextField.text
        set(newText) {
            seedTextField.text = newText
        }

    var configurationId: String
        get() = configurationIdTextField.text
        set(newConfig) {
            configurationIdTextField.text = newConfig
        }

    var clientOnThread: Boolean
        get() = clientOnThreadCheckBox.isSelected
        set(newStatus) {
            clientOnThreadCheckBox.isSelected = newStatus
        }

    var junitCheck: Boolean
        get() = junitCheckCheckBox.isSelected
        set(newStatus) {
            junitCheckCheckBox.isSelected = newStatus
        }

    var criterionLine: Boolean
        get() = criterionLineCheckBox.isSelected
        set(newStatus) {
            criterionLineCheckBox.isSelected = newStatus
        }

    var criterionBranch: Boolean
        get() = criterionBranchCheckBox.isSelected
        set(newStatus) {
            criterionBranchCheckBox.isSelected = newStatus
        }

    var criterionException: Boolean
        get() = criterionExceptionCheckBox.isSelected
        set(newStatus) {
            criterionExceptionCheckBox.isSelected = newStatus
        }

    var criterionWeakMutation: Boolean
        get() = criterionWeakMutationCheckBox.isSelected
        set(newStatus) {
            criterionWeakMutationCheckBox.isSelected = newStatus
        }

    var criterionOutput: Boolean
        get() = criterionOutputCheckBox.isSelected
        set(newStatus) {
            criterionOutputCheckBox.isSelected = newStatus
        }

    var criterionMethod: Boolean
        get() = criterionMethodCheckBox.isSelected
        set(newStatus) {
            criterionMethodCheckBox.isSelected = newStatus
        }

    var criterionMethodNoException: Boolean
        get() = criterionMethodNoExceptionCheckBox.isSelected
        set(newStatus) {
            criterionMethodNoExceptionCheckBox.isSelected = newStatus
        }

    var criterionCBranch: Boolean
        get() = criterionCBranchCheckBox.isSelected
        set(newStatus) {
            criterionCBranchCheckBox.isSelected = newStatus
        }

    var minimize: Boolean
        get() = minimizeCheckBox.isSelected
        set(newStatus) {
            minimizeCheckBox.isSelected = newStatus
        }
}
