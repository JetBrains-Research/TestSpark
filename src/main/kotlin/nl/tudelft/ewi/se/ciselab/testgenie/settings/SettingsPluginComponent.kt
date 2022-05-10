package nl.tudelft.ewi.se.ciselab.testgenie.settings

import com.intellij.util.ui.FormBuilder
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * This class displays and captures changes to the values of the Settings entries.
 */
class SettingsPluginComponent {
    var panel: JPanel? = null
    private var showCoverageCheckBox = JCheckBox("Do you want visualised coverage? ")

    init {
        panel = FormBuilder.createFormBuilder()
                .addComponent(showCoverageCheckBox, 10)
                .addComponentFillVertically(JPanel(), 0)
                .panel
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
}