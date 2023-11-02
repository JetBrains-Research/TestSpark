package org.jetbrains.research.testspark.actions

import com.google.gson.JsonParser
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.io.HttpRequests
import com.intellij.util.ui.FormBuilder
import org.jetbrains.research.testspark.TestSparkLabelsBundle
import org.jetbrains.research.testspark.TestSparkToolTipsBundle
import org.jetbrains.research.testspark.tools.Manager
import org.jetbrains.research.testspark.tools.evosuite.EvoSuite
import org.jetbrains.research.testspark.tools.llm.Llm
import java.awt.CardLayout
import java.awt.Dimension
import java.awt.Toolkit
import java.net.HttpURLConnection
import javax.swing.BoxLayout
import javax.swing.ButtonGroup
import javax.swing.DefaultComboBoxModel
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/**
 * Represents an action to be performed in the TestSpark plugin.
 *
 * This class extends the AnAction class and is responsible for handling the action performed event.
 * It creates a dialog wrapper and displays it when the associated action is performed.
 */
class TestSparkAction : AnAction() {
    /**
     * Handles the action performed event.
     *
     * This method is called when the associated action is performed.
     *
     * @param e The AnActionEvent object representing the action event.
     *           It provides information about the event, such as the source of the event and the project context.
     *           This parameter is required.
     */
    override fun actionPerformed(e: AnActionEvent) {
        TestSparkActionWindow(e)
    }

    /**
     * Class representing the TestSparkActionWindow.
     *
     * @property e The AnActionEvent object.
     */
    class TestSparkActionWindow(val e: AnActionEvent) : JFrame("TestSpark") {
        private val llmButton = JRadioButton("<html><b>${Llm().name}</b></html>")
        private val evoSuiteButton = JRadioButton("<html><b>${EvoSuite().name}</b></html>")
        private val testGeneratorButtonGroup = ButtonGroup()
        private val codeTypes = getCurrentListOfCodeTypes(e)
        private val codeTypeButtons: MutableList<JRadioButton> = mutableListOf()
        private val codeTypeButtonGroup = ButtonGroup()

        private val defaultModulesArray = arrayOf("")
        private var modelSelector = ComboBox(defaultModulesArray)
        private var llmUserTokenField = JTextField(30)
        private var platformSelector = ComboBox(arrayOf("OpenAI"))
        private var lastChosenModule = ""

        private val nextButton = JButton("Next")
        private val backButton = JButton("Back")
        private val okButton = JButton("OK")

        private val cardLayout = CardLayout()

        init {
            val panel = JPanel(cardLayout)

            panel.add(getMainPanel(), "1")
            panel.add(getLlmPanel(), "2")

            addListeners(panel)

            add(panel)

            pack()

            val dimension: Dimension = Toolkit.getDefaultToolkit().screenSize
            val x = (dimension.width - size.width) / 2
            val y = (dimension.height - size.height) / 2
            setLocation(x, y)

            isVisible = true
        }

        private fun getMainPanel(): JPanel {
            val mainPanel = JPanel()
            mainPanel.setLayout(BoxLayout(mainPanel, BoxLayout.Y_AXIS))

            testGeneratorButtonGroup.add(llmButton)
            testGeneratorButtonGroup.add(evoSuiteButton)

            val testGeneratorPanel = JPanel()
            testGeneratorPanel.add(JLabel("Select the test generator:"))
            testGeneratorPanel.add(llmButton)
            testGeneratorPanel.add(evoSuiteButton)
            mainPanel.add(testGeneratorPanel)

            for (codeType in codeTypes) {
                val button = JRadioButton(codeType as String)
                codeTypeButtons.add(button)
                codeTypeButtonGroup.add(button)
            }

            val codesToTestPanel = JPanel()
            codesToTestPanel.add(JLabel("Select the code type:"))
            for (button in codeTypeButtons) codesToTestPanel.add(button)
            mainPanel.add(codesToTestPanel)

            nextButton.isEnabled = false
            mainPanel.add(nextButton)

            return mainPanel
        }

        private fun getLlmPanel(): JPanel {
            val bottomButtons = JPanel()
            bottomButtons.add(backButton)
            bottomButtons.add(okButton)

            llmUserTokenField.toolTipText = TestSparkToolTipsBundle.defaultValue("llmToken")
            modelSelector.toolTipText = TestSparkToolTipsBundle.defaultValue("model")
            modelSelector.isEnabled = false

            if (isGrazieClassLoaded()) {
                platformSelector.model = DefaultComboBoxModel(arrayOf("Grazie", "OpenAI"))
            } else {
                platformSelector.isEnabled = false
            }

            return FormBuilder.createFormBuilder()
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
                .addComponentFillVertically(bottomButtons, 10)
                .panel
        }

        private fun addListeners(panel: JPanel) {
            llmButton.addActionListener {
                updateNextButton()
            }
            evoSuiteButton.addActionListener {
                updateNextButton()
            }
            for (button in codeTypeButtons) {
                button.addActionListener { updateNextButton() }
            }
            nextButton.addActionListener {
                cardLayout.next(panel)
                pack()
            }
            llmUserTokenField.document.addDocumentListener(object : DocumentListener {
                override fun insertUpdate(e: DocumentEvent?) {
                    updateModelSelector()
                }

                override fun removeUpdate(e: DocumentEvent?) {
                    updateModelSelector()
                }

                override fun changedUpdate(e: DocumentEvent?) {
                    updateModelSelector()
                }
            })
            platformSelector.addItemListener {
                updateModelSelector()
            }
            backButton.addActionListener {
                cardLayout.previous(panel)
                pack()
            }
            okButton.addActionListener {
                if (llmButton.isSelected) {
                    if (codeTypeButtons[0].isSelected) Manager.generateTestsForClassByLlm(e)
                    if (codeTypeButtons[1].isSelected) Manager.generateTestsForMethodByLlm(e)
                    if (codeTypeButtons[2].isSelected) Manager.generateTestsForLineByLlm(e)
                }
                if (evoSuiteButton.isSelected) {
                    if (codeTypeButtons[0].isSelected) Manager.generateTestsForClassByEvoSuite(e)
                    if (codeTypeButtons[1].isSelected) Manager.generateTestsForMethodByEvoSuite(e)
                    if (codeTypeButtons[2].isSelected) Manager.generateTestsForLineByEvoSuite(e)
                }
                dispose()
            }
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

        private fun updateNextButton() {
            val isTestGeneratorButtonGroupSelected = llmButton.isSelected || evoSuiteButton.isSelected
            var isCodeTypeButtonGroupSelected = false
            for (button in codeTypeButtons) {
                isCodeTypeButtonGroupSelected = isCodeTypeButtonGroupSelected || button.isSelected
            }
            nextButton.isEnabled = isTestGeneratorButtonGroupSelected && isCodeTypeButtonGroupSelected
        }

        private fun updateModelSelector() {
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
    }
}
