package org.jetbrains.research.testspark.settings.llm

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.Messages
import com.intellij.ui.EditorTextField
import com.intellij.ui.JBColor
import org.jetbrains.research.testspark.bundles.TestSparkBundle
import org.jetbrains.research.testspark.bundles.TestSparkLabelsBundle
import org.jetbrains.research.testspark.data.JsonEncoding
import org.jetbrains.research.testspark.display.TestSparkIcons
import org.jetbrains.research.testspark.display.createButton
import org.jetbrains.research.testspark.services.PromptParserService
import org.jetbrains.research.testspark.settings.SettingsApplicationState
import java.awt.FlowLayout
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import kotlin.math.max

/**
 * Creating the part of LLM settings that is responsible for prompt templates is organized
 */
class PromptTemplateFactory(
    private val settingsState: SettingsApplicationState,
    private val promptEditorType: PromptEditorType,
) {
    private var templates = mutableListOf<String>()
    private var names = mutableListOf<String>()

    private var currentDefaultIndex = 0

    private var currentTemplateNumber = 0

    private val promptTemplateName = JTextField()
    private val removeButton = createButton(TestSparkIcons.remove, TestSparkLabelsBundle.defaultValue("removeTemplate"))
    private val setAsDefaultButton =
        JButton(TestSparkLabelsBundle.defaultValue("setAsDefault"), TestSparkIcons.setDefault)

    private val editorTextField = EditorTextField()

    private val defaultSetAsDefaultButtonBorder = setAsDefaultButton.border
    private val defaultPromptTemplateNameBorder = promptTemplateName.border
    private val redBorder = BorderFactory.createLineBorder(JBColor.RED)

    private val previousButton =
        createButton(TestSparkIcons.previous, TestSparkLabelsBundle.defaultValue("previousRequest"))
    private val nextButton = createButton(TestSparkIcons.next, TestSparkLabelsBundle.defaultValue("nextRequest"))
    private val addButton = JButton(TestSparkLabelsBundle.defaultValue("addPromptTemplate"), TestSparkIcons.add)

    init {
        setSettingsStateParameters()

        setEditorTextField()

        update()

        addListeners()
    }

    fun getUpperButtonsPanel(): JPanel {
        val panel = JPanel(FlowLayout(FlowLayout.LEFT))
        panel.add(JLabel(TestSparkLabelsBundle.defaultValue("promptTemplateName")))
        panel.add(promptTemplateName)
        panel.add(setAsDefaultButton)
        panel.add(removeButton)

        return panel
    }

    fun getEditorTextField() = editorTextField

    fun getLowerButtonsPanel(): JPanel {
        val panel = JPanel(FlowLayout(FlowLayout.LEFT))
        panel.add(previousButton)
        panel.add(nextButton)
        panel.add(addButton)

        return panel
    }

    fun getCurrentDefaultPromptIndex() = currentDefaultIndex

    fun setCurrentDefaultPromptIndex(value: Int) {
        currentDefaultIndex = value
        currentTemplateNumber = currentDefaultIndex
        update()
    }

    fun getCommonPrompt(): String = JsonEncoding.encode(templates)

    fun setCommonPrompt(value: String) {
        templates.clear()
        templates = JsonEncoding.decode(value)
    }

    fun getCommonName(): String {
        for (i in names.indices) {
            if (names[i].isBlank()) {
                names[i] = getDefaultPromptTemplateName(i + 1)
            }
        }
        return JsonEncoding.encode(names)
    }

    fun setCommonName(value: String) {
        names.clear()
        names = JsonEncoding.decode(value)
    }

    private fun setSettingsStateParameters() {
        templates.clear()
        templates = when (promptEditorType) {
            PromptEditorType.CLASS -> JsonEncoding.decode(settingsState.classPrompts)
            PromptEditorType.METHOD -> JsonEncoding.decode(settingsState.methodPrompts)
            PromptEditorType.LINE -> JsonEncoding.decode(settingsState.linePrompts)
        }
        names = when (promptEditorType) {
            PromptEditorType.CLASS -> JsonEncoding.decode(settingsState.classPromptNames)
            PromptEditorType.METHOD -> JsonEncoding.decode(settingsState.methodPromptNames)
            PromptEditorType.LINE -> JsonEncoding.decode(settingsState.linePromptNames)
        }
        currentDefaultIndex = when (promptEditorType) {
            PromptEditorType.CLASS -> settingsState.classCurrentDefaultPromptIndex
            PromptEditorType.METHOD -> settingsState.methodCurrentDefaultPromptIndex
            PromptEditorType.LINE -> settingsState.lineCurrentDefaultPromptIndex
        }
        currentTemplateNumber = currentDefaultIndex
    }

    private fun setEditorTextField() {
        editorTextField.setOneLineMode(false)
        ApplicationManager.getApplication().runWriteAction {
            editorTextField.document.setText(templates[currentTemplateNumber])
        }
    }

    private fun getDefaultPromptTemplateName(number: Int) =
        TestSparkLabelsBundle.defaultValue("defaultPromptTemplateName") + number.toString()

    private fun addListeners() {
        promptTemplateName.document.addDocumentListener(
            object : DocumentListener {
                private fun update() {
                    if (promptTemplateName.text.isBlank()) {
                        promptTemplateName.border = redBorder
                    } else {
                        promptTemplateName.border = defaultPromptTemplateNameBorder
                    }
                    names[currentTemplateNumber] = promptTemplateName.text
                }

                override fun insertUpdate(e: DocumentEvent) {
                    this.update()
                }

                override fun removeUpdate(e: DocumentEvent) {
                    this.update()
                }

                override fun changedUpdate(e: DocumentEvent) {
                    this.update()
                }
            },
        )

        setAsDefaultButton.addActionListener {
            currentDefaultIndex = currentTemplateNumber
            updateSetAsDefaultButton()
        }

        removeButton.addActionListener {
            if (currentDefaultIndex == currentTemplateNumber) {
                Messages.showErrorDialog(
                    TestSparkBundle.message("removeTemplateMessage"),
                    TestSparkBundle.message("removeTemplateTitle"),
                )
            } else {
                if (currentTemplateNumber < currentDefaultIndex) currentDefaultIndex--
                templates.removeAt(currentTemplateNumber)
                names.removeAt(currentTemplateNumber)
                currentTemplateNumber = max(0, currentTemplateNumber - 1)
                update()
            }
        }

        editorTextField.document.addDocumentListener(
            object : com.intellij.openapi.editor.event.DocumentListener {
                override fun documentChanged(event: com.intellij.openapi.editor.event.DocumentEvent) {
                    updateTemplate()
                    updateSetAsDefaultButton()
                    updateEditorTextField()
                }
            },
        )

        previousButton.addActionListener {
            if (currentTemplateNumber > 0) currentTemplateNumber--
            update()
        }

        nextButton.addActionListener {
            if (currentTemplateNumber < templates.size - 1) currentTemplateNumber++
            update()
        }

        addButton.addActionListener {
            currentTemplateNumber = templates.size
            templates.add("")
            names.add(getDefaultPromptTemplateName(currentTemplateNumber + 1))
            update()
        }
    }

    private fun updateTemplate() {
        templates[currentTemplateNumber] = editorTextField.document.text
    }

    private fun updateSetAsDefaultButton() {
        setAsDefaultButton.isEnabled = (currentDefaultIndex != currentTemplateNumber)

        if (!service<PromptParserService>().isPromptValid(editorTextField.document.text)) {
            setAsDefaultButton.border = redBorder
            setAsDefaultButton.isEnabled = false
        } else {
            setAsDefaultButton.border = defaultSetAsDefaultButtonBorder
        }
    }

    private fun updateEditorTextField() {
        service<PromptParserService>().highlighter(editorTextField, editorTextField.document.text)
        if (!service<PromptParserService>().isPromptValid(editorTextField.document.text)) {
            editorTextField.border = redBorder
        } else {
            editorTextField.border = null
        }
    }

    /**
     * Updates the label displaying the request number information.
     * Uses the requestNumber template to format the label text.
     */
    private fun update() {
        ApplicationManager.getApplication().runWriteAction {
            editorTextField.document.setText(templates[currentTemplateNumber])
        }
        removeButton.isEnabled = templates.size != 1

        promptTemplateName.text = names[currentTemplateNumber]

        updateSetAsDefaultButton()
        updateEditorTextField()
    }
}
