package org.jetbrains.research.testspark.settings.llm

import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.EditorTextField
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.jetbrains.research.testspark.bundles.TestSparkLabelsBundle
import org.jetbrains.research.testspark.display.TestSparkIcons
import org.jetbrains.research.testspark.display.createButton
import org.jetbrains.research.testspark.services.SettingsApplicationService
import org.jetbrains.research.testspark.settings.SettingsApplicationState
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel

class PromptTemplateFactory(private val promptEditorType: PromptEditorType) {
    private val settingsState: SettingsApplicationState
        get() = SettingsApplicationService.getInstance().state!!

    private var templates = mutableListOf<String>()

    private val previousButton =
        createButton(TestSparkIcons.previous, TestSparkLabelsBundle.defaultValue("previousRequest"))
    private val requestNumber = "%d / %d"
    private val requestLabel = JLabel(requestNumber)
    private val nextButton = createButton(TestSparkIcons.next, TestSparkLabelsBundle.defaultValue("nextRequest"))
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

        updateRequestLabel()

        addListeners()
    }

    fun getEditorTextField() = editorTextField

    fun getButtonsPanel(): JPanel {
        val panel = JPanel(FlowLayout(FlowLayout.LEFT))
        panel.add(previousButton)
        panel.add(requestLabel)
        panel.add(nextButton)
        panel.add(addButton)
        return panel
    }

    fun getCurrentTemplateNumber() = currentTemplateNumber

    fun setCurrentTemplateNumber(currentTemplateNumber: Int) {
        this.currentTemplateNumber = currentTemplateNumber
    }

    fun getCommonPrompt(): String {
        var jsonString = Json.encodeToString(
            ListSerializer(String.serializer()),
            templates,
        )
        val replacements = mapOf(
            "\\n" to "\n",
            "\\t" to "\t",
            "\\r" to "\r",
            "\\\\" to "\\",
            "\\\"" to "\"",
            "\\'" to "\'",
            "\\b" to "\b",
            "\\f" to "\u000c",
        )
        replacements.forEach { (key, value) ->
            jsonString = jsonString.replace(key, value)
        }
        return jsonString
    }

    fun setCommonPrompt(value: String) {
        templates.clear()
        templates = Json.decodeFromString(ListSerializer(String.serializer()), value) as MutableList<String>
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
            ApplicationManager.getApplication().runWriteAction {
                editorTextField.document.setText(templates[currentTemplateNumber - 1])
            }
            updateRequestLabel()
        }

        nextButton.addActionListener {
            if (currentTemplateNumber < allTemplateNumber) currentTemplateNumber++
            ApplicationManager.getApplication().runWriteAction {
                editorTextField.document.setText(templates[currentTemplateNumber - 1])
            }
            updateRequestLabel()
        }

        addButton.addActionListener {
            allTemplateNumber++
            currentTemplateNumber = allTemplateNumber
            ApplicationManager.getApplication().runWriteAction {
                templates.add("")
                editorTextField.document.setText("")
            }
            updateRequestLabel()
        }
    }

    /**
     * Updates the label displaying the request number information.
     * Uses the requestNumber template to format the label text.
     */
    private fun updateRequestLabel() {
        requestLabel.text = String.format(
            requestNumber,
            currentTemplateNumber,
            allTemplateNumber,
        )
    }
}
