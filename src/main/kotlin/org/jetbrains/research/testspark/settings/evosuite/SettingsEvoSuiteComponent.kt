package org.jetbrains.research.testspark.settings.evosuite

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import org.jdesktop.swingx.JXTitledSeparator
import org.jetbrains.research.testspark.bundles.evosuite.EvoSuiteLabelsBundle
import org.jetbrains.research.testspark.data.ContentDigestAlgorithm
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
    private var evosuitePortField = JTextField()

    private val evosuiteSetupCheckBox: JCheckBox = JCheckBox(EvoSuiteLabelsBundle.get("evosuiteSetupCheckBox"), true)

    // EvoSuite checkboxes options
    private var sandboxCheckBox = JCheckBox(EvoSuiteLabelsBundle.get("sandbox"))
    private var assertionsCheckBox = JCheckBox(EvoSuiteLabelsBundle.get("assertionCreation"))
    private var clientOnThreadCheckBox = JCheckBox(EvoSuiteLabelsBundle.get("debug"))
    private var junitCheckCheckBox = JCheckBox(EvoSuiteLabelsBundle.get("junitCheck"))
    private var minimizeCheckBox = JCheckBox(EvoSuiteLabelsBundle.get("minimize"))

    // Criterion selection checkboxes
    // There is a limited amount of criteria, but multiple can be selected at once.
    // Effectively, this requires its own section (or a checkboxed combobox of sorts)
    private var criterionSeparator = JXTitledSeparator(EvoSuiteLabelsBundle.get("criterionSeparator"))
    private var criterionLineCheckBox = JCheckBox(EvoSuiteLabelsBundle.get("criterionLine"))
    private var criterionBranchCheckBox = JCheckBox(EvoSuiteLabelsBundle.get("criterionBranch"))
    private var criterionExceptionCheckBox = JCheckBox(EvoSuiteLabelsBundle.get("criterionException"))
    private var criterionWeakMutationCheckBox = JCheckBox(EvoSuiteLabelsBundle.get("criterionWeakMutation"))
    private var criterionOutputCheckBox = JCheckBox(EvoSuiteLabelsBundle.get("criterionOutput"))
    private var criterionMethodCheckBox = JCheckBox(EvoSuiteLabelsBundle.get("criterionMethod"))
    private var criterionMethodNoExceptionCheckBox = JCheckBox(EvoSuiteLabelsBundle.get("criterionMethodNoExc"))
    private var criterionCBranchCheckBox = JCheckBox(EvoSuiteLabelsBundle.get("criterionCBranch"))

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
            .addComponent(JXTitledSeparator(EvoSuiteLabelsBundle.get("javaSettings")))
            .addLabeledComponent(JBLabel(EvoSuiteLabelsBundle.get("javaPath")), javaPathTextField, 10, false)
            .addComponent(JXTitledSeparator(EvoSuiteLabelsBundle.get("generalSettings")))
            // EvoSuite "input" options (e.g. text, number)
            // Important settings like algorithm selection, seed selection
            .addLabeledComponent(JBLabel(EvoSuiteLabelsBundle.get("defaultSearch")), algorithmSelector, 10, false)
            .addLabeledComponent(JBLabel(EvoSuiteLabelsBundle.get("seed")), seedTextField, 10, false)
            .addLabeledComponent(JBLabel(EvoSuiteLabelsBundle.get("port")), evosuitePortField, 10, false)
            .addLabeledComponent(JBLabel(EvoSuiteLabelsBundle.get("configId")), configurationIdTextField, 5, false)
            .addComponent(evosuiteSetupCheckBox, 10)
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
        seedTextField.toolTipText = EvoSuiteLabelsBundle.get("seed")
        evosuitePortField.toolTipText = EvoSuiteLabelsBundle.get("port")
        configurationIdTextField.toolTipText = EvoSuiteLabelsBundle.get("configId")
        clientOnThreadCheckBox.toolTipText = EvoSuiteLabelsBundle.get("debug")
        junitCheckCheckBox.toolTipText = EvoSuiteLabelsBundle.get("junitCheck")
        criterionSeparator.toolTipText = EvoSuiteLabelsBundle.get("assertionCreation")

        javaPathTextField.toolTipText = EvoSuiteLabelsBundle.get("javaPath")
    }

    /**
     * Returns the UI component that should be focused when a user opens the TestSpark Settings page.
     *
     * @return preferred UI component
     */
    fun getPreferredFocusedComponent(): JComponent {
        return algorithmSelector
    }

    var evosuiteSetupCheckBoxSelected: Boolean
        get() = evosuiteSetupCheckBox.isSelected
        set(newStatus) {
            evosuiteSetupCheckBox.isSelected = newStatus
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

    var evosuitePort: String
        get() = evosuitePortField.text
        set(newPort) {
            evosuitePortField.text = newPort
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
