package org.jetbrains.research.testspark.settings

import com.intellij.openapi.components.service
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import org.jdesktop.swingx.JXTitledSeparator
import org.jetbrains.research.testspark.TestSparkLabelsBundle
import org.jetbrains.research.testspark.TestSparkToolTipsBundle
import org.jetbrains.research.testspark.services.SettingsProjectService
import java.awt.Color
import java.awt.Dimension
import javax.swing.JCheckBox
import javax.swing.JColorChooser
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * This class displays and captures changes to the values of the Settings entries.
 */
class SettingsPluginComponent(project: Project) {
    private val projectDuplicate: Project = project

    var panel: JPanel? = null

    // BuildPath options
    private var buildPathTextField = JTextField()

    // BuildCommand options
    private var buildCommandTextField = JTextField()

    // Telemetry
    private val telemetryDescription = JLabel(
        "<html><body>With your permission, TestSpark will send usage statistics to Intelligent Collaboration tool lab at " +
            "the Jetbrains Research in order to help with the research. This includes " +
            "information about your usage patterns, such as the tests you generate and the way you " +
            "modify them manually before applying them to a test suite.",
    )
    private val showCoverageCheckbox: JCheckBox = JCheckBox(TestSparkLabelsBundle.defaultValue("showCoverage"))
    private val telemetrySeparator = JXTitledSeparator(TestSparkLabelsBundle.defaultValue("telemetry"))
    private var telemetryEnabledCheckbox = JCheckBox(TestSparkLabelsBundle.defaultValue("telemetryEnabled"))
    private val fileChooserDescriptor = FileChooserDescriptor(false, true, false, false, false, false)
    private val textBrowseFolderListener = TextBrowseFolderListener(fileChooserDescriptor)
    private val telemetryPathChooser = TextFieldWithBrowseButton()
    private val telemetryPathLabel = JBLabel(TestSparkLabelsBundle.defaultValue("telemetryPath"))

    // Accessibility options
    private val accessibilitySeparator = JXTitledSeparator(TestSparkLabelsBundle.defaultValue("accessibility"))
    private var colorPicker = JColorChooser()

    init {
        // Apply style to panel (must be first)
        stylizePanel()

        // Create panel
        createSettingsPanel()
        // Create telemetry file chooser field
        telemetryPathField()
    }

    private fun telemetryPathField() {
        // Watch for the checkbox being clicked
        telemetryEnabledCheckbox.addActionListener {
            // If checkbox is clicked, change the path chooser according to the new status
            telemetryPathChooser.isEditable = telemetryEnabledCheckbox.isSelected
            telemetryPathChooser.isEnabled = telemetryEnabledCheckbox.isSelected
        }
        val telemetryEnabled = projectDuplicate.service<SettingsProjectService>().state.telemetryEnabled
        telemetryPathChooser.addBrowseFolderListener(textBrowseFolderListener) // Add the ability to choose folders
        telemetryPathChooser.isEditable = telemetryEnabled
        telemetryPathChooser.isEnabled = telemetryEnabled
    }

