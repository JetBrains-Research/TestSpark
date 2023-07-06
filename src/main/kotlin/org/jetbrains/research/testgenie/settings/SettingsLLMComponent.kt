package org.jetbrains.research.testgenie.settings

import com.intellij.ide.ui.UINumericRange
import com.intellij.ui.JBIntSpinner
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
    private var grazieUserTokenField = JTextField(30)
    // Maximum number of LLM requests
    private var maxLLMRequestsField = JBIntSpinner(UINumericRange(SettingsApplicationState.DefaultSettingsApplicationState.maxLLMRequest, 1, 20))
    // The depth of input parameters used in class under tests
    private var maxInputParamsDepthField = JBIntSpinner(UINumericRange(SettingsApplicationState.DefaultSettingsApplicationState.maxInputParamsDepth, 1, 5))
    // Maximum polymorphism depth
    private var maxPolyDepthField = JBIntSpinner(UINumericRange(SettingsApplicationState.DefaultSettingsApplicationState.maxPolyDepth, 1, 5))

    init {
        // Adds the panel components
        createSettingsPanel()

        // Adds additional style (width, tooltips)
        stylizePanel()
    }

    private fun stylizePanel() {
        grazieUserTokenField.toolTipText = TestGenieToolTipsBundle.defaultValue("grazieToken")
        maxLLMRequestsField.toolTipText = TestGenieToolTipsBundle.defaultValue("maximumNumberOfRequests")
        maxInputParamsDepthField.toolTipText = TestGenieToolTipsBundle.defaultValue("parametersDepth")
        maxPolyDepthField.toolTipText = TestGenieToolTipsBundle.defaultValue("maximumPolyDepth")
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
            .addLabeledComponent(
                JBLabel(TestGenieLabelsBundle.defaultValue("parametersDepth")),
                maxInputParamsDepthField,
                10,
                false
            )
            .addLabeledComponent(
                JBLabel(TestGenieLabelsBundle.defaultValue("maximumPolyDepth")),
                maxPolyDepthField,
                10,
                false
            )
            .addLabeledComponent(
                JBLabel(TestGenieLabelsBundle.defaultValue("maximumNumberOfRequests")),
                maxLLMRequestsField,
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

    var maxLLMRequest: Int
        get() = maxLLMRequestsField.number
        set(value) {
            maxLLMRequestsField.number = value
        }

    var maxInputParamsDepth: Int
        get() = maxInputParamsDepthField.number
        set(value) {
            maxInputParamsDepthField.number = value
        }

    var maxPolyDepth: Int
        get() = maxPolyDepthField.number
        set(value) {
            maxPolyDepthField.number = value
        }
}
