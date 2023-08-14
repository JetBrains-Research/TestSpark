package org.jetbrains.research.testspark.settings

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import org.jdesktop.swingx.JXTitledSeparator
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * This class displays and captures changes to the values of the Settings entries.
 */
class SettingsEvoSuiteComponent {
    var panel: JPanel? = null

    // EvoSuite "input" options (e.g. text, number)
    private var algorithmSelector = ComboBox(ContentDigestAlgorithm.values())
    private var configurationIdTextField = JTextField()
    private var seedTextField = JTextField()

    // EvoSuite checkboxes options
    private var sandboxCheckBox = JCheckBox(org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("sandbox"))
    private var assertionsCheckBox = JCheckBox(org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("assertionCreation"))
    private var clientOnThreadCheckBox = JCheckBox(org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("debug"))
    private var junitCheckCheckBox = JCheckBox(org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("junitCheck"))
    private var minimizeCheckBox = JCheckBox(org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("minimize"))

    // Criterion selection checkboxes
    // There is a limited amount of criteria, but multiple can be selected at once.
    // Effectively, this requires its own section (or a checkboxed combobox of sorts)
    private var criterionSeparator = JXTitledSeparator(org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("criterionSeparator"))
    private var criterionLineCheckBox = JCheckBox(org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("criterionLine"))
    private var criterionBranchCheckBox = JCheckBox(org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("criterionBranch"))
    private var criterionExceptionCheckBox = JCheckBox(org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("criterionException"))
    private var criterionWeakMutationCheckBox = JCheckBox(org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("criterionWeakMutation"))
    private var criterionOutputCheckBox = JCheckBox(org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("criterionOutput"))
    private var criterionMethodCheckBox = JCheckBox(org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("criterionMethod"))
    private var criterionMethodNoExceptionCheckBox = JCheckBox(org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("criterionMethodNoExc"))
    private var criterionCBranchCheckBox = JCheckBox(org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("criterionCBranch"))

    // Java path
    private var javaPathTextField = JTextField()

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
            .addComponent(JXTitledSeparator(org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("javaSettings")))
            .addLabeledComponent(JBLabel(org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("javaPath")), javaPathTextField, 10, false)
            .addComponent(JXTitledSeparator(org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("generalSettings")))
            // EvoSuite "input" options (e.g. text, number)
            // Important settings like algorithm selection, seed selection
            .addLabeledComponent(JBLabel(org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("defaultSearch")), algorithmSelector, 10, false)
            .addLabeledComponent(JBLabel(org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("seed")), seedTextField, 10, false)
            .addLabeledComponent(JBLabel(org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("configId")), configurationIdTextField, 5, false)
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
        // Dimensions adjustments
        algorithmSelector.setMinimumAndPreferredWidth(300)

        // Tooltips
        seedTextField.toolTipText = org.jetbrains.research.testspark.TestSparkToolTipsBundle.defaultValue("seed")
        configurationIdTextField.toolTipText = org.jetbrains.research.testspark.TestSparkToolTipsBundle.defaultValue("configId")
        clientOnThreadCheckBox.toolTipText = org.jetbrains.research.testspark.TestSparkToolTipsBundle.defaultValue("debug")
        junitCheckCheckBox.toolTipText = org.jetbrains.research.testspark.TestSparkToolTipsBundle.defaultValue("junit")
        criterionSeparator.toolTipText = org.jetbrains.research.testspark.TestSparkToolTipsBundle.defaultValue("criterion")

        javaPathTextField.toolTipText = org.jetbrains.research.testspark.TestSparkToolTipsBundle.defaultValue("javaPath")
    }

    /**
     * Returns the UI component that should be focused when a user opens the TestSpark Settings page.
     *
     * @return preferred UI component
     */
    fun getPreferredFocusedComponent(): JComponent {
        return algorithmSelector
    }

    // Settings "changers"

    var javaPath: String
        get() = javaPathTextField.text
        set(newConfig) {
            javaPathTextField.text = newConfig
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

    var algorithm: ContentDigestAlgorithm
        get() = algorithmSelector.item
        set(newAlg) {
            algorithmSelector.item = newAlg
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
