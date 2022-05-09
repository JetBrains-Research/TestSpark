package nl.tudelft.ewi.se.ciselab.testgenie.settings

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
class TestGenieSettingsComponent {
    var panel: JPanel? = null
    private var showCoverageCheckBox = JCheckBox("Do you want visualised coverage? ")
    private var sandboxCheckBox = JCheckBox("Execute tests in a sandbox environment")
    private var assertionsCheckBox = JCheckBox("Create assertions")
    private var seedTextField = JTextField()
    //DropDown menu
    private var algorithmSelector = ComboBox(ContentDigestAlgorithm.values())
    private var configurationIdTextField = JTextField()
    private var clientOnThreadCheckBox = JCheckBox("Client on thread")
    private var junitCheckCheckBox = JCheckBox("Compile and run suite")
    //There is a limited amount of criteria, but multiple can be selected at once.
    //Effectively, this requires its own section (or a checkboxed combobox of sorts)
    private var criterionSeparator = JXTitledSeparator("Criterion selection")
    private var criterionLineCheckBox = JCheckBox("Line coverage")
    private var criterionBranchCheckBox = JCheckBox("Branch coverage")
    private var criterionExceptionCheckBox = JCheckBox("Exception coverage")
    private var criterionWeakMutationCheckBox = JCheckBox("Mutation coverage")
    private var criterionOutputCheckBox = JCheckBox("Output coverage")
    private var criterionMethodCheckBox = JCheckBox("Method coverage")
    private var criterionMethodNoExceptionCheckBox = JCheckBox("Method no exception coverage")
    private var criterionCBranchCheckBox = JCheckBox("CBranch coverage")
    private var minimizeCheckBox = JCheckBox("Minimize test suite after generation")

    init {
        panel = FormBuilder.createFormBuilder()
            .addComponent(JXTitledSeparator("General settings"))
            .addComponent(showCoverageCheckBox, 10)
            .addComponent(sandboxCheckBox, 10)
            .addComponent(assertionsCheckBox, 10)
            .addLabeledComponent(JBLabel("Seed(random if left empty) "), seedTextField, 10, false)
            .addLabeledComponent(JBLabel("Select search algorithm"), algorithmSelector, 10, false)
            .addLabeledComponent(JBLabel("Select configuration id (null if left empty) "), configurationIdTextField, 5, false)
            .addComponent(clientOnThreadCheckBox, 10)
            .addComponent(minimizeCheckBox, 10)
            .addComponent(junitCheckCheckBox, 10)
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

        algorithmSelector.setMinimumAndPreferredWidth(300)
        configurationIdTextField.toolTipText = "Label that identifies the used configuration of EvoSuite. This is only done when running experiments."
        clientOnThreadCheckBox.toolTipText = "Run client process on same JVM of master in separate thread. To be used only for debugging purposes"
        junitCheckCheckBox.toolTipText = "Compile and run resulting JUnit test suite (if any was created)"
        criterionSeparator.toolTipText = "Coverage criterion. Can define more than one criterion by checking multiple checkboxes. " +
                "\n By default, all are used."
    }

    /**
     * Returns the UI component that should be focused when a user opens the TestGenie Settings page.
     *
     * @return preferred UI component
     */
    fun getPreferredFocusedComponent(): JComponent {
        return showCoverageCheckBox
    }


    var showCoverage: Boolean
        get() = showCoverageCheckBox.isSelected
        set(newStatus) {
            showCoverageCheckBox.isSelected = newStatus
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

    var junitCheck : Boolean
        get() = junitCheckCheckBox.isSelected
        set(newStatus) {
            junitCheckCheckBox.isSelected = newStatus
        }

    var criterionLine : Boolean
        get() = criterionLineCheckBox.isSelected
        set(newStatus) {
            criterionLineCheckBox.isSelected = newStatus
        }

    var criterionBranch : Boolean
        get() = criterionBranchCheckBox.isSelected
        set(newStatus) {
            criterionBranchCheckBox.isSelected = newStatus
        }

    var criterionException : Boolean
        get() = criterionExceptionCheckBox.isSelected
        set(newStatus) {
            criterionExceptionCheckBox.isSelected = newStatus
        }

    var criterionWeakMutation : Boolean
        get() = criterionWeakMutationCheckBox.isSelected
        set(newStatus) {
            criterionWeakMutationCheckBox.isSelected = newStatus
        }

    var criterionOutput : Boolean
        get() = criterionOutputCheckBox.isSelected
        set(newStatus) {
            criterionOutputCheckBox.isSelected = newStatus
        }

    var criterionMethod : Boolean
        get() = criterionMethodCheckBox.isSelected
        set(newStatus) {
            criterionMethodCheckBox.isSelected = newStatus
        }

    var criterionMethodNoException : Boolean
        get() = criterionMethodNoExceptionCheckBox.isSelected
        set(newStatus) {
            criterionMethodNoExceptionCheckBox.isSelected = newStatus
        }

    var criterionCBranch : Boolean
        get() = criterionCBranchCheckBox.isSelected
        set(newStatus) {
            criterionCBranchCheckBox.isSelected = newStatus
        }

    var minimize : Boolean
        get() = minimizeCheckBox.isSelected
        set(newStatus) {
            minimizeCheckBox.isSelected = newStatus
        }
}