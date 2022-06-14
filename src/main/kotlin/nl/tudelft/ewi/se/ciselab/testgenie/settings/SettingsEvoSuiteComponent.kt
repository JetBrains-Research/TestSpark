package nl.tudelft.ewi.se.ciselab.testgenie.settings

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import nl.tudelft.ewi.se.ciselab.testgenie.TestGenieLabelsBundle
import nl.tudelft.ewi.se.ciselab.testgenie.TestGenieToolTipsBundle
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
    private var sandboxCheckBox = JCheckBox(TestGenieLabelsBundle.defaultValue("sandbox"))
    private var assertionsCheckBox = JCheckBox(TestGenieLabelsBundle.defaultValue("assertionCreation"))
    private var clientOnThreadCheckBox = JCheckBox(TestGenieLabelsBundle.defaultValue("debug"))
    private var junitCheckCheckBox = JCheckBox(TestGenieLabelsBundle.defaultValue("junitCheck"))
    private var minimizeCheckBox = JCheckBox(TestGenieLabelsBundle.defaultValue("minimize"))

    // Criterion selection checkboxes
    // There is a limited amount of criteria, but multiple can be selected at once.
    // Effectively, this requires its own section (or a checkboxed combobox of sorts)
    private var criterionSeparator = JXTitledSeparator(TestGenieLabelsBundle.defaultValue("criterionSeparator"))
    private var criterionLineCheckBox = JCheckBox(TestGenieLabelsBundle.defaultValue("criterionLine"))
    private var criterionBranchCheckBox = JCheckBox(TestGenieLabelsBundle.defaultValue("criterionBranch"))
    private var criterionExceptionCheckBox = JCheckBox(TestGenieLabelsBundle.defaultValue("criterionException"))
    private var criterionWeakMutationCheckBox = JCheckBox(TestGenieLabelsBundle.defaultValue("criterionWeakMutation"))
    private var criterionOutputCheckBox = JCheckBox(TestGenieLabelsBundle.defaultValue("criterionOutput"))
    private var criterionMethodCheckBox = JCheckBox(TestGenieLabelsBundle.defaultValue("criterionMethod"))
    private var criterionMethodNoExceptionCheckBox = JCheckBox(TestGenieLabelsBundle.defaultValue("criterionMethodNoExc"))
    private var criterionCBranchCheckBox = JCheckBox(TestGenieLabelsBundle.defaultValue("criterionCBranch"))

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
            .addComponent(JXTitledSeparator(TestGenieLabelsBundle.defaultValue("generalSettings")))
            // EvoSuite "input" options (e.g. text, number)
            // Important settings like algorithm selection, seed selection
            .addLabeledComponent(JBLabel(TestGenieLabelsBundle.defaultValue("defaultSearch")), algorithmSelector, 10, false)
            .addLabeledComponent(JBLabel(TestGenieLabelsBundle.defaultValue("seed")), seedTextField, 10, false)
            .addLabeledComponent(JBLabel(TestGenieLabelsBundle.defaultValue("configId")), configurationIdTextField, 5, false)
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
        seedTextField.toolTipText = TestGenieToolTipsBundle.defaultValue("seed")
        configurationIdTextField.toolTipText = TestGenieToolTipsBundle.defaultValue("configId")
        clientOnThreadCheckBox.toolTipText = TestGenieToolTipsBundle.defaultValue("debug")
        junitCheckCheckBox.toolTipText = TestGenieToolTipsBundle.defaultValue("junit")
        criterionSeparator.toolTipText = TestGenieToolTipsBundle.defaultValue("criterion")
    }

    /**
     * Returns the UI component that should be focused when a user opens the TestGenie Settings page.
     *
     * @return preferred UI component
     */
    fun getPreferredFocusedComponent(): JComponent {
        return algorithmSelector
    }

    // Settings "changers"

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