    /**
     * Create the main panel for Plugin settings page
     */
    private fun createSettingsPanel() {
        panel = FormBuilder.createFormBuilder()
            .addComponent(JXTitledSeparator(TestSparkLabelsBundle.defaultValue("showCoverageDescription")), 15)
            .addComponent(showCoverageCheckbox, 10)
            .addComponent(JXTitledSeparator(TestSparkLabelsBundle.defaultValue("environmentSettings")), 15)
            // Add buildPath option
            .addLabeledComponent(
                JBLabel(TestSparkLabelsBundle.defaultValue("buildPath")),
                buildPathTextField,
                10,
                false,
            )
            // Add buildPath option
            .addLabeledComponent(
                JBLabel(TestSparkLabelsBundle.defaultValue("buildCommand")),
                buildCommandTextField,
                10,
                false,
            )
            // Add telemetry options
            .addComponent(telemetrySeparator, 15)
            .addComponent(telemetryDescription, 10)
            .addComponent(telemetryEnabledCheckbox, 10)
            .addLabeledComponent(telemetryPathLabel, telemetryPathChooser, 10, false)
            // Add accessibility options
            .addComponent(accessibilitySeparator, 15)
            .addComponent(JBLabel(TestSparkLabelsBundle.defaultValue("colorPicker")), 15)
            .addComponent(colorPicker, 10)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    /**
     * Add stylistic additions to elements of Plugin settings panel (e.g. tooltips)
     * IMPORTANT: this is responsible for wrapping the text of a label. It must be created before createSettingsPanel()
     */
    private fun stylizePanel() {
        // Add description to telemetry path show coverage checkbox
        showCoverageCheckbox.toolTipText = TestSparkToolTipsBundle.defaultValue("showCoverage")

        // Add description to build Path
        buildPathTextField.toolTipText = TestSparkToolTipsBundle.defaultValue("buildPath")

        // Add description to build Command
        buildCommandTextField.toolTipText = TestSparkToolTipsBundle.defaultValue("buildCommand")

        // Add description to telemetry
        telemetryEnabledCheckbox.toolTipText = TestSparkToolTipsBundle.defaultValue("telemetryEnabled")

        // Add description to telemetry path
        telemetryPathLabel.toolTipText = TestSparkToolTipsBundle.defaultValue("telemetryPath")

        // Get dimensions of visible rectangle
        val width = panel?.visibleRect?.width
        val height = panel?.visibleRect?.height

        // Simplify colorPicker
        colorPicker.removeChooserPanel(colorPicker.chooserPanels.component1())
        colorPicker.removeChooserPanel(colorPicker.chooserPanels.component2())
        colorPicker.removeChooserPanel(colorPicker.chooserPanels.component2())
        colorPicker.removeChooserPanel(colorPicker.chooserPanels.component2())
        colorPicker.chooserPanels.component1().isColorTransparencySelectionEnabled = false

        telemetryDescription.preferredSize = Dimension(width ?: 100, height ?: 100)
        telemetryPathChooser.preferredSize = Dimension(width ?: 100, telemetryPathChooser.preferredSize.height)

        // Set colorPicker to wrap around dimensions
        colorPicker.preferredSize = Dimension(width ?: 100, height ?: 400)
    }

    var showCoverageCheckboxSelected: Boolean
        get() = showCoverageCheckbox.isSelected
        set(newStatus) {
            showCoverageCheckbox.isSelected = newStatus
        }

    var buildPath: String
        get() = buildPathTextField.text
        set(newConfig) {
            buildPathTextField.text = newConfig
        }

    var buildCommand: String
        get() = buildCommandTextField.text
        set(newConfig) {
            buildCommandTextField.text = newConfig
        }

    var telemetryEnabled: Boolean
        get() = telemetryEnabledCheckbox.isSelected
        set(newStatus) {
            telemetryEnabledCheckbox.isSelected = newStatus
        }

    var telemetryPath: String
        get() = telemetryPathChooser.text
        set(newPath) {
            telemetryPathChooser.text = newPath
        }

    var colorRed: Int
        get() = colorPicker.color.red
        set(newStatus) {
            colorPicker.color = JBColor(
                TestSparkToolTipsBundle.defaultValue("colorName"),
                Color(newStatus, colorPicker.color.green, colorPicker.color.blue),
            )
        }

    var colorGreen: Int
        get() = colorPicker.color.green
        set(newStatus) {
            colorPicker.color = JBColor(
                TestSparkToolTipsBundle.defaultValue("colorName"),
                Color(colorPicker.color.red, newStatus, colorPicker.color.blue),
            )
        }
    var colorBlue: Int
        get() = colorPicker.color.blue
        set(newStatus) {
            colorPicker.color = JBColor(
                TestSparkToolTipsBundle.defaultValue("colorName"),
                Color(colorPicker.color.red, colorPicker.color.green, newStatus),
            )
        }
}
