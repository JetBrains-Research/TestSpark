package org.jetbrains.research.testspark.display

import com.intellij.lang.Language
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.diff.DiffColors
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.JBColor
import com.intellij.ui.LanguageTextField
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import org.jetbrains.research.testspark.bundles.plugin.PluginLabelsBundle
import org.jetbrains.research.testspark.bundles.plugin.PluginMessagesBundle
import org.jetbrains.research.testspark.core.data.Report
import org.jetbrains.research.testspark.core.data.TestCase
import org.jetbrains.research.testspark.core.generation.llm.getClassWithTestCaseName
import org.jetbrains.research.testspark.core.progress.CustomProgressIndicator
import org.jetbrains.research.testspark.core.test.data.TestSuiteGeneratedByLLM
import org.jetbrains.research.testspark.data.UIContext
import org.jetbrains.research.testspark.data.llm.JsonEncoding
import org.jetbrains.research.testspark.display.custom.IJProgressIndicator
import org.jetbrains.research.testspark.helpers.JavaClassBuilderHelper
import org.jetbrains.research.testspark.helpers.LLMHelper
import org.jetbrains.research.testspark.helpers.ReportHelper
import org.jetbrains.research.testspark.services.LLMSettingsService
import org.jetbrains.research.testspark.services.TestCaseDisplayService
import org.jetbrains.research.testspark.services.TestsExecutionResultService
import org.jetbrains.research.testspark.settings.llm.LLMSettingsState
import org.jetbrains.research.testspark.tools.TestProcessor
import org.jetbrains.research.testspark.tools.ToolUtils
import org.jetbrains.research.testspark.tools.llm.test.JUnitTestSuitePresenter
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection
import java.util.Queue
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.ScrollPaneConstants
import javax.swing.SwingUtilities
import javax.swing.border.Border
import javax.swing.border.MatteBorder

