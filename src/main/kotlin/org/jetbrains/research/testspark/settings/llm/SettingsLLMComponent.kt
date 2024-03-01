package org.jetbrains.research.testspark.settings.llm

import com.intellij.ide.ui.UINumericRange
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.EditorTextField
import com.intellij.ui.JBColor
import com.intellij.ui.JBIntSpinner
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTabbedPane
import com.intellij.util.ui.FormBuilder
import org.jdesktop.swingx.JXTitledSeparator
import org.jetbrains.research.testspark.bundles.TestSparkDefaultsBundle
import org.jetbrains.research.testspark.bundles.TestSparkLabelsBundle
import org.jetbrains.research.testspark.bundles.TestSparkToolTipsBundle
import org.jetbrains.research.testspark.helpers.addLLMPanelListeners
import org.jetbrains.research.testspark.helpers.getLLLMPlatforms
import org.jetbrains.research.testspark.helpers.stylizeMainComponents
import org.jetbrains.research.testspark.services.PromptParserService
import org.jetbrains.research.testspark.settings.SettingsApplicationState
import org.jetbrains.research.testspark.tools.llm.generation.LLMPlatform
import java.awt.FlowLayout
import java.awt.Font
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JSeparator
import javax.swing.JTextField
import org.jetbrains.research.testspark.display.TestSparkIcons
import org.jetbrains.research.testspark.display.createButton
import org.jetbrains.research.testspark.tools.llm.SettingsArguments

class SettingsLLMComponent {
    private val settingsState: SettingsApplicationState = SettingsArguments.settingsState!!

    var panel: JPanel? = null

    // LLM Token
    private var llmUserTokenField = JTextField(30)

    // Models
    private var modelSelector = ComboBox(arrayOf(""))
    private var platformSelector = ComboBox(arrayOf(TestSparkDefaultsBundle.defaultValue("openAI")))

    // Default LLM Requests
    private var defaultLLMRequestsSeparator = JXTitledSeparator(TestSparkLabelsBundle.defaultValue("defaultLLMRequestsSeparator"))
    private val defaultLLMRequestsFormBuilder = FormBuilder.createFormBuilder()
    private var defaultLLMRequestsPanel = defaultLLMRequestsFormBuilder.panel
    private val defaultLLMRequestPanels = mutableListOf<JPanel>()
    private val removeButtons = mutableListOf<JButton>()
    private val textFields = mutableListOf<JTextField>()

    // Prompt Editor
    private var promptSeparator = JXTitledSeparator(TestSparkLabelsBundle.defaultValue("PromptSeparator"))
    private var promptEditorTabbedPane = createTabbedPane()

    // Maximum number of LLM requests
    private var maxLLMRequestsField = JBIntSpinner(UINumericRange(settingsState.maxLLMRequest, 1, 20))

    // The depth of input parameters used in class under tests
    private var maxInputParamsDepthField = JBIntSpinner(UINumericRange(settingsState.maxInputParamsDepth, 1, 5))

    // Maximum polymorphism depth
    private var maxPolyDepthField = JBIntSpinner(UINumericRange(settingsState.maxPolyDepth, 1, 5))

    val llmPlatforms: List<LLMPlatform> = getLLLMPlatforms()

