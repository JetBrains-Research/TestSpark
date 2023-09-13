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
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.border.Border
import javax.swing.border.MatteBorder

class TestCaseMiddleAndBottomPanelFactory(
    private val project: Project,
    private val testCase: TestCase,
    private val editor: Editor,
    private val checkbox: JCheckBox,
    private val testCaseUpperPanelFactory: TestCaseUpperPanelFactory,

) {
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

    // Create "Remove" button to remove the test from cache
    private val removeButton = createButton(TestSparkIcons.remove, TestSparkLabelsBundle.defaultValue("removeTip"))

    // Create "Reset" button to reset the changes in the source code of the test
    private val resetButton = createButton(TestSparkIcons.reset, TestSparkLabelsBundle.defaultValue("resetTip"))

    // Create "Reset" button to reset the changes to last run in the source code of the test
    private val resetToLastRunButton =
        createButton(TestSparkIcons.resetToLastRun, TestSparkLabelsBundle.defaultValue("resetToLastRunTip"))

    // Create "Run tests" button to remove the test from cache
    private val runTestButton = createRunTestButton()

    /**
     * Retrieves the middle panel of the application.
     * This method sets the border of the languageTextField and
     * adds it to the middlePanel with appropriate spacing.
     */
    fun getMiddlePanel(): JPanel {
        // Set border
        languageTextField.border = getBorder(testCase.testName, testCase.testCode)

        val panel = JPanel()

        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.add(Box.createRigidArea(Dimension(0, 5)))
        panel.add(languageTextField)
        panel.add(Box.createRigidArea(Dimension(0, 5)))

        addLanguageTextFieldListener(languageTextField)

        return panel
    }

    private fun addLanguageTextFieldListener(languageTextField: LanguageTextField) {
        languageTextField.document.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                val lastRunCode =
                    project.service<Workspace>().testJob!!.report.testCaseList[testCase.testName]!!.testCode
                languageTextField.editor!!.markupModel.removeAllHighlighters()

                resetButton.isEnabled = languageTextField.document.text != testCase.testCode
                resetToLastRunButton.isEnabled = languageTextField.document.text != lastRunCode

                val error = getError(testCase.testName, languageTextField.document.text)
                if (error.isNullOrBlank()) {
                    project.service<TestsExecutionResultService>().addCurrentPassedTest(testCase.testName)
                } else {
                    project.service<TestsExecutionResultService>().addCurrentFailedTest(testCase.testName, error)
                }
                testCaseUpperPanelFactory.updateErrorLabel()
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

                // select checkbox
                checkbox.isSelected = true
            }
        })
    }

    /**
     * Returns the bottom panel.
     */
    fun getBottomPanel(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.X_AXIS)
        panel.add(Box.createRigidArea(Dimension(checkbox.preferredSize.width, checkbox.preferredSize.height)))
        runTestButton.isEnabled = false
        panel.add(runTestButton)
        panel.add(Box.createHorizontalGlue())
        resetButton.isEnabled = false
        panel.add(resetButton)
        panel.add(Box.createRigidArea(Dimension(5, 0)))
        resetToLastRunButton.isEnabled = false
        panel.add(resetToLastRunButton)
        panel.add(Box.createRigidArea(Dimension(5, 0)))
        panel.add(removeButton)
        panel.add(Box.createRigidArea(Dimension(10, 0)))

        runTestButton.addActionListener { runTestButtonListener() }
        resetButton.addActionListener { resetButtonListener() }
        resetToLastRunButton.addActionListener { resetToLastRunButtonListener() }
        removeButton.addActionListener { removeButtonListener() }

        return panel
    }

    /**
     * Listens for a click event on the "Run Test" button and runs the test.
     * It updates the test case data in the workspace with the current language input
     * and test name. It disables the "Reset to Last Run" and "Run Test" buttons,
     * updates the border of the language text field with the test name, updates the error
     * label in the test case upper panel, removes all highlighters from the language text field,
     * and updates the UI.
     */
    private fun runTestButtonListener() {
        project.service<Workspace>().updateTestCase(
            project.service<TestCoverageCollectorService>()
                .updateDataWithTestCase(languageTextField.document.text, testCase.testName),
        )
        resetToLastRunButton.isEnabled = false
        runTestButton.isEnabled = false
        updateBorder()
        testCaseUpperPanelFactory.updateErrorLabel()
        languageTextField.editor!!.markupModel.removeAllHighlighters()

        project.service<TestCaseDisplayService>().updateUI()
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
    private fun resetButtonListener() {
        WriteCommandAction.runWriteCommandAction(project) {
            languageTextField.document.setText(testCase.testCode)
            updateBorder()
            project.service<Workspace>().updateTestCase(testCase)
            resetButton.isEnabled = false
            if (getError(testCase.testName, languageTextField.document.text)!!.isBlank()) {
                project.service<TestsExecutionResultService>().addPassedTest(testCase.testName, testCase.testCode)
            } else {
                project.service<TestsExecutionResultService>()
                    .addFailedTest(testCase.testName, testCase.testCode, testCaseUpperPanelFactory.getCurrentError())
            }
            resetToLastRunButton.isEnabled = false
            runTestButton.isEnabled = false
            testCaseUpperPanelFactory.updateErrorLabel()
            languageTextField.editor!!.markupModel.removeAllHighlighters()
            project.service<TestCaseDisplayService>().updateUI()
        }
    }

    /**
     * Resets the language text field to the code from the last test run and updates the UI accordingly.
     */
    private fun resetToLastRunButtonListener() {
        WriteCommandAction.runWriteCommandAction(project) {
            val code = project.service<Workspace>().testJob!!.report.testCaseList[testCase.testName]!!.testCode
            languageTextField.document.setText(code)
            resetToLastRunButton.isEnabled = false
            runTestButton.isEnabled = false
            updateBorder()
            testCaseUpperPanelFactory.updateErrorLabel()
            languageTextField.editor!!.markupModel.removeAllHighlighters()

            project.service<TestCaseDisplayService>().updateUI()
        }
    }

    /**
     * Removes the button listener for the test case.
     *
     * This method is responsible for removing the highlighting of the test, removing the test case from the cache,
     * and updating the UI.
     */
    private fun removeButtonListener() {
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
        val runTestButton = JButton("Run", TestSparkIcons.runTest)
        runTestButton.isEnabled = false
        runTestButton.isOpaque = false
        runTestButton.isContentAreaFilled = false
        runTestButton.isBorderPainted = true
        runTestButton.preferredSize = Dimension(60, 30)
        return runTestButton
    }

    /**
     * Returns the indexes of lines that are modified between two lists of strings.
     *
     * @param source The source list of strings.
     * @param target The target list of strings.
     * @return The indexes of modified lines.
     */
    fun getModifiedLines(source: List<String>, target: List<String>): List<Int> {
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
}
