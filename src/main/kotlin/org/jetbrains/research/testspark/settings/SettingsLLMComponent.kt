package org.jetbrains.research.testspark.settings

import com.intellij.ide.ui.UINumericRange
import com.intellij.ui.JBIntSpinner
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import org.jdesktop.swingx.JXTitledSeparator
import org.jetbrains.research.testspark.TestSparkLabelsBundle
import org.jetbrains.research.testspark.TestSparkToolTipsBundle
import javax.swing.JPanel

class SettingsLLMComponent {
    var panel: JPanel? = null

    // Maximum number of LLM requests
    private var maxLLMRequestsField =
        JBIntSpinner(UINumericRange(SettingsApplicationState.DefaultSettingsApplicationState.maxLLMRequest, 1, 20))

    // The depth of input parameters used in class under tests
    private var maxInputParamsDepthField =
        JBIntSpinner(UINumericRange(SettingsApplicationState.DefaultSettingsApplicationState.maxInputParamsDepth, 1, 5))

    // Maximum polymorphism depth
    private var maxPolyDepthField =
        JBIntSpinner(UINumericRange(SettingsApplicationState.DefaultSettingsApplicationState.maxPolyDepth, 1, 5))

    init {
        // Adds the panel components
        createSettingsPanel()

        // Adds additional style (width, tooltips)
        stylizePanel()
    }

    private fun stylizePanel() {
        maxLLMRequestsField.toolTipText = TestSparkToolTipsBundle.defaultValue("maximumNumberOfRequests")
        maxInputParamsDepthField.toolTipText = TestSparkToolTipsBundle.defaultValue("parametersDepth")
        maxPolyDepthField.toolTipText = TestSparkToolTipsBundle.defaultValue("maximumPolyDepth")
    }

    /**
     * Create the main panel for LLM-related settings page
     */
    private fun createSettingsPanel() {
        panel = FormBuilder.createFormBuilder()
            .addComponent(JXTitledSeparator(TestSparkLabelsBundle.defaultValue("LLMSettings")))
            .addLabeledComponent(
                JBLabel(TestSparkLabelsBundle.defaultValue("parametersDepth")),
                maxInputParamsDepthField,
                10,
                false,
            )
            .addLabeledComponent(
                JBLabel(TestSparkLabelsBundle.defaultValue("maximumPolyDepth")),
                maxPolyDepthField,
                10,
                false,
            )
            .addLabeledComponent(
                JBLabel(TestSparkLabelsBundle.defaultValue("maximumNumberOfRequests")),
                maxLLMRequestsField,
                10,
                false,
            )
            .addComponentFillVertically(JPanel(), 0)
            .panel
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