    var currentLLMPlatformName: String
        get() = platformSelector.item
        set(newAlg) {
            platformSelector.item = newAlg
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

    var classPrompt: String
        get() = getEditorTextField(PromptEditorType.CLASS).document.text
        set(value) {
            ApplicationManager.getApplication().runWriteAction {
                val editorTextField =
                    getEditorTextField(PromptEditorType.CLASS)
                editorTextField.document.setText(value)
            }
        }

    var methodPrompt: String
        get() = getEditorTextField(PromptEditorType.METHOD).document.text
        set(value) {
            ApplicationManager.getApplication().runWriteAction {
                val editorTextField =
                    getEditorTextField(PromptEditorType.METHOD)
                editorTextField.document.setText(value)
            }
        }

    var linePrompt: String
        get() = getEditorTextField(PromptEditorType.LINE).document.text
        set(value) {
            ApplicationManager.getApplication().runWriteAction {
                val editorTextField =
                    getEditorTextField(PromptEditorType.LINE)
                editorTextField.document.setText(value)
            }
        }

    init {
        // Adds additional style (width, tooltips)
        stylizeMainComponents(platformSelector, modelSelector, llmUserTokenField, llmPlatforms)
        stylizePanel()

        fillDefaultLLMRequestsPanel()

        // Adds the panel components
        createSettingsPanel()

        // Adds listeners
        addListeners()
    }

    fun updateHighlighting(prompt: String, editorType: PromptEditorType) {
        val editorTextField = getEditorTextField(editorType)
        service<PromptParserService>().highlighter(editorTextField, prompt)
        if (!service<PromptParserService>().isPromptValid(prompt)) {
            val border = BorderFactory.createLineBorder(JBColor.RED)
            editorTextField.border = border
        } else {
            editorTextField.border = null
        }
    }

    private fun createTabbedPane(): JBTabbedPane {
        val tabbedPane = JBTabbedPane()

        // Add tabs for each testing level
        addPromptEditorTab(tabbedPane, PromptEditorType.CLASS)
        addPromptEditorTab(tabbedPane, PromptEditorType.METHOD)
        addPromptEditorTab(tabbedPane, PromptEditorType.LINE)

        return tabbedPane
    }

    private fun addPromptEditorTab(tabbedPane: JBTabbedPane, promptEditorType: PromptEditorType) {
        // initiate the panel
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        // Add editor text field (the prompt editor) to the panel
        val editorTextField = EditorTextField()
        editorTextField.setOneLineMode(false)
        panel.add(editorTextField)
        panel.add(JSeparator())
        // add buttons for inserting keywords to the prompt editor
        addPromptButtons(panel)
        // add the panel as a new tab
        tabbedPane.addTab(promptEditorType.text, panel)
    }

    private fun addPromptButtons(panel: JPanel) {
        val keywords = service<PromptParserService>().getKeywords()
        val editorTextField = panel.getComponent(0) as EditorTextField
        keywords.forEach {
            val btnPanel = JPanel(FlowLayout(FlowLayout.LEFT))

            val button = JButton("\$${it.text}")
            button.setForeground(JBColor.ORANGE)
            button.font = Font("Monochrome", Font.BOLD, 12)

            // add actionListener for button
            button.addActionListener { _ ->
                val editor = editorTextField.editor

                editor?.let { e ->
                    val offset = e.caretModel.offset
                    val document = editorTextField.document
                    WriteCommandAction.runWriteCommandAction(e.project) {
                        document.insertString(offset, "\$${it.text}")
                    }
                }
            }

            // add button and it's description to buttons panel
            btnPanel.add(button)
            btnPanel.add(JBLabel("${it.description} - ${if (it.mandatory) "mandatory" else "optional"}"))

            panel.add(btnPanel)
        }
    }

    private fun fillDefaultLLMRequestsPanel() {
        val defaultLLMRequestsList = settingsState.defaultLLMRequests.split("\n")
        for (defaultLLMRequest in defaultLLMRequestsList) {
            val defaultLLMRequestPanel = JPanel(FlowLayout(FlowLayout.LEFT))

            val textField = JTextField(defaultLLMRequest)
            textField.columns = 30
            defaultLLMRequestPanel.add(textField)
            textFields.add(textField)

            val removeButton = createButton(TestSparkIcons.remove, TestSparkLabelsBundle.defaultValue("removeRequest"))
            defaultLLMRequestPanel.add(removeButton)
            removeButtons.add(removeButton)

            defaultLLMRequestsFormBuilder.addComponent(defaultLLMRequestPanel, 10)
            defaultLLMRequestPanels.add(defaultLLMRequestPanel)
        }
        defaultLLMRequestsPanel = defaultLLMRequestsFormBuilder.panel
        defaultLLMRequestsPanel.revalidate()
    }

    /**
     * Adds listeners to the document of the user token field.
     * These listeners will update the model selector based on the text entered the user token field.
     */
    private fun addListeners() {
        addLLMPanelListeners(
            platformSelector,
            modelSelector,
            llmUserTokenField,
            llmPlatforms,
        )

        for (index in removeButtons.indices) {
            removeButtons[index].addActionListener {
                defaultLLMRequestsPanel.remove(defaultLLMRequestPanels[index])
                defaultLLMRequestsPanel.revalidate()
            }
        }

        addHighlighterListeners()
    }

    private fun addHighlighterListeners() {
        PromptEditorType.values().forEach {
            getEditorTextField(it).document.addDocumentListener(
                object : com.intellij.openapi.editor.event.DocumentListener {
                    override fun documentChanged(event: com.intellij.openapi.editor.event.DocumentEvent) {
                        updateHighlighting(event.document.text, it)
                    }
                },
            )
        }
    }

    private fun stylizePanel() {
        maxLLMRequestsField.toolTipText = TestSparkToolTipsBundle.defaultValue("maximumNumberOfRequests")
        maxInputParamsDepthField.toolTipText = TestSparkToolTipsBundle.defaultValue("parametersDepth")
        maxPolyDepthField.toolTipText = TestSparkToolTipsBundle.defaultValue("maximumPolyDepth")
        promptSeparator.toolTipText = TestSparkToolTipsBundle.defaultValue("promptEditor")
    }

    /**
     * Create the main panel for LLM-related settings page
     */
    private fun createSettingsPanel() {
        panel = FormBuilder.createFormBuilder()
            .addComponent(JXTitledSeparator(TestSparkLabelsBundle.defaultValue("LLMSettings")))
            .addLabeledComponent(
                JBLabel(TestSparkLabelsBundle.defaultValue("llmPlatform")),
                platformSelector,
                10,
                false,
            )
            .addLabeledComponent(
                JBLabel(TestSparkLabelsBundle.defaultValue("llmToken")),
                llmUserTokenField,
                10,
                false,
            )
            .addLabeledComponent(
                JBLabel(TestSparkLabelsBundle.defaultValue("model")),
                modelSelector,
                10,
                false,
            )
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
            .addComponent(defaultLLMRequestsSeparator, 15)
            .addComponent(defaultLLMRequestsPanel, 15)
            .addComponent(promptSeparator, 15)
            .addComponent(promptEditorTabbedPane, 15)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    private fun getEditorTextField(editorType: PromptEditorType): EditorTextField {
        return (promptEditorTabbedPane.getComponentAt(editorType.index) as JPanel).getComponent(0) as EditorTextField
    }

    fun updateTokenAndModel() {
        for (llmPlatform in llmPlatforms) {
            if (currentLLMPlatformName == llmPlatform.name) {
                llmUserTokenField.text = llmPlatform.token
                if (modelSelector.isEnabled) modelSelector.selectedItem = llmPlatform.model
            }
        }
    }
}
