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
import org.jetbrains.research.testspark.data.ContentDigestAlgorithm
import org.jetbrains.research.testspark.display.TestSparkIcons
import org.jetbrains.research.testspark.services.ActionsStateService
import org.jetbrains.research.testspark.tools.Manager
import org.jetbrains.research.testspark.tools.evosuite.EvoSuite
import org.jetbrains.research.testspark.tools.llm.Llm
import java.awt.CardLayout
import java.awt.Dimension
import java.awt.Font
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
        private val backLlmButton = JButton("Back")
        private val okLlmButton = JButton("OK")

        private var algorithmSelector = ComboBox(ContentDigestAlgorithm.values())
        private var javaPathTextField = JTextField(30)
        private val backEvoSuiteButton = JButton("Back")
        private val okEvoSuiteButton = JButton("OK")

        private val actionsState = ActionsStateService.getInstance().state!!

        private val cardLayout = CardLayout()

        init {
            val panel = JPanel(cardLayout)

            panel.add(getMainPanel(), "1")
            panel.add(getLlmPanel(), "2")
            panel.add(getEvoSuitePanel(), "3")

            addListeners(panel)

            add(panel)

            pack()

            val dimension: Dimension = Toolkit.getDefaultToolkit().screenSize
            val x = (dimension.width - size.width) / 2
            val y = (dimension.height - size.height) / 2
            setLocation(x, y)

            isVisible = true
        }

        /**
         * Returns the main panel for the test generator UI.
         * This panel contains options for selecting the test generator and the code type.
         * It also includes a button for proceeding to the next step.
         *
         * @return the main panel for the test generator UI
         */
        private fun getMainPanel(): JPanel {
            val mainPanel = JPanel()
            mainPanel.setLayout(BoxLayout(mainPanel, BoxLayout.Y_AXIS))

            val panelTitle = JPanel()
            val iconTitle = JLabel(TestSparkIcons.pluginIcon)
            val textTitle = JLabel("Welcome to TestSpark!")
            textTitle.font = Font("Monochrome", Font.BOLD, 20)
            panelTitle.add(iconTitle)
            panelTitle.add(textTitle)
            mainPanel.add(panelTitle)

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
            if (codeTypeButtons.size == 1) codeTypeButtons[0].isSelected = true
            for (button in codeTypeButtons) codesToTestPanel.add(button)
            mainPanel.add(codesToTestPanel)

            val nextButtonPanel = JPanel()
            nextButton.isEnabled = false
            nextButtonPanel.add(nextButton)
            mainPanel.add(nextButtonPanel)

            return mainPanel
        }

        /**
         * Retrieves the LLM panel.
         *
         * @return The JPanel object representing the LLM setup panel.
         */
        private fun getLlmPanel(): JPanel {
            val textTitle = JLabel("LLM Setup")
            textTitle.font = Font("Monochrome", Font.BOLD, 20)

            val titlePanel = JPanel()
            titlePanel.add(textTitle)

            val bottomButtons = JPanel()

            backLlmButton.isOpaque = false
            backLlmButton.isContentAreaFilled = false
            bottomButtons.add(backLlmButton)

            okLlmButton.isOpaque = false
            okLlmButton.isContentAreaFilled = false
            okLlmButton.isEnabled = false
            bottomButtons.add(okLlmButton)

            llmUserTokenField.toolTipText = TestSparkToolTipsBundle.defaultValue("llmToken")
            llmUserTokenField.text = actionsState.llmUserToken

            modelSelector.toolTipText = TestSparkToolTipsBundle.defaultValue("model")
            modelSelector.isEnabled = false
            updateModelSelector()

            if (isGrazieClassLoaded()) {
                platformSelector.model = DefaultComboBoxModel(arrayOf("Grazie", "OpenAI"))
            } else {
                platformSelector.isEnabled = false
            }

            return FormBuilder.createFormBuilder()
                .setFormLeftIndent(10)
                .addVerticalGap(5)
                .addComponent(titlePanel)
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

        /**
         * Returns the EvoSuite panel for setting up EvoSuite configurations.
         *
         * @return the JPanel containing the EvoSuite setup GUI components
         */
        private fun getEvoSuitePanel(): JPanel {
            val textTitle = JLabel("EvoSuite Setup")
            textTitle.font = Font("Monochrome", Font.BOLD, 20)

            val titlePanel = JPanel()
            titlePanel.add(textTitle)

            val bottomButtons = JPanel()

            backEvoSuiteButton.isOpaque = false
            backEvoSuiteButton.isContentAreaFilled = false
            bottomButtons.add(backEvoSuiteButton)

            okEvoSuiteButton.isOpaque = false
            okEvoSuiteButton.isContentAreaFilled = false
            bottomButtons.add(okEvoSuiteButton)

            algorithmSelector.setMinimumAndPreferredWidth(300)
            algorithmSelector.selectedItem = actionsState.algorithm

            javaPathTextField.toolTipText = TestSparkToolTipsBundle.defaultValue("javaPath")
            javaPathTextField.text = actionsState.javaPath

            return FormBuilder.createFormBuilder()
                .setFormLeftIndent(10)
                .addVerticalGap(5)
                .addComponent(titlePanel)
                .addLabeledComponent(
                    JBLabel(TestSparkLabelsBundle.defaultValue("javaPath")),
                    javaPathTextField,
                    10,
                    false,
                )
                .addLabeledComponent(
                    JBLabel(TestSparkLabelsBundle.defaultValue("defaultSearch")),
                    algorithmSelector,
                    10,
                    false,
                )
                .addComponentFillVertically(JPanel(), 30)
                .addComponentFillVertically(bottomButtons, 10)
                .panel
        }

        /**
         * Adds listeners to various components in the given panel.
         *
         * @param panel the JPanel to add listeners to
         */
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
                if (evoSuiteButton.isSelected) {
                    cardLayout.next(panel)
                }
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

            backLlmButton.addActionListener {
                cardLayout.previous(panel)
                pack()
            }

            okLlmButton.addActionListener {
                actionsState.llmPlatform = platformSelector.selectedItem!!.toString()
                actionsState.llmUserToken = llmUserTokenField.text
                actionsState.model = modelSelector.selectedItem!!.toString()
                if (codeTypeButtons[0].isSelected) {
                    Manager.generateTestsForClassByLlm(e)
                } else if (codeTypeButtons[1].isSelected) {
                    Manager.generateTestsForMethodByLlm(e)
                } else if (codeTypeButtons[2].isSelected) Manager.generateTestsForLineByLlm(e)
                dispose()
            }

            javaPathTextField.document.addDocumentListener(object : DocumentListener {
                override fun insertUpdate(e: DocumentEvent?) {
                    updateJavaPath()
                }

                override fun removeUpdate(e: DocumentEvent?) {
                    updateJavaPath()
                }

                override fun changedUpdate(e: DocumentEvent?) {
                    updateJavaPath()
                }

                fun updateJavaPath() { actionsState.javaPath = javaPathTextField.text }
            })

            algorithmSelector.addActionListener {
                actionsState.algorithm = algorithmSelector.selectedItem!! as ContentDigestAlgorithm
            }

            backEvoSuiteButton.addActionListener {
                cardLayout.previous(panel)
                cardLayout.previous(panel)
                pack()
            }

            okEvoSuiteButton.addActionListener {
                if (codeTypeButtons[0].isSelected) {
                    Manager.generateTestsForClassByEvoSuite(e)
                } else if (codeTypeButtons[1].isSelected) {
                    Manager.generateTestsForMethodByEvoSuite(e)
                } else if (codeTypeButtons[2].isSelected) Manager.generateTestsForLineByEvoSuite(e)
                dispose()
            }
        }

        /**
         * Checks if the Grazie class is loaded.
         * @return true if the Grazie class is loaded, false otherwise.
         */
        private fun isGrazieClassLoaded(): Boolean {
            val className = "org.jetbrains.research.grazie.Request"
            return try {
                Class.forName(className)
                true
            } catch (e: ClassNotFoundException) {
                false
            }
        }

        /**
         * Updates the state of the "Next" button based on the selected options.
         * The "Next" button is enabled only if a test generator button (llmButton or evoSuiteButton) and at least one
         * code type button (from codeTypeButtons) are selected.
         *
         * This method should be called whenever*/
        private fun updateNextButton() {
            val isTestGeneratorButtonGroupSelected = llmButton.isSelected || evoSuiteButton.isSelected
            var isCodeTypeButtonGroupSelected = false
            for (button in codeTypeButtons) {
                isCodeTypeButtonGroupSelected = isCodeTypeButtonGroupSelected || button.isSelected
            }
            nextButton.isEnabled = isTestGeneratorButtonGroupSelected && isCodeTypeButtonGroupSelected
        }

        /**
         * Updates the model selector based on the selected platform in the platform selector.
         * If the selected platform is "Grazie", the model selector is disabled and set to display only "GPT-4".
         * If the selected platform is not "Grazie", the model selector is updated with the available modules fetched asynchronously using llmUserTokenField and enables the okLlmButton.
         * If the modules fetch fails, the model selector is set to display the default modules and is disabled.
         *
         * This method runs on a separate thread using ApplicationManager.getApplication().executeOnPooledThread{}.
         */
        private fun updateModelSelector() {
            okLlmButton.isEnabled = false
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
                    okLlmButton.isEnabled = true
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

            if (models.isNotEmpty()) return models.sortedWith(gptComparator).toTypedArray().filter { !it.contains("vision") }.toTypedArray()

            return null
        }
    }
}
