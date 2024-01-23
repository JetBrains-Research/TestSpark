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
import com.intellij.ui.JBColor
import com.intellij.ui.LanguageTextField
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import org.jetbrains.research.testspark.bundles.TestSparkBundle
import org.jetbrains.research.testspark.bundles.TestSparkLabelsBundle
import org.jetbrains.research.testspark.data.TestCase
import org.jetbrains.research.testspark.editor.Workspace
import org.jetbrains.research.testspark.services.CollectorService
import org.jetbrains.research.testspark.services.ErrorService
import org.jetbrains.research.testspark.services.JavaClassBuilderService
import org.jetbrains.research.testspark.services.LLMChatService
import org.jetbrains.research.testspark.services.ReportLockingService
import org.jetbrains.research.testspark.services.TestCaseDisplayService
import org.jetbrains.research.testspark.services.TestStorageProcessingService
import org.jetbrains.research.testspark.services.TestsExecutionResultService
import org.jetbrains.research.testspark.tools.llm.test.TestSuiteGeneratedByLLM
import org.jetbrains.research.testspark.tools.processStopped
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.FocusManager
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.ScrollPaneConstants
import javax.swing.SwingUtilities
import javax.swing.border.Border
import javax.swing.border.MatteBorder

class TestCasePanelFactory(
    private val project: Project,
    private val testCase: TestCase,
    editor: Editor,
    private val checkbox: JCheckBox,
) {
    private val panel = JPanel()
    private val previousButtons =
        createButton(TestSparkIcons.previous, TestSparkLabelsBundle.defaultValue("previousRequest"))
    private var requestNumber: String = "%d / %d"
    private var requestLabel: JLabel = JLabel(requestNumber)
    private val nextButtons = createButton(TestSparkIcons.next, TestSparkLabelsBundle.defaultValue("nextRequest"))
    private val errorLabel = JLabel(TestSparkIcons.showError)
    private val copyButton = createButton(TestSparkIcons.copy, TestSparkLabelsBundle.defaultValue("copyTip"))
    private val likeButton = createButton(TestSparkIcons.like, TestSparkLabelsBundle.defaultValue("likeTip"))
    private val dislikeButton = createButton(TestSparkIcons.dislike, TestSparkLabelsBundle.defaultValue("dislikeTip"))

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
            project.service<JavaClassBuilderService>().getClassWithTestCaseName(testCase.testName),
        ),
        false,
    )

    private val languageTextFieldScrollPane = JBScrollPane(
        languageTextField,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS,
    )

    // Create "Remove" button to remove the test from cache
    private val removeButton = createButton(TestSparkIcons.remove, TestSparkLabelsBundle.defaultValue("removeTip"))

    // Create "Reset" button to reset the changes in the source code of the test
    private val resetButton = createButton(TestSparkIcons.reset, TestSparkLabelsBundle.defaultValue("resetTip"))

    // Create "Reset" button to reset the changes to last run in the source code of the test
    private val resetToLastRunButton =
        createButton(TestSparkIcons.resetToLastRun, TestSparkLabelsBundle.defaultValue("resetToLastRunTip"))

    // Create "Run tests" button to remove the test from cache
    private val runTestButton = createRunTestButton()

    private val requestField = HintTextField(TestSparkLabelsBundle.defaultValue("requestFieldHint"))

    private val sendButton = createButton(TestSparkIcons.send, TestSparkLabelsBundle.defaultValue("send"))

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
        panel.add(previousButtons)
        panel.add(requestLabel)
        panel.add(nextButtons)
        panel.add(errorLabel)
        panel.add(Box.createHorizontalGlue())
        panel.add(copyButton)
        panel.add(likeButton)
        panel.add(dislikeButton)
        panel.add(Box.createRigidArea(Dimension(12, 0)))

        previousButtons.addActionListener {
            WriteCommandAction.runWriteCommandAction(project) {
                if (currentRequestNumber > 1) currentRequestNumber--
                switchToAnotherCode()
                updateRequestLabel()
            }
        }

        nextButtons.addActionListener {
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

            project.service<CollectorService>().likedDislikedCollector.logEvent(
                true,
                getTestId(),
                project.service<Workspace>().technique!!,
                project.service<Workspace>().codeType!!,
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

            project.service<CollectorService>().likedDislikedCollector.logEvent(
                false,
                getTestId(),
                project.service<Workspace>().technique!!,
                project.service<Workspace>().codeType!!,
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
                    TestSparkBundle.message("testCaseCopied"),
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
        requestPanel.add(requestField)
        requestPanel.add(Box.createRigidArea(Dimension(dimensionSize, 0)))
        requestPanel.add(sendButton)
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
                TestSparkBundle.message("runCautionMessage"),
                TestSparkBundle.message("confirmationTitle"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE,
            )

            if (choice == JOptionPane.OK_OPTION) runTest()
        }
        resetButton.addActionListener { reset() }
        resetToLastRunButton.addActionListener { resetToLastRun() }
        removeButton.addActionListener { remove() }

        sendButton.isEnabled = false
        sendButton.addActionListener { sendRequest() }

        // Add a document listener to listen for changes
        requestField.document.addDocumentListener(object : DocumentListener, javax.swing.event.DocumentListener {
            override fun insertUpdate(e: javax.swing.event.DocumentEvent?) {
                textChanged()
            }

            override fun removeUpdate(e: javax.swing.event.DocumentEvent?) {
                textChanged()
            }

            override fun changedUpdate(e: javax.swing.event.DocumentEvent?) {
                textChanged()
            }

            private fun textChanged() {
                sendButton.isEnabled = requestField.text.isNotBlank()
            }
        })

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

        val modifiedLineIndexes = getModifiedLines(
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

        project.service<ReportLockingService>().updateTestCase(testCase)
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
        sendButton.isEnabled = false

        ProgressManager.getInstance()
            .run(object : Task.Backgroundable(project, TestSparkBundle.message("sendingFeedback")) {
                override fun run(indicator: ProgressIndicator) {
                    if (processStopped(project, indicator)) return

                    project.service<CollectorService>().feedbackSentCollector.logEvent(
                        project.service<Workspace>().id!! + "_" + testCase.id,
                        project.service<Workspace>().technique!!,
                        project.service<Workspace>().codeType!!,
                        testCase.testCode != initialCodes[currentRequestNumber - 1],
                    )

                    val modifiedTest = project.service<LLMChatService>()
                        .testModificationRequest(
                            initialCodes[currentRequestNumber - 1],
                            requestField.text,
                            indicator,
                            project,
                        )

                    if (modifiedTest != null) {
                        modifiedTest.setTestFileName(
                            project.service<JavaClassBuilderService>().getClassWithTestCaseName(testCase.testName),
                        )
                        addTest(modifiedTest)
                    } else {
                        NotificationGroupManager.getInstance()
                            .getNotificationGroup("LLM Execution Error")
                            .createNotification(
                                TestSparkBundle.message("llmWarningTitle"),
                                TestSparkBundle.message("noRequestFromLLM"),
                                NotificationType.WARNING,
                            )
                            .notify(project)

                        loadingLabel.isVisible = false
                        sendButton.isEnabled = true
                    }

                    if (processStopped(project, indicator)) return

                    indicator.stop()
                }
            })
    }

    private fun addTest(testSuite: TestSuiteGeneratedByLLM) {
        WriteCommandAction.runWriteCommandAction(project) {
            project.service<ErrorService>().clear()
            val code = testSuite.toString()
            testCase.testName =
                project.service<JavaClassBuilderService>()
                    .getTestMethodNameFromClassWithTestCase(testCase.testName, code)
            testCase.testCode = code

            // update numbers
            allRequestsNumber++
            currentRequestNumber = allRequestsNumber
            updateRequestLabel()

            // update lists
            initialCodes.add(code)
            lastRunCodes.add(code)
            currentCodes.add(code)

            requestField.text = ""
            loadingLabel.isVisible = false

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
    fun runTest() {
        if (isRemoved) return
        if (!runTestButton.isEnabled) return

        loadingLabel.isVisible = true
        runTestButton.isEnabled = false

        SwingUtilities.invokeLater {
            val newTestCase = project.service<TestStorageProcessingService>()
                .processNewTestCase(
                    "${project.service<JavaClassBuilderService>().getClassFromTestCaseCode(testCase.testCode)}.java",
                    testCase.id,
                    testCase.testName,
                    testCase.testCode,
                )
            testCase.coveredLines = newTestCase.coveredLines

            testCaseCodeToListOfCoveredLines[testCase.testCode] = testCase.coveredLines

            lastRunCodes[currentRequestNumber - 1] = testCase.testCode
            loadingLabel.isVisible = false

            updateUI()
        }
    }

    /**
     * Resets the button listener for the reset button. When the reset button is clicked,
     * this method is called to perform the necessary actions.
     *
     * This method updates the language text field with the test code from the current test case,
     * sets the border of the language text field based on the test name and test code,
     * updates the current test case in the workspace,
     * disables the reset button,
     * adds the current test to the passed or failed tests in the*/
    private fun reset() {
        WriteCommandAction.runWriteCommandAction(project) {
            languageTextField.document.setText(initialCodes[currentRequestNumber - 1])
            currentCodes[currentRequestNumber - 1] = testCase.testCode
            lastRunCodes[currentRequestNumber - 1] = testCase.testCode

            updateUI()
        }
    }

    fun isGlobalModified(): Boolean = testCase.testCode != initialCodes[0]

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
     * This method is responsible for removing the highlighting of the test, removing the test case from the cache,
     * and updating the UI.
     */
    private fun remove() {
        // Remove the test case from the cache
        project.service<TestCaseDisplayService>().removeTestCase(testCase.testName)

        runTestButton.isEnabled = false
        isRemoved = true

        project.service<ReportLockingService>().removeTestCase(testCase)
        project.service<TestCaseDisplayService>().updateUI()
    }

    /**
     * Determines if the "Run" button is enabled.
     *
     * @return true if the "Run" button is enabled, false otherwise.
     */
    fun isRunEnabled() = runTestButton.isEnabled

    /**
     * Updates the border of the languageTextField based on the provided test name and text.
     */
    private fun updateBorder() {
        languageTextField.border = getBorder()
    }

    /**
     * Retrieves the error message for a given test case.
     *
     * @param testCaseId the id of the test case
     * @param testCaseCode the code of the test case
     * @return the error message for the test case
     */
    fun getError() = project.service<TestsExecutionResultService>().getError(testCase.id, testCase.testCode)

    /**
     * Returns the border for a given test case.
     *
     * @param testCaseId the id of the test case
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
        val runTestButton = JButton(TestSparkLabelsBundle.defaultValue("run"), TestSparkIcons.runTest)
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
     * Returns the indexes of lines that are modified between two lists of strings.
     *
     * @param source The source list of strings.
     * @param target The target list of strings.
     * @return The indexes of modified lines.
     */
    private fun getModifiedLines(source: List<String>, target: List<String>): List<Int> {
        val dp = Array(source.size + 1) { IntArray(target.size + 1) }

        for (i in 1..source.size) {
            for (j in 1..target.size) {
                if (source[i - 1] == target[j - 1]) {
                    dp[i][j] = dp[i - 1][j - 1] + 1
                } else {
                    dp[i][j] = maxOf(dp[i - 1][j], dp[i][j - 1])
                }
            }
        }

        var i = source.size
        var j = target.size

        val modifiedLineIndexes = mutableListOf<Int>()

        while (i > 0 && j > 0) {
            if (source[i - 1] == target[j - 1]) {
                i--
                j--
            } else if (dp[i][j] == dp[i - 1][j]) {
                i--
            } else if (dp[i][j] == dp[i][j - 1]) {
                modifiedLineIndexes.add(j - 1)
                j--
            }
        }

        while (j > 0) {
            modifiedLineIndexes.add(j - 1)
            j--
        }

        modifiedLineIndexes.reverse()

        return modifiedLineIndexes
    }

    /**
     * Updates the current test case with the specified test name and test code.
     */
    private fun updateTestCaseInformation() {
        testCase.testName =
            project.service<JavaClassBuilderService>()
                .getTestMethodNameFromClassWithTestCase(testCase.testName, languageTextField.document.text)
        testCase.testCode = languageTextField.document.text
    }

    /**
     * Retrieves the test ID by concatenating the workspace ID and the test case ID.
     *
     * @return The test ID as a string.
     */
    private fun getTestId(): String = project.service<Workspace>().id!! + "_" + testCase.id

    /**
     * A custom JTextField with a hint text that is displayed when the field is empty and not in focus.
     */
    class HintTextField(private val hint: String) : JTextField() {
        override fun paintComponent(pG: Graphics) {
            super.paintComponent(pG)
            if (getText().isEmpty() && FocusManager.getCurrentKeyboardFocusManager().focusOwner !== this) {
                val g = pG as Graphics2D
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                g.color = disabledTextColor
                g.drawString(
                    hint,
                    getInsets().left + 5,
                    getInsets().top + (1.3 * pG.getFontMetrics().maxAscent).toInt(),
                )
            }
        }
    }
}