class TestCasePanelFactory(
    private val project: Project,
    private val testCase: TestCase,
    editor: Editor,
    private val checkbox: JCheckBox,
    val uiContext: UIContext?,
    val report: Report,
) {
    private val llmSettingsState: LLMSettingsState
        get() = project.getService(LLMSettingsService::class.java).state

    private val panel = JPanel()
    private val previousButton =
        IconButtonCreator.getButton(TestSparkIcons.previous, PluginLabelsBundle.get("previousRequest"))
    private var requestNumber: String = "%d / %d"
    private var requestLabel: JLabel = JLabel(requestNumber)
    private val nextButton = IconButtonCreator.getButton(TestSparkIcons.next, PluginLabelsBundle.get("nextRequest"))
    private val errorLabel = JLabel(TestSparkIcons.showError)
    private val copyButton = IconButtonCreator.getButton(TestSparkIcons.copy, PluginLabelsBundle.get("copyTip"))
    private val likeButton = IconButtonCreator.getButton(TestSparkIcons.like, PluginLabelsBundle.get("likeTip"))
    private val dislikeButton =
        IconButtonCreator.getButton(TestSparkIcons.dislike, PluginLabelsBundle.get("dislikeTip"))

    private var allRequestsNumber = 1
    private var currentRequestNumber = 1

    private val testCaseCodeToListOfCoveredLines: HashMap<String, Set<Int>> = hashMapOf()

    private val dimensionSize = 7

    private var isRemoved = false

    // Add an editor to modify the test source code
    private val languageTextField = LanguageTextField(
        Language.findLanguageByID("JAVA"),
        editor.project,
        testCase.testCode,
        TestCaseDocumentCreator(
            getClassWithTestCaseName(testCase.testName),
        ),
        false,
    )

    private val languageTextFieldScrollPane = JBScrollPane(
        languageTextField,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS,
    )

    // Create "Remove" button to remove the test from cache
    private val removeButton =
        IconButtonCreator.getButton(TestSparkIcons.remove, PluginLabelsBundle.get("removeTip"))

    // Create "Reset" button to reset the changes in the source code of the test
    private val resetButton = IconButtonCreator.getButton(TestSparkIcons.reset, PluginLabelsBundle.get("resetTip"))

    // Create "Reset" button to reset the changes to last run in the source code of the test
    private val resetToLastRunButton =
        IconButtonCreator.getButton(TestSparkIcons.resetToLastRun, PluginLabelsBundle.get("resetToLastRunTip"))

    // Create "Run tests" button to remove the test from cache
    private val runTestButton = createRunTestButton()

    private val requestJLabel = JLabel(PluginLabelsBundle.get("requestJLabel"))
    private val requestComboBox = ComboBox(arrayOf("") + JsonEncoding.decode(llmSettingsState.defaultLLMRequests))

    private val sendButton = IconButtonCreator.getButton(TestSparkIcons.send, PluginLabelsBundle.get("send"))

    private val loadingLabel: JLabel = JLabel(TestSparkIcons.loading)

    private val initialCodes: MutableList<String> = mutableListOf()
    private val lastRunCodes: MutableList<String> = mutableListOf()
    private val currentCodes: MutableList<String> = mutableListOf()

    /**
     * Retrieves the upper panel for the GUI.
     *
     * This panel contains various components such as buttons, labels, and checkboxes. It is used to display information and
     * perform actions related to the GUI.
     *
     * @return The JPanel object representing the upper panel.
     */
    fun getUpperPanel(): JPanel {
        updateErrorLabel()
        panel.layout = BoxLayout(panel, BoxLayout.X_AXIS)
        panel.add(Box.createRigidArea(Dimension(checkbox.preferredSize.width, checkbox.preferredSize.height)))
        panel.add(previousButton)
        panel.add(requestLabel)
        panel.add(nextButton)
        panel.add(errorLabel)
        panel.add(Box.createHorizontalGlue())
        panel.add(copyButton)
        panel.add(likeButton)
        panel.add(dislikeButton)
        panel.add(Box.createRigidArea(Dimension(12, 0)))

        previousButton.addActionListener {
            WriteCommandAction.runWriteCommandAction(project) {
                if (currentRequestNumber > 1) currentRequestNumber--
                switchToAnotherCode()
                updateRequestLabel()
            }
        }

        nextButton.addActionListener {
            WriteCommandAction.runWriteCommandAction(project) {
                if (currentRequestNumber < allRequestsNumber) currentRequestNumber++
                switchToAnotherCode()
                updateRequestLabel()
            }
        }

        likeButton.addActionListener {
            if (likeButton.icon == TestSparkIcons.likeSelected) {
                likeButton.icon = TestSparkIcons.like
            } else if (likeButton.icon == TestSparkIcons.like) {
                likeButton.icon = TestSparkIcons.likeSelected
            }
            dislikeButton.icon = TestSparkIcons.dislike
            collectorsData.likedDislikedCollector.logEvent(
                true,
                getTestId(),
                collectorsData.data.technique!!,
                collectorsData.data.codeType!!,
                testCase.testCode != initialCodes[currentRequestNumber - 1],
            )
        }

        dislikeButton.addActionListener {
            if (dislikeButton.icon == TestSparkIcons.dislikeSelected) {
                dislikeButton.icon = TestSparkIcons.dislike
            } else if (dislikeButton.icon == TestSparkIcons.dislike) {
                dislikeButton.icon = TestSparkIcons.dislikeSelected
            }
            likeButton.icon = TestSparkIcons.like
            collectorsData.likedDislikedCollector.logEvent(
                false,
                getTestId(),
                collectorsData.data.technique!!,
                collectorsData.data.codeType!!,
                testCase.testCode != initialCodes[currentRequestNumber - 1],
            )
        }

        copyButton.addActionListener {
            val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
            clipboard.setContents(
                StringSelection(
                    project.service<TestCaseDisplayService>().getEditor(testCase.testName)!!.document.text,
                ),
                null,
            )
            NotificationGroupManager.getInstance()
                .getNotificationGroup("Test case copied")
                .createNotification(
                    "",
                    PluginMessagesBundle.get("testCaseCopied"),
                    NotificationType.INFORMATION,
                )
                .notify(project)
        }

        updateRequestLabel()

        return panel
    }

    /**
     * Retrieves the middle panel of the application.
     * This method sets the border of the languageTextField and
     * adds it to the middlePanel with appropriate spacing.
     */
    fun getMiddlePanel(): JPanel {
        initialCodes.add(testCase.testCode)
        lastRunCodes.add(testCase.testCode)
        currentCodes.add(testCase.testCode)

        // Set border
        updateBorder()

        val panel = JPanel()

        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.add(Box.createRigidArea(Dimension(0, 5)))
        panel.add(languageTextFieldScrollPane)
        panel.add(Box.createRigidArea(Dimension(0, 5)))

        addLanguageTextFieldListener(languageTextField)

        return panel
    }

    /**
     * Returns the bottom panel.
     */
    fun getBottomPanel(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

        val requestPanel = JPanel()
        requestPanel.layout = BoxLayout(requestPanel, BoxLayout.X_AXIS)
        requestPanel.add(Box.createRigidArea(Dimension(checkbox.preferredSize.width, checkbox.preferredSize.height)))
        requestPanel.add(requestJLabel)
        requestPanel.add(Box.createRigidArea(Dimension(dimensionSize, 0)))

        // temporary panel to avoid IDEA's bug
        val requestComboBoxAndSendButtonPanel = JPanel()
        requestComboBoxAndSendButtonPanel.layout = BoxLayout(requestComboBoxAndSendButtonPanel, BoxLayout.X_AXIS)
        requestComboBoxAndSendButtonPanel.add(requestComboBox)
        requestComboBoxAndSendButtonPanel.add(Box.createRigidArea(Dimension(dimensionSize, 0)))
        requestComboBoxAndSendButtonPanel.add(sendButton)
        requestPanel.add(requestComboBoxAndSendButtonPanel)
        requestPanel.add(Box.createRigidArea(Dimension(15, 0)))

        val buttonsPanel = JPanel()
        buttonsPanel.layout = BoxLayout(buttonsPanel, BoxLayout.X_AXIS)
        buttonsPanel.add(Box.createRigidArea(Dimension(checkbox.preferredSize.width, checkbox.preferredSize.height)))
        runTestButton.isEnabled = true
        buttonsPanel.add(runTestButton)
        loadingLabel.isVisible = false
        buttonsPanel.add(loadingLabel)
        buttonsPanel.add(Box.createHorizontalGlue())
        resetButton.isEnabled = false
        buttonsPanel.add(resetButton)
        resetToLastRunButton.isEnabled = false
        buttonsPanel.add(resetToLastRunButton)
        buttonsPanel.add(removeButton)
        buttonsPanel.add(Box.createRigidArea(Dimension(12, 0)))

        panel.add(requestPanel)
        panel.add(buttonsPanel)

        runTestButton.addActionListener {
            val choice = JOptionPane.showConfirmDialog(
                null,
                PluginMessagesBundle.get("runCautionMessage"),
                PluginMessagesBundle.get("confirmationTitle"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE,
            )

            if (choice == JOptionPane.OK_OPTION) runTest()
        }
        resetButton.addActionListener { reset() }
        resetToLastRunButton.addActionListener { resetToLastRun() }
        removeButton.addActionListener { remove() }

        sendButton.addActionListener { sendRequest() }

        requestComboBox.isEditable = true

        return panel
    }

    /**
     * Updates the label displaying the request number information.
     * Uses the requestNumber template to format the label text.
     */
    private fun updateRequestLabel() {
        requestLabel.text = String.format(
            requestNumber,
            currentRequestNumber,
            allRequestsNumber,
        )
    }

    /**
     * Updates the error label with a new message.
     */
    private fun updateErrorLabel() {
        val error = project.service<TestsExecutionResultService>().getCurrentError(testCase.id)
        if (error.isBlank()) {
            errorLabel.isVisible = false
        } else {
            errorLabel.isVisible = true
            errorLabel.toolTipText = error
        }
    }

    /**
     * Adds a document listener to the provided LanguageTextField.
     * The listener triggers the updateUI() method whenever the document of the LanguageTextField changes.
     *
     * @param languageTextField the LanguageTextField to add the listener to
     */
    private fun addLanguageTextFieldListener(languageTextField: LanguageTextField) {
        languageTextField.document.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                updateUI()
            }
        })
    }

    /**
     * Updates the user interface based on the provided code.
     */
    private fun updateUI() {
        updateTestCaseInformation()

        val lastRunCode = lastRunCodes[currentRequestNumber - 1]
        languageTextField.editor?.markupModel?.removeAllHighlighters()

        resetButton.isEnabled = testCase.testCode != initialCodes[currentRequestNumber - 1]
        resetToLastRunButton.isEnabled = testCase.testCode != lastRunCode

        val error = getError()
        if (error.isNullOrBlank()) {
            project.service<TestsExecutionResultService>().addCurrentPassedTest(testCase.id)
        } else {
            project.service<TestsExecutionResultService>().addCurrentFailedTest(testCase.id, error)
        }
        updateErrorLabel()
        runTestButton.isEnabled = (error == null)

        updateBorder()

        val modifiedLineIndexes = ModifiedLinesGetter.getLines(
            lastRunCode.split("\n"),
            testCase.testCode.split("\n"),
        )

        for (index in modifiedLineIndexes) {
            languageTextField.editor!!.markupModel.addLineHighlighter(
                DiffColors.DIFF_MODIFIED,
                index,
                HighlighterLayer.FIRST,
            )
        }

        currentCodes[currentRequestNumber - 1] = testCase.testCode

        // select checkbox
        checkbox.isSelected = true

        if (testCaseCodeToListOfCoveredLines.containsKey(testCase.testCode)) {
            testCase.coveredLines = testCaseCodeToListOfCoveredLines[testCase.testCode]!!
        } else {
            testCase.coveredLines = setOf()
        }

        ReportHelper.updateTestCase(project, report, testCase)
        project.service<TestCaseDisplayService>().updateUI()
    }

    /**
     * Sends a request and adds a new code created based on the request.
     * The request is obtained from the `requestField` text field.
     * The code includes the request and the working code obtained from `initialCodes` based on the current request number.
     * After adding the code, it switches to another code.
     */
    private fun sendRequest() {
        loadingLabel.isVisible = true
        enableComponents(false)

        ProgressManager.getInstance()
            .run(object : Task.Backgroundable(project, PluginMessagesBundle.get("sendingFeedback")) {
                override fun run(indicator: ProgressIndicator) {
                    val ijIndicator = IJProgressIndicator(indicator)
                    if (ToolUtils.isProcessStopped(uiContext!!.errorMonitor, ijIndicator)) {
                        finishProcess()
                        return
                    }

                    collectorsData.feedbackSentCollector.logEvent(
                        collectorsData.data.id!! + "_" + testCase.id,
                        collectorsData.data.technique!!,
                        collectorsData.data.codeType!!,
                        testCase.testCode != initialCodes[currentRequestNumber - 1],
                    )

                    val modifiedTest = LLMHelper.testModificationRequest(
                        initialCodes[currentRequestNumber - 1],
                        requestComboBox.editor.item.toString(),
                        ijIndicator,
                        uiContext.requestManager!!,
                        project,
                        uiContext.testGenerationOutput,
                        uiContext.errorMonitor
                    )

                    if (modifiedTest != null) {
                        modifiedTest.setTestFileName(
                            getClassWithTestCaseName(testCase.testName),
                        )
                        addTest(modifiedTest)
                    }

                    if (ToolUtils.isProcessStopped(uiContext.errorMonitor, ijIndicator)) {
                        finishProcess()
                        return
                    }

                    finishProcess()
                    ijIndicator.stop()
                }
            })
    }

    private fun finishProcess() {
        uiContext!!.errorMonitor.clear()
        loadingLabel.isVisible = false
        enableComponents(true)
    }

    private fun enableComponents(isEnabled: Boolean) {
        nextButton.isEnabled = isEnabled
        previousButton.isEnabled = isEnabled
        runTestButton.isEnabled = isEnabled
        resetToLastRunButton.isEnabled = isEnabled
        resetButton.isEnabled = isEnabled
        removeButton.isEnabled = isEnabled
        sendButton.isEnabled = isEnabled
    }

    private fun addTest(testSuite: TestSuiteGeneratedByLLM) {
        val testSuitePresenter = JUnitTestSuitePresenter(project, uiContext!!.testGenerationOutput)

        WriteCommandAction.runWriteCommandAction(project) {
            uiContext.errorMonitor.clear()
            val code = testSuitePresenter.toString(testSuite)
            testCase.testName = JavaClassBuilderHelper.getTestMethodNameFromClassWithTestCase(testCase.testName, code)
            testCase.testCode = code

            // update numbers
            allRequestsNumber++
            currentRequestNumber = allRequestsNumber
            updateRequestLabel()

            // update lists
            initialCodes.add(code)
            lastRunCodes.add(code)
            currentCodes.add(code)

            requestComboBox.selectedItem = requestComboBox.getItemAt(0)
            sendButton.isEnabled = true

            switchToAnotherCode()
        }
    }

    /**
     * Listens for a click event on the "Run Test" button and runs the test.
     * It updates the test case data in the workspace with the current language input
     * and test name. It disables the "Reset to Last Run" and "Run Test" buttons,
     * updates the border of the language text field with the test name, updates the error
     * label in the test case upper panel, removes all highlighters from the language text field,
     * and updates the UI.
     */
    private fun runTest() {
        if (isRemoved) return
        if (!runTestButton.isEnabled) return

        loadingLabel.isVisible = true
        enableComponents(false)

        ProgressManager.getInstance()
            .run(object : Task.Backgroundable(project, PluginMessagesBundle.get("sendingFeedback")) {
                override fun run(indicator: ProgressIndicator) {
                    runTest(IJProgressIndicator(indicator))
                }
            })
    }

    fun addTask(tasks: Queue<(CustomProgressIndicator) -> Unit>) {
        if (isRemoved) return
        if (!runTestButton.isEnabled) return

        loadingLabel.isVisible = true
        enableComponents(false)

        tasks.add { indicator ->
            runTest(indicator)
        }
    }

    private fun runTest(indicator: CustomProgressIndicator) {
        indicator.setText("Executing ${testCase.testName}")

        val newTestCase = TestProcessor(project)
            .processNewTestCase(
                "${JavaClassBuilderHelper.getClassFromTestCaseCode(testCase.testCode)}.java",
                testCase.id,
                testCase.testName,
                testCase.testCode,
                uiContext!!.testGenerationOutput.packageLine,
                uiContext.testGenerationOutput.resultPath,
                uiContext.projectContext,
            )

        testCase.coveredLines = newTestCase.coveredLines

        testCaseCodeToListOfCoveredLines[testCase.testCode] = testCase.coveredLines

        lastRunCodes[currentRequestNumber - 1] = testCase.testCode

        SwingUtilities.invokeLater {
            updateUI()
        }

        finishProcess()
        indicator.stop()
    }

    /**
     * Resets the button listener for the reset button. When the reset button is clicked,
     * this method is called to perform the necessary actions.
     *
     * This method does the following:
     * 1. Updates the language text field with the test code from the current test case.
     * 2. Sets the border of the language text field based on the test name and test code.
     * 3. Updates the current test case in the workspace.
     * 4. Disables the reset button.
     * 5. Adds the current test to the passed or failed tests in the
     */
    private fun reset() {
        WriteCommandAction.runWriteCommandAction(project) {
            languageTextField.document.setText(initialCodes[currentRequestNumber - 1])
            currentCodes[currentRequestNumber - 1] = testCase.testCode
            lastRunCodes[currentRequestNumber - 1] = testCase.testCode

            updateUI()
        }
    }

    /**
     * Resets the language text field to the code from the last test run and updates the UI accordingly.
     */
    private fun resetToLastRun() {
        WriteCommandAction.runWriteCommandAction(project) {
            languageTextField.document.setText(lastRunCodes[currentRequestNumber - 1])
            currentCodes[currentRequestNumber - 1] = testCase.testCode

            updateUI()
        }
    }

    /**
     * Removes the button listener for the test case.
     *
     * This method is responsible for:
     * 1. Removing the highlighting of the test.
     * 2. Removing the test case from the cache.
     * 3. Updating the UI.
     */
    private fun remove() {
        // Remove the test case from the cache
        project.service<TestCaseDisplayService>().removeTestCase(testCase.testName)

        runTestButton.isEnabled = false
        isRemoved = true

        ReportHelper.removeTestCase(project, report, testCase)
        project.service<TestCaseDisplayService>().updateUI()
    }

    /**
     * Determines if the "Run" button is enabled.
     *
     * @return true if the "Run" button is enabled, false otherwise.
     */
    fun isRunEnabled() = runTestButton.isEnabled

    fun isGlobalModified(): Boolean = testCase.testCode != initialCodes[0]

    /**
     * Updates the border of the languageTextField based on the provided test name and text.
     */
    private fun updateBorder() {
        languageTextField.border = getBorder()
    }

    /**
     * Retrieves the error message for a given test case.
     *
     * @return the error message for the test case
     */
    fun getError() = project.service<TestsExecutionResultService>().getError(testCase.id, testCase.testCode)

    /**
     * Returns the border for a given test case.
     *
     * @return the border for the test case
     */
    private fun getBorder(): Border {
        val size = 3
        return when (getError()) {
            null -> JBUI.Borders.empty()
            "" -> MatteBorder(size, size, size, size, JBColor.GREEN)
            else -> MatteBorder(size, size, size, size, JBColor.RED)
        }
    }

    /**
     * Creates a button to reset the changes in the test source code.
     *
     * @return the created button
     */
    private fun createRunTestButton(): JButton {
        val runTestButton = JButton(PluginLabelsBundle.get("run"), TestSparkIcons.runTest)
        runTestButton.isOpaque = false
        runTestButton.isContentAreaFilled = false
        runTestButton.isBorderPainted = true
        return runTestButton
    }

    /**
     * Switches to another code in the language text field.
     * Retrieves the current request number from the test case upper panel factory and uses it to retrieve the corresponding code from the current codes array.
     * Sets the retrieved code as the text of the language text field document.
     */
    private fun switchToAnotherCode() {
        languageTextField.document.setText(currentCodes[currentRequestNumber - 1])
        updateUI()
    }

    /**
     * Checks if the item is marked as removed.
     *
     * @return true if the item is removed, false otherwise.
     */
    fun isRemoved() = isRemoved

    /**
     * Updates the current test case with the specified test name and test code.
     */
    private fun updateTestCaseInformation() {
        testCase.testName =
            JavaClassBuilderHelper.getTestMethodNameFromClassWithTestCase(testCase.testName, languageTextField.document.text)
        testCase.testCode = languageTextField.document.text
    }

    private fun getTestId(): String = collectorsData.data.id!! + "_" + testCase.id
}
