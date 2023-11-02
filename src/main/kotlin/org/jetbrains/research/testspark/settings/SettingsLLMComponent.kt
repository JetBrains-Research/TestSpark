package org.jetbrains.research.testspark.settings

import com.google.gson.JsonParser
import com.intellij.ide.ui.UINumericRange
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.EditorTextField
import com.intellij.ui.JBIntSpinner
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTabbedPane
import com.intellij.util.io.HttpRequests
import com.intellij.util.ui.FormBuilder
import org.jdesktop.swingx.JXTitledSeparator
import org.jetbrains.research.testspark.TestSparkLabelsBundle
import org.jetbrains.research.testspark.TestSparkToolTipsBundle
import org.jetbrains.research.testspark.services.PromptParserService
import java.net.HttpURLConnection
import javax.swing.DefaultComboBoxModel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class SettingsLLMComponent {
    var panel: JPanel? = null

    // LLM Token
    private var llmUserTokenField = JTextField(30)

    // Models
    private val defaultModulesArray = arrayOf("")
    private var modelSelector = ComboBox(defaultModulesArray)
    private var platformSelector = ComboBox(arrayOf("OpenAI"))

    // Prompt Editor
    private var promptSeparator = JXTitledSeparator(TestSparkLabelsBundle.defaultValue("PromptSeparator"))
    private var promptEditorTabbedPane = creatTabbedPane()



    private var lastChosenModule = ""

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

        // Adds listeners
        addListeners()
    }

    fun update() {
        if (platformSelector.selectedItem!!.toString() == "Grazie") {
            modelSelector.model = DefaultComboBoxModel(arrayOf("GPT-4"))
            modelSelector.isEnabled = false
            return
        }
        ApplicationManager.getApplication().executeOnPooledThread {
            val modules = getModules(llmUserTokenField.text)
            modelSelector.removeAllItems()
            if (modules != null) {
                modelSelector.model = DefaultComboBoxModel(modules)
                modelSelector.isEnabled = true
            } else {
                modelSelector.model = DefaultComboBoxModel(defaultModulesArray)
                modelSelector.isEnabled = false
            }
        }
    }

    fun updateHighlighting(){
        println("CHANGED") // ToDO
    }

    private fun creatTabbedPane(): JBTabbedPane{
        val tabbedPane = JBTabbedPane()

        //Add Class Tab
        val editorTextField = EditorTextField()
        editorTextField.setOneLineMode(false);

        tabbedPane.addTab("Class", editorTextField)
        //Add Method Tab
        val label2 = JBLabel("This is Tab 2")
        tabbedPane.addTab("Method", label2)

        //Add Line Tab
        val label3 = JBLabel("This is Tab 2")
        tabbedPane.addTab("Line", label3)

        return tabbedPane
    }

    /**
     * Adds listeners to the document of the user token field.
     * These listeners will update the model selector based on the text entered the user token field.
     */
    private fun addListeners() {
        llmUserTokenField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) {
                update()
            }

            override fun removeUpdate(e: DocumentEvent?) {
                update()
            }

            override fun changedUpdate(e: DocumentEvent?) {
                update()
            }
        })
        platformSelector.addItemListener { update() }

        (promptEditorTabbedPane.getComponent(0) as EditorTextField).document.addDocumentListener(object : com.intellij.openapi.editor.event.DocumentListener {
            override fun documentChanged(event: com.intellij.openapi.editor.event.DocumentEvent) {
                updateHighlighting()
            }
        }
        )
    }

    /**
     * Retrieves all available models from the OpenAI API using the provided token.
     *
     * @param token Authorization token for the OpenAI API.
     * @return An array of model names if request is successful, otherwise null.
     */
    private fun getModules(token: String): Array<String>? {
        val url = "https://api.openai.com/v1/models"

        val httpRequest = HttpRequests.request(url).tuner {
            it.setRequestProperty("Authorization", "Bearer $token")
        }

        val models = mutableListOf<String>()

        try {
            httpRequest.connect {
                if ((it.connection as HttpURLConnection).responseCode == HttpURLConnection.HTTP_OK) {
                    val jsonObject = JsonParser.parseString(it.readString()).asJsonObject
                    val dataArray = jsonObject.getAsJsonArray("data")
                    for (dataObject in dataArray) {
                        val id = dataObject.asJsonObject.getAsJsonPrimitive("id").asString
                        models.add(id)
                    }
                }
            }
        } catch (e: HttpRequests.HttpStatusException) {
            return null
        }

        val gptComparator = Comparator<String> { s1, s2 ->
            when {
                s1 == lastChosenModule -> -1
                s2 == lastChosenModule -> 1
                s1.contains("gpt") && s2.contains("gpt") -> s2.compareTo(s1)
                s1.contains("gpt") -> -1
                s2.contains("gpt") -> 1
                else -> s1.compareTo(s2)
            }
        }

        if (models.isNotEmpty()) return models.sortedWith(gptComparator).toTypedArray()

        return null
    }

    private fun stylizePanel() {
        llmUserTokenField.toolTipText = TestSparkToolTipsBundle.defaultValue("llmToken")
        maxLLMRequestsField.toolTipText = TestSparkToolTipsBundle.defaultValue("maximumNumberOfRequests")
        modelSelector.toolTipText = TestSparkToolTipsBundle.defaultValue("model")
        modelSelector.isEnabled = false
        maxInputParamsDepthField.toolTipText = TestSparkToolTipsBundle.defaultValue("parametersDepth")
        maxPolyDepthField.toolTipText = TestSparkToolTipsBundle.defaultValue("maximumPolyDepth")
        promptSeparator.toolTipText = TestSparkToolTipsBundle.defaultValue("promptEditor")
    }

    /**
     * Create the main panel for LLM-related settings page
     */
    private fun createSettingsPanel() {
        // Check if the Grazie platform access is available in the current build
        if (isGrazieClassLoaded()) {
            platformSelector.model = DefaultComboBoxModel(arrayOf("Grazie", "OpenAI"))
        } else
            platformSelector.isEnabled = false

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
            .addComponent(promptSeparator, 15)
            .addComponent(promptEditorTabbedPane, 15)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    private fun isGrazieClassLoaded(): Boolean {
        val className = "org.jetbrains.research.grazie.Request"
        return try {
            Class.forName(className)
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    var llmUserToken: String
        get() = llmUserTokenField.text
        set(newText) {
            llmUserTokenField.text = newText
        }

    var model: String
        get() = modelSelector.item
        set(newAlg) {
            lastChosenModule = newAlg
            modelSelector.item = newAlg
        }

    var llmPlatform: String
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
        get() = (promptEditorTabbedPane.getComponentAt(0) as EditorTextField).document.text
        set(value) {
            val editorTextField = (promptEditorTabbedPane.getComponentAt(0) as EditorTextField)
            service<PromptParserService>().highlighter(editorTextField, value)
//            promptEditorTabbedPane.setComponentAt(0, service<PromptParserService>().highlighter(editorTextField, value))
//            (promptEditorTabbedPane.getComponentAt(0) as EditorTextField).text = value
        }
}
