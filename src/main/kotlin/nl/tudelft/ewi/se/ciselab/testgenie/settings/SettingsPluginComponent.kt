package nl.tudelft.ewi.se.ciselab.testgenie.settings

import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import org.jdesktop.swingx.JXTitledSeparator
import java.awt.Dimension
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * This class displays and captures changes to the values of the Settings entries.
 */
class SettingsPluginComponent {
    var panel: JPanel? = null

    // Plugin description
    private val pluginDiscription = JLabel(
        "<html><body>TestGenie is an external graphical IntelliJ plugin that integrates EvoSuite into the IDE." +
            " EvoSuite is a tool that automatically generates test cases with assertions for classes written in Java code." +
            " TestGenie makes this much easier, as it provides an intuitive modern interface for EvoSuite – so, no more CLI."
    )

    // Checkbox options
    private var showCoverageCheckBox = JCheckBox("Show visualised coverage")

    // Environment options (Java path)
    private var javaPathTextField = JTextField()

    init {
        // Apply style to panel (must be first)
        stylizePanel()

        // Create panel
        createSettingsPanel()
    }

    /**
     * Create the main panel for Plugin settings page
     */
    private fun createSettingsPanel() {
        panel = FormBuilder.createFormBuilder()
            // Add description of TestGenie
            .addComponent(pluginDiscription)
            // Add visual coverage checkbox
            .addComponent(showCoverageCheckBox, 10)
            .addComponent(JXTitledSeparator("Environment settings"), 15)
            .addLabeledComponent(JBLabel("Java 11 path:"), javaPathTextField, 10, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    /**
     * Add stylistic additions to elements of Plugin settings panel (e.g. tooltips)
     * IMPORTANT: this is responsible for wrapping the text of a label. It must be created before createSettingsPanel()
     */
    private fun stylizePanel() {

        // Get dimensions of visible rectangle
        val width = panel?.visibleRect?.width
        val height = panel?.visibleRect?.height

        // Set description text to wrap around dimensions
        pluginDiscription.preferredSize = Dimension(width ?: 100, height ?: 100)
    }

    /**
     * Returns the UI component that should be focused when a user opens the TestGenie Settings page.
     *
     * @return preferred UI component
     */
    fun getPreferredFocusedComponent(): JComponent {
        return showCoverageCheckBox
    }

    // Settings "changers"

    var showCoverage: Boolean
        get() = showCoverageCheckBox.isSelected
        set(newStatus) {
            showCoverageCheckBox.isSelected = newStatus
        }

    var javaPath: String
        get() = javaPathTextField.text
        set(newConfig) {
            javaPathTextField.text = newConfig
        }
}
