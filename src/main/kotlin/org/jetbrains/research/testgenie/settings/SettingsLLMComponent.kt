package org.jetbrains.research.testgenie.settings

import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import org.jdesktop.swingx.JXTitledSeparator
import org.jetbrains.research.testgenie.TestGenieLabelsBundle
import org.jetbrains.research.testgenie.TestGenieToolTipsBundle
import javax.swing.JPanel
import javax.swing.JTextField

class SettingsLLMComponent {
    var panel: JPanel? = null

    // Grazie Token
    private var grazieUserTokenField = JTextField()

    init {
        // Adds the panel components
        createSettingsPanel()

        // Adds additional style (width, tooltips)
        stylizePanel()
    }

    private fun stylizePanel() {
        grazieUserTokenField.toolTipText = TestGenieToolTipsBundle.defaultValue("grazieToken")
    }

    /**
     * Create the main panel for LLM-related settings page
     */
    private fun createSettingsPanel() {
        panel = FormBuilder.createFormBuilder()
            .addComponent(JXTitledSeparator(TestGenieLabelsBundle.defaultValue("LLMSettings")))
            .addLabeledComponent(
                JBLabel(TestGenieLabelsBundle.defaultValue("grazieToken")),
                grazieUserTokenField,
                10,
                false
            )
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    var grazieUserToken: String
        get() = grazieUserTokenField.text
        set(newText) {
            grazieUserTokenField.text = newText
        }
}
