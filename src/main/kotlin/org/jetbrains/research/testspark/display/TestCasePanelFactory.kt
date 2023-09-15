package org.jetbrains.research.testspark.display

import com.intellij.lang.Language
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.diff.DiffColors
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.LanguageTextField
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import org.jetbrains.research.testspark.TestSparkLabelsBundle
import org.jetbrains.research.testspark.data.TestCase
import org.jetbrains.research.testspark.editor.Workspace
import org.jetbrains.research.testspark.services.COVERAGE_SELECTION_TOGGLE_TOPIC
import org.jetbrains.research.testspark.services.JavaClassBuilderService
import org.jetbrains.research.testspark.services.TestCaseDisplayService
import org.jetbrains.research.testspark.services.TestCoverageCollectorService
import org.jetbrains.research.testspark.services.TestsExecutionResultService
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
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.ScrollPaneConstants
import javax.swing.border.Border
import javax.swing.border.MatteBorder

class TestCasePanelFactory(
    private val project: Project,
    private val testCase: TestCase,
    private val editor: Editor,
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

    private val loadingIcon: JLabel = JLabel(TestSparkIcons.loading)

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
        panel.add(Box.createRigidArea(Dimension(5, 0)))
        panel.add(requestLabel)
        panel.add(Box.createRigidArea(Dimension(5, 0)))
        panel.add(nextButtons)
        panel.add(Box.createRigidArea(Dimension(5, 0)))
        panel.add(errorLabel)
        panel.add(Box.createHorizontalGlue())
        panel.add(copyButton)
        panel.add(Box.createRigidArea(Dimension(5, 0)))
        panel.add(likeButton)
        panel.add(Box.createRigidArea(Dimension(5, 0)))
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
//            TODO add implementation
        }

        dislikeButton.addActionListener {
            if (dislikeButton.icon == TestSparkIcons.dislikeSelected) {
                dislikeButton.icon = TestSparkIcons.dislike
            } else if (dislikeButton.icon == TestSparkIcons.dislike) {
                dislikeButton.icon = TestSparkIcons.dislikeSelected
            }
            likeButton.icon = TestSparkIcons.like
//            TODO add implementation
        }

        copyButton.addActionListener {
            val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
            clipboard.setContents(
                StringSelection(
                    project.service<TestCaseDisplayService>().getEditor(testCase.testName)!!.document.text,
                ),
                null,
            )
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
        initialCodes.add(languageTextField.document.text)
        lastRunCodes.add(languageTextField.document.text)
        currentCodes.add(languageTextField.document.text)

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
        requestPanel.add(Box.createRigidArea(Dimension(5, 0)))
        requestPanel.add(sendButton)
        requestPanel.add(Box.createRigidArea(Dimension(15, 0)))

        val buttonsPanel = JPanel()
        buttonsPanel.layout = BoxLayout(buttonsPanel, BoxLayout.X_AXIS)
        buttonsPanel.add(Box.createRigidArea(Dimension(checkbox.preferredSize.width, checkbox.preferredSize.height)))
        runTestButton.isEnabled = false
        buttonsPanel.add(runTestButton)
        buttonsPanel.add(Box.createRigidArea(Dimension(5, 0)))
        loadingIcon.isVisible = false
        buttonsPanel.add(loadingIcon)
        buttonsPanel.add(Box.createHorizontalGlue())
        resetButton.isEnabled = false
        buttonsPanel.add(resetButton)
        buttonsPanel.add(Box.createRigidArea(Dimension(5, 0)))
        resetToLastRunButton.isEnabled = false
        buttonsPanel.add(resetToLastRunButton)
        buttonsPanel.add(Box.createRigidArea(Dimension(5, 0)))
        buttonsPanel.add(removeButton)
        buttonsPanel.add(Box.createRigidArea(Dimension(12, 0)))

        panel.add(requestPanel)
        panel.add(buttonsPanel)

        runTestButton.addActionListener { runTest() }
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
        val error = project.service<TestsExecutionResultService>().getCurrentError(testCase.testName)
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
        val lastRunCode = lastRunCodes[currentRequestNumber - 1]
        languageTextField.editor!!.markupModel.removeAllHighlighters()

        resetButton.isEnabled = languageTextField.document.text != initialCodes[currentRequestNumber - 1]
        resetToLastRunButton.isEnabled = languageTextField.document.text != lastRunCode

        val error = getError(testCase.testName, languageTextField.document.text)
        if (error.isNullOrBlank()) {
            project.service<TestsExecutionResultService>().addCurrentPassedTest(testCase.testName)
        } else {
            project.service<TestsExecutionResultService>().addCurrentFailedTest(testCase.testName, error)
        }
        updateErrorLabel()
        runTestButton.isEnabled = (error == null)

        updateBorder()

        val modifiedLineIndexes = getModifiedLines(
            lastRunCode.split("\n"),
            languageTextField.document.text.split("\n"),
        )

        for (index in modifiedLineIndexes) {
            languageTextField.editor!!.markupModel.addLineHighlighter(
                DiffColors.DIFF_MODIFIED,
                index,
                HighlighterLayer.FIRST,
            )
        }

        currentCodes[currentRequestNumber - 1] = languageTextField.document.text

        // select checkbox
        checkbox.isSelected = true
    }

    /**
     * Sends a request and adds a new code created based on the request.
     * The request is obtained from the `requestField` text field.
     * The code includes the request and the working code obtained from `initialCodes` based on the current request number.
     * After adding the code, it switches to another code.
     */
    private fun sendRequest() {
        WriteCommandAction.runWriteCommandAction(project) {
            loadingIcon.isVisible = true
            loadingIcon.repaint()

            // TODO implement code creator
            val code = "// Here will be a new code.\n" +
                "// Your request: ${requestField.text}.\n" +
                "// Working code:\n" +
                initialCodes[currentRequestNumber - 1]

            // run new code
            project.service<Workspace>().updateTestCase(
                project.service<TestCoverageCollectorService>()
                    .updateDataWithTestCase(code, testCase.testName),
            )

            // update numbers
            allRequestsNumber++
            currentRequestNumber = allRequestsNumber
            updateRequestLabel()

            // update lists
            initialCodes.add(code)
            lastRunCodes.add(code)
            currentCodes.add(code)

            requestField.text = ""
            sendButton.isEnabled = false

            switchToAnotherCode()

            loadingIcon.isVisible = false
            loadingIcon.repaint()
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
        loadingIcon.isVisible = true
        loadingIcon.repaint()

        project.service<Workspace>().updateTestCase(
            project.service<TestCoverageCollectorService>()
                .updateDataWithTestCase(languageTextField.document.text, testCase.testName),
        )
        resetToLastRunButton.isEnabled = false
        runTestButton.isEnabled = false
        updateBorder()
        updateErrorLabel()
        languageTextField.editor!!.markupModel.removeAllHighlighters()

        lastRunCodes[currentRequestNumber - 1] = languageTextField.document.text

        project.service<TestCaseDisplayService>().updateUI()

        loadingIcon.isVisible = false
        loadingIcon.repaint()
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
            updateBorder()
            project.service<Workspace>().updateTestCase(testCase)
            resetButton.isEnabled = false
            if (getError(testCase.testName, languageTextField.document.text)!!.isBlank()) {
                project.service<TestsExecutionResultService>().addPassedTest(testCase.testName, testCase.testCode)
            } else {
                project.service<TestsExecutionResultService>()
                    .addFailedTest(testCase.testName, testCase.testCode, errorLabel.toolTipText)
            }
            resetToLastRunButton.isEnabled = false
            runTestButton.isEnabled = false
            updateErrorLabel()
            languageTextField.editor!!.markupModel.removeAllHighlighters()

            currentCodes[currentRequestNumber - 1] = languageTextField.document.text
            lastRunCodes[currentRequestNumber - 1] = languageTextField.document.text

            project.service<TestCaseDisplayService>().updateUI()
        }
    }

    /**
     * Resets the language text field to the code from the last test run and updates the UI accordingly.
     */
    private fun resetToLastRun() {
        WriteCommandAction.runWriteCommandAction(project) {
            val code = lastRunCodes[currentRequestNumber - 1]
            languageTextField.document.setText(code)
            resetToLastRunButton.isEnabled = false
            runTestButton.isEnabled = false
            updateBorder()
            updateErrorLabel()
            languageTextField.editor!!.markupModel.removeAllHighlighters()

            currentCodes[currentRequestNumber - 1] = languageTextField.document.text

            project.service<TestCaseDisplayService>().updateUI()
        }
    }

    /**
     * Removes the button listener for the test case.
     *
     * This method is responsible for removing the highlighting of the test, removing the test case from the cache,
     * and updating the UI.
     */
    private fun remove() {
        // Remove the highlighting of the test
        project.messageBus.syncPublisher(COVERAGE_SELECTION_TOGGLE_TOPIC)
            .testGenerationResult(testCase.testName, false, editor)

        // Remove the test case from the cache
        project.service<TestCaseDisplayService>().removeTestCase(testCase.testName)

        project.service<TestCaseDisplayService>().updateUI()
    }

    /**
     * Updates the border of the languageTextField based on the provided test name and text.
     */
    private fun updateBorder() {
        languageTextField.border = getBorder(testCase.testName, languageTextField.document.text)
    }

    /**
     * Retrieves the error message for a given test case.
     *
     * @param testCaseName the name of the test case
     * @param testCaseCode the code of the test case
     * @return the error message for the test case
     */
    private fun getError(testCaseName: String, testCaseCode: String) =
        project.service<TestsExecutionResultService>().getError(testCaseName, testCaseCode)

    /**
     * Returns the border for a given test case.
     *
     * @param testCaseName the name of the test case
     * @return the border for the test case
     */
    private fun getBorder(testCaseName: String, testCaseCode: String): Border {
        val size = 3
        return when (getError(testCaseName, testCaseCode)) {
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
        runTestButton.isEnabled = false
        runTestButton.isOpaque = false
        runTestButton.isContentAreaFilled = false
        runTestButton.isBorderPainted = true
        runTestButton.preferredSize = Dimension(60, 30)
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
