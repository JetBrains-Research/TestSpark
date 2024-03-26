package org.jetbrains.research.testspark.settings.llm

import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.EditorTextField
import org.jetbrains.research.testspark.bundles.TestSparkLabelsBundle
import org.jetbrains.research.testspark.data.JsonEncoding
import org.jetbrains.research.testspark.display.TestSparkIcons
import org.jetbrains.research.testspark.display.createButton
import org.jetbrains.research.testspark.services.SettingsApplicationService
import org.jetbrains.research.testspark.settings.SettingsApplicationState
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import kotlin.math.max

/**
 * Creating the part of LLM settings that is responsible for prompt templates is organized
 */
class PromptTemplateFactory(private val promptEditorType: PromptEditorType) {
    private val settingsState: SettingsApplicationState
        get() = SettingsApplicationService.getInstance().state!!

    private var templates = mutableListOf<String>()

    private val previousButton =
        createButton(TestSparkIcons.previous, TestSparkLabelsBundle.defaultValue("previousRequest"))
    private val requestNumber = "%d / %d"
    private val requestLabel = JLabel(requestNumber)
    private val nextButton = createButton(TestSparkIcons.next, TestSparkLabelsBundle.defaultValue("nextRequest"))
    private val removeButton = createButton(TestSparkIcons.remove, TestSparkLabelsBundle.defaultValue("removeTemplate"))
    private val addButton = JButton(TestSparkLabelsBundle.defaultValue("addPromptTemplate"), TestSparkIcons.add)
    private var currentTemplateNumber = 1
    private var allTemplateNumber = 1

    private val editorTextField = EditorTextField()

    init {
        setCommonPrompt(
            when (promptEditorType) {
                PromptEditorType.CLASS -> settingsState.classPrompt
                PromptEditorType.METHOD -> settingsState.methodPrompt
                PromptEditorType.LINE -> settingsState.linePrompt
            },
        )

        editorTextField.setOneLineMode(false)
        ApplicationManager.getApplication().runWriteAction {
            editorTextField.document.setText(templates[currentTemplateNumber - 1])
        }

        update()

        addListeners()
    }

    fun getEditorTextField() = editorTextField

    fun templatesUpdate() {
        templates[currentTemplateNumber - 1] = editorTextField.document.text
    }

    fun getButtonsPanel(): JPanel {
        val panel = JPanel(FlowLayout(FlowLayout.LEFT))
        panel.add(previousButton)
        panel.add(requestLabel)
        panel.add(nextButton)
        panel.add(removeButton)
        panel.add(addButton)
        return panel
    }

    fun getCurrentTemplateNumber() = currentTemplateNumber

    fun setCurrentTemplateNumber(currentTemplateNumber: Int) {
        this.currentTemplateNumber = currentTemplateNumber
        update()
    }

    fun getCommonPrompt(): String = JsonEncoding.encode(templates)

    fun setCommonPrompt(value: String) {
        templates.clear()
        templates = JsonEncoding.decode(value)
        allTemplateNumber = templates.size
        currentTemplateNumber = when (promptEditorType) {
            PromptEditorType.CLASS -> settingsState.currentClassTemplateNumber
            PromptEditorType.METHOD -> settingsState.currentMethodTemplateNumber
            PromptEditorType.LINE -> settingsState.currentLineTemplateNumber
        }
    }

    private fun addListeners() {
        previousButton.addActionListener {
            if (currentTemplateNumber > 1) currentTemplateNumber--
            update()
        }

        nextButton.addActionListener {
            if (currentTemplateNumber < allTemplateNumber) currentTemplateNumber++
            update()
        }

        addButton.addActionListener {
            allTemplateNumber++
            currentTemplateNumber = allTemplateNumber
            ApplicationManager.getApplication().runWriteAction {
                templates.add("")
                editorTextField.document.setText("")
            }
            update()
        }

        removeButton.addActionListener {
            templates.removeAt(currentTemplateNumber - 1)
            currentTemplateNumber = max(1, currentTemplateNumber - 1)
            allTemplateNumber--
            update()
        }
    }

    /**
     * Updates the label displaying the request number information.
     * Uses the requestNumber template to format the label text.
     */
    private fun update() {
        requestLabel.text = String.format(
            requestNumber,
            currentTemplateNumber,
            allTemplateNumber,
        )
        ApplicationManager.getApplication().runWriteAction {
            editorTextField.document.setText(templates[currentTemplateNumber - 1])
        }
        removeButton.isEnabled = allTemplateNumber != 1
    }
}
