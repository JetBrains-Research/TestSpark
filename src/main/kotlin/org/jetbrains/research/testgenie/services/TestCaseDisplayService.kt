package org.jetbrains.research.testgenie.services

import com.intellij.coverage.CoverageDataManager
import com.intellij.coverage.CoverageSuitesBundle
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.diff.DiffColors
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiElementFactory
import com.intellij.refactoring.suggested.newRange
import com.intellij.refactoring.suggested.startOffset
import com.intellij.ui.EditorTextField
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.content.ContentManager
import com.intellij.util.containers.stream
import com.intellij.util.ui.JBUI
import org.jetbrains.research.testgenie.TestGenieBundle
import org.jetbrains.research.testgenie.TestGenieLabelsBundle
import org.jetbrains.research.testgenie.editor.Workspace
import org.jetbrains.research.testgenie.evosuite.Pipeline
import org.jetbrains.research.testgenie.evosuite.validation.Validator
import org.evosuite.utils.CompactReport
import org.evosuite.utils.CompactTestCase
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.FlowLayout
import java.util.Locale
import javax.swing.JPanel
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.BoxLayout
import javax.swing.BorderFactory
import javax.swing.JOptionPane
import javax.swing.JCheckBox
import javax.swing.Box
import javax.swing.border.Border
import kotlin.streams.toList

class TestCaseDisplayService(private val project: Project) {

    private var cacheLazyPipeline: Pipeline? = null

    private val mainPanel: JPanel = JPanel()
    private val applyButton: JButton = JButton(TestGenieLabelsBundle.defaultValue("applyButton"))
    private val selectAllButton: JButton = JButton(TestGenieLabelsBundle.defaultValue("selectAllButton"))
    private val deselectAllButton: JButton = JButton(TestGenieLabelsBundle.defaultValue("deselectAllButton"))
    private val removeAllButton: JButton = JButton(TestGenieLabelsBundle.defaultValue("removeAllButton"))
    val validateButton: JButton = JButton(TestGenieLabelsBundle.defaultValue("validateButton"))
    val toggleJacocoButton: JButton = JButton(TestGenieLabelsBundle.defaultValue("jacocoToggle"))

    private var testsSelected: Int = 0
    private val testsSelectedText: String = "${TestGenieLabelsBundle.defaultValue("testsSelected")}: %d/%d"
    private val testsSelectedLabel: JLabel = JLabel(testsSelectedText)

    private val allTestCasePanel: JPanel = JPanel()
    private val scrollPane: JBScrollPane = JBScrollPane(
        allTestCasePanel,
        JBScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
        JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER,
    )
    private var testCasePanels: HashMap<String, JPanel> = HashMap()
    private var originalTestCases: HashMap<String, String> = HashMap()

    // Default color for the editors in the tool window
    private var defaultEditorColor: Color? = null
    private var defaultBorder: Border? = null

    // Content Manager to be able to add / remove tabs from tool window
    private var contentManager: ContentManager? = null

    // Variable to keep reference to the coverage visualisation content
    private var content: Content? = null

    private var testJob: Workspace.TestJob? = null
    private var currentJacocoCoverageBundle: CoverageSuitesBundle? = null
    private var isJacocoCoverageActive = false

    // Code required of imports and package for generated tests
    var importsCode: String = ""
    var packageLine: String = ""

    var fileUrl: String = ""

    init {
        allTestCasePanel.layout = BoxLayout(allTestCasePanel, BoxLayout.Y_AXIS)
        mainPanel.layout = BorderLayout()

        val topButtons = JPanel()
        topButtons.layout = FlowLayout(FlowLayout.TRAILING)

        topButtons.add(testsSelectedLabel)
        topButtons.add(selectAllButton)
        topButtons.add(deselectAllButton)
        topButtons.add(removeAllButton)
        topButtons.add(validateButton)
        topButtons.add(toggleJacocoButton)

        mainPanel.add(topButtons, BorderLayout.NORTH)
        mainPanel.add(scrollPane, BorderLayout.CENTER)
        mainPanel.add(applyButton, BorderLayout.SOUTH)

        applyButton.addActionListener { applyTests() }
        validateButton.addActionListener { validateTests() }
        selectAllButton.addActionListener { toggleAllCheckboxes(true) }
        deselectAllButton.addActionListener { toggleAllCheckboxes(false) }
        toggleJacocoButton.addActionListener { toggleJacocoCoverage() }
        removeAllButton.addActionListener { removeAllTestCases() }
    }

    fun makeValidatedButtonAvailable() {
        validateButton.isEnabled = true
    }

    fun setJacocoReport(coverageSuitesBundle: CoverageSuitesBundle) {
        currentJacocoCoverageBundle = coverageSuitesBundle
    }

    /**
     * Creates the complete panel in the "Generated Tests" tab,
     * and adds the "Generated Tests" tab to the sidebar tool window.
     *
     * @param testJob the new test job
     * @param editor editor instance where coverage should be
     *               visualized
     * @param cacheLazyPipeline the runner that was instantiated but not used to create the test suite
     *                        due to a cache hit, or null if there was a cache miss
     */
    fun showGeneratedTests(testJob: Workspace.TestJob, editor: Editor, cacheLazyPipeline: Pipeline?) {
        this.testJob = testJob
        this.cacheLazyPipeline = cacheLazyPipeline
        displayTestCases(testJob.report, editor)
        displayLazyRunnerButton()
        createToolWindowTab()
    }

    /**
     * Fill the panel with the generated test cases. Remove all previously shown test cases.
     * Add Tests and their names to a List of pairs (used for highlighting)
     *
     * @param testReport The report from which each testcase should be displayed
     * @param editor editor instance where coverage should be
     *               visualized
     */
    private fun displayTestCases(testReport: CompactReport, editor: Editor) {
        allTestCasePanel.removeAll()
        testCasePanels.clear()
        testReport.testCaseList.values.forEach {
            val testCase = it
            val testCasePanel = JPanel()
            testCasePanel.layout = BorderLayout()

            // Fix Windows line separators
            val testCodeFormatted = testCase.testCode.replace("\r\n", "\n")
            originalTestCases[testCase.testName] = testCodeFormatted

            // Add a checkbox to select the test
            val checkbox = JCheckBox()
            checkbox.isSelected = true
            testCasePanel.add(checkbox, BorderLayout.WEST)

            // Toggle coverage when checkbox is clicked
            checkbox.addItemListener {
                project.messageBus.syncPublisher(COVERAGE_SELECTION_TOGGLE_TOPIC)
                    .testGenerationResult(testCase.testName, checkbox.isSelected, editor)

                // Update the number of selected tests
                testsSelected -= (1 - 2 * checkbox.isSelected.compareTo(false))

                validateButton.isEnabled = testsSelected > 0

                updateTestsSelectedLabel()
            }

            // Add an editor to modify the test source code
            val document = EditorFactory.getInstance().createDocument(testCodeFormatted)
            val textFieldEditor = EditorTextField(document, project, JavaFileType.INSTANCE)
            // Set the default editor color to the one the editor was created with (only done once)
            if (defaultEditorColor == null) {
                defaultEditorColor = textFieldEditor.background
            }
            textFieldEditor.setOneLineMode(false)

            // Add test case title
            val middlePanel = JPanel()
            middlePanel.layout = BoxLayout(middlePanel, BoxLayout.Y_AXIS)

            val testCaseTitle = JLabel(testCase.testName)

            middlePanel.add(testCaseTitle)
            middlePanel.add(textFieldEditor)

            testCasePanel.add(middlePanel, BorderLayout.CENTER)

            // Create "Remove"  button to remove the test from cache
            val removeFromCacheButton = createRemoveButton(testCase, editor, testCasePanel)

            // Create "Reset" button to reset the changes in the source code of the test
            val resetButton = createResetButton(document, textFieldEditor, testCodeFormatted)

            // Enable reset button when editor is changed
            addListenerToTestDocument(document, resetButton, textFieldEditor, checkbox)

            // Add "Remove" and "Reset" buttons to the test case panel
            resetButton.addActionListener {
                WriteCommandAction.runWriteCommandAction(project) {
                    document.setText(testCodeFormatted)
                    resetButton.isEnabled = false
                    textFieldEditor.border = JBUI.Borders.empty()
                    textFieldEditor.editor!!.markupModel.removeAllHighlighters()
                }
            }
            val bottomPanel = JPanel()
            bottomPanel.layout = BoxLayout(bottomPanel, BoxLayout.Y_AXIS)
            val bottomButtons = JPanel()
            bottomButtons.layout = FlowLayout(FlowLayout.TRAILING)
            bottomButtons.add(removeFromCacheButton)
            bottomButtons.add(resetButton)
            bottomPanel.add(bottomButtons)
            bottomPanel.add(Box.createRigidArea(Dimension(0, 25)))
            testCasePanel.add(bottomPanel, BorderLayout.SOUTH)

            // Add panel to parent panel
            testCasePanel.maximumSize = Dimension(Short.MAX_VALUE.toInt(), Short.MAX_VALUE.toInt())
            allTestCasePanel.add(testCasePanel)
            testCasePanels[testCase.testName] = testCasePanel

            // Update the number of selected tests (all tests are selected by default)
            testsSelected = testCasePanels.size
            updateTestsSelectedLabel()
        }
    }

    /**
     * Highlight the mini-editor in the tool window whose name corresponds with the name of the test provided
     *
     * @param name name of the test whose editor should be highlighted
     */
    fun highlightTestCase(name: String) {
        val myPanel = testCasePanels[name] ?: return
        openToolWindowTab()
        scrollToPanel(myPanel)

        val editor = getEditor(name) ?: return
        if (!editor.background.equals(defaultEditorColor)) {
            return
        }
        val settingsProjectState = project.service<SettingsProjectService>().state
        val highlightColor =
            Color(settingsProjectState.colorRed, settingsProjectState.colorGreen, settingsProjectState.colorBlue, 30)
        editor.background = highlightColor
        returnOriginalEditorBackground(editor)
    }

    /**
     * Method to open the toolwindow tab with generated tests if not already open.
     */
    private fun openToolWindowTab() {
        val toolWindowManager = ToolWindowManager.getInstance(project).getToolWindow("TestGenie")
        contentManager = toolWindowManager!!.contentManager
        if (content != null) {
            toolWindowManager.show()
            toolWindowManager.contentManager.setSelectedContent(content!!)
        }
    }

    /**
     * Scrolls to the highlighted panel.
     *
     * @param myPanel the panel to scroll to
     */
    private fun scrollToPanel(myPanel: JPanel) {
        var sum = 0
        for (panel in testCasePanels.values) {
            if (panel == myPanel) {
                break
            } else {
                sum += panel.height
            }
        }
        val scroll = scrollPane.verticalScrollBar
        scroll.value = (scroll.minimum + scroll.maximum) * sum / allTestCasePanel.height
    }

    /**
     * Removes all coverage highlighting from the editor.
     */
    private fun removeAllHighlights() {
        val editor = project.service<Workspace>().editorForFileUrl(fileUrl)
        editor?.markupModel?.removeAllHighlighters()
    }

    /**
     * Reset the provided editors color to the default (initial) one after 10 seconds
     * @param editor the editor whose color to change
     */
    private fun returnOriginalEditorBackground(editor: EditorTextField) {
        Thread {
            Thread.sleep(10000)
            editor.background = defaultEditorColor
        }.start()
    }

    /**
     * Highlight tests failing dynamic validation
     *
     * @param names set of test names that fail
     */
    fun markFailingTestCases(names: Set<String>) {
        for (testCase in testCasePanels) {
            if (names.contains(testCase.key)) {
                val editor = getEditor(testCase.key) ?: return
                val highlightColor = Color(255, 0, 0, 90)
                defaultBorder = editor.border
                editor.border = BorderFactory.createLineBorder(highlightColor, 3)
            } else {
                val editor = getEditor(testCase.key) ?: return
                editor.border = JBUI.Borders.empty()
            }
        }
    }

    /**
     * Highlight a range of editors
     * @param names list of test names to pass to highlight function
     */
    fun highlightCoveredMutants(names: List<String>) {
        names.forEach {
            highlightTestCase(it)
        }
    }

    /**
     * Show a dialog where the user can select what test class the tests should be applied to,
     * and apply the selected tests to the test class.
     */
    private fun applyTests() {
        // Filter the selected test cases
        val selectedTestCasePanels = testCasePanels.filter { (it.value.getComponent(0) as JCheckBox).isSelected }
        val selectedTestCases = selectedTestCasePanels.map { it.key }

        // Get the test case components (source code of the tests)
        val testCaseComponents = selectedTestCases
            .map { getEditor(it)!! }
            .map { it.document.text }

        // Descriptor for choosing folders and java files
        val descriptor = FileChooserDescriptor(true, true, false, false, false, false)

        // Apply filter with folders and java files with main class
        descriptor.withFileFilter { file ->
            file.isDirectory || (
                file.extension?.lowercase(Locale.getDefault()) == "java" && (
                    PsiManager.getInstance(project).findFile(file!!) as PsiJavaFile
                    ).classes.stream().map { it.name }
                    .toList()
                    .contains(
                        (PsiManager.getInstance(project).findFile(file) as PsiJavaFile).name.removeSuffix(".java")
                    )
                )
        }

        val fileChooser = FileChooser.chooseFiles(
            descriptor,
            project,
            LocalFileSystem.getInstance().findFileByPath(project.basePath!!)
        )

        // Cancel button pressed
        if (fileChooser.isEmpty()) return

        // Chosen files by user
        val chosenFile = fileChooser[0]

        // Virtual file of a final java file
        var virtualFile: VirtualFile? = null
        // PsiClass of a final java file
        var psiClass: PsiClass? = null
        // PsiJavaFile of a final java file
        var psiJavaFile: PsiJavaFile? = null
        if (chosenFile.isDirectory) {
            // Input new file name
            var className: String
            while (true) {
                val jOptionPane =
                    JOptionPane.showInputDialog(
                        null,
                        TestGenieLabelsBundle.defaultValue("optionPaneMessage"),
                        TestGenieLabelsBundle.defaultValue("optionPaneTitle"),
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        null,
                        null,
                    )

                // Cancel button pressed
                jOptionPane ?: return

                // Get class name from user
                className = jOptionPane as String

                // Check the correctness of a class name
                if (Regex("[A-Z][a-zA-Z0-9]*[.java]?").matches(className)) {
                    break
                } else {
                    JOptionPane.showMessageDialog(
                        null,
                        TestGenieLabelsBundle.defaultValue("errorWindowMessage"),
                        TestGenieLabelsBundle.defaultValue("errorWindowTitle"),
                        JOptionPane.ERROR_MESSAGE
                    )
                }
            }

            // Create new file and set services of this file
            WriteCommandAction.runWriteCommandAction(project) {
                val fileName = "${className.split('.')[0]}.java"
                chosenFile.createChildData(null, fileName)
                val filePath = "${chosenFile.path}/$fileName"
                virtualFile = VirtualFileManager.getInstance().findFileByUrl("file://$filePath")!!
                psiJavaFile = (PsiManager.getInstance(project).findFile(virtualFile!!) as PsiJavaFile)
                psiClass = PsiElementFactory.getInstance(project).createClass(className)
                psiJavaFile!!.add(psiClass!!)
            }
        } else {
            // Set services of the chosen file
            virtualFile = chosenFile
            psiJavaFile = (PsiManager.getInstance(project).findFile(virtualFile!!) as PsiJavaFile)
            psiClass = psiJavaFile!!.classes[
                psiJavaFile!!.classes.stream().map { it.name }.toList()
                    .indexOf(psiJavaFile!!.name.removeSuffix(".java"))
            ]
        }

        // Add tests to the file
        WriteCommandAction.runWriteCommandAction(project) {
            appendTestsToClass(testCaseComponents, psiClass!!, psiJavaFile!!)
        }

        // Open the file after adding
        FileEditorManager.getInstance(project).openTextEditor(
            OpenFileDescriptor(project, virtualFile!!),
            true,
        )

        // The scheduled tests will be submitted in the background
        // (they will be checked every 5 minutes and also when the project is closed)
        scheduleTelemetry(selectedTestCases)

        // Remove the selected test cases from the cache and the tool window UI
        removeSelectedTestCases(selectedTestCasePanels)
    }

    private fun getActiveTests(): Set<String> {
        val selectedTestCases =
            testCasePanels.filter { (it.value.getComponent(0) as JCheckBox).isSelected }.map { it.key }

        return selectedTestCases.toSet()
    }

    /**
     * Retrieve the editor corresponding to a particular test case
     *
     * @param testCase the name of the test case
     * @return the editor corresponding to the test case, or null if it does not exist
     */
    private fun getEditor(testCase: String): EditorTextField? {
        val middlePanelComponent = testCasePanels[testCase]?.getComponent(1) ?: return null
        val middlePanel = middlePanelComponent as JPanel
        return middlePanel.getComponent(1) as EditorTextField
    }

    /**
     * Returns a pair of most-recent edit of selected tests, containing the test name and test code
     *
     * @return a pair of each test, containing the test name and test code
     */
    private fun getCurrentVersionsOfSelectedTests(): HashMap<String, String> {
        val selectedTestCases = getActiveTests()

        val lastEditsOfSelectedTestCases = selectedTestCases.associateWith {
            getEditor(it)!!.document.text
        }

        return HashMap(lastEditsOfSelectedTestCases)
    }

    /**
     * Validates the tests from the cache.
     */
    private fun validateTests() {
        val testJob = testJob ?: return
        val edits = getCurrentVersionsOfSelectedTests()
        validateButton.isEnabled = false
        toggleJacocoButton.isEnabled = false
        if (edits.isEmpty()) {
            showEmptyTests()
            return
        }

        Validator(project, testJob.info, edits).validateSuite()
    }

    private fun toggleJacocoCoverage() {
        val manager = CoverageDataManager.getInstance(project)
        val editor = project.service<Workspace>().editorForFileUrl(fileUrl)
        editor?.markupModel?.removeAllHighlighters()

        if (isJacocoCoverageActive) {
            manager.chooseSuitesBundle(null)
            isJacocoCoverageActive = false
        } else {
            currentJacocoCoverageBundle.let {
                ApplicationManager.getApplication().invokeLater {
                    manager.chooseSuitesBundle(currentJacocoCoverageBundle)
                    isJacocoCoverageActive = true
                }
            }
        }
    }

    /**
     * Toggles check boxes so that they are either all selected or all not selected,
     *  depending on the provided parameter.
     *
     *  @param selected whether the check boxes have to be selected or not
     */
    private fun toggleAllCheckboxes(selected: Boolean) {
        toggleJacocoButton.isEnabled = selected
        testCasePanels.forEach { (_, jPanel) ->
            val checkBox = jPanel.getComponent(0) as JCheckBox
            checkBox.isSelected = selected
        }
        testsSelected = if (selected) testCasePanels.size else 0
    }

    /**
     * Updates the label with the number selected tests.
     */
    private fun updateTestsSelectedLabel() {
        testsSelectedLabel.text = String.format(testsSelectedText, testsSelected, testCasePanels.size)
    }

    /**
     * Append the provided test cases to the provided class.
     *
     * @param testCaseComponents the test cases to be appended
     * @param selectedClass the class which the test cases should be appended to
     * @param outputFile the output file for tests
     */
    private fun appendTestsToClass(testCaseComponents: List<String>, selectedClass: PsiClass, outputFile: PsiJavaFile) {
        // block document
        PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(
            PsiDocumentManager.getInstance(project).getDocument(outputFile)!!,
        )

        // insert tests to a code
        testCaseComponents.forEach {
            PsiDocumentManager.getInstance(project).getDocument(outputFile)!!.insertString(
                selectedClass.rBrace!!.textRange.startOffset,
                // Fix Windows line separators
                it.replace("\r\n", "\n"),
            )
        }

        // insert imports to a code
        PsiDocumentManager.getInstance(project).getDocument(outputFile)!!.insertString(
            outputFile.importList?.startOffset ?: outputFile.packageStatement?.startOffset ?: 0,
            importsCode,
        )

        // insert package to a code
        outputFile.packageStatement ?: PsiDocumentManager.getInstance(project).getDocument(outputFile)!!
            .insertString(0, packageLine)
    }

    /**
     * Creates a new toolWindow tab for the coverage visualisation.
     */
    private fun createToolWindowTab() {
        // Remove generated tests tab from content manager if necessary
        val toolWindowManager = ToolWindowManager.getInstance(project).getToolWindow("TestGenie")
        contentManager = toolWindowManager!!.contentManager
        if (content != null) {
            contentManager!!.removeContent(content!!, true)
        }

        // If there is no generated tests tab, make it
        val contentFactory: ContentFactory = ContentFactory.getInstance()
        content = contentFactory.createContent(
            mainPanel,
            TestGenieLabelsBundle.defaultValue("generatedTests"),
            true,
        )
        contentManager!!.addContent(content!!)

        // Focus on generated tests tab and open toolWindow if not opened already
        contentManager!!.setSelectedContent(content!!)
        toolWindowManager.show()
    }

    /**
     * Closes the tool window and destroys the content of the tab.
     */
    private fun closeToolWindow() {
        contentManager!!.removeContent(content!!, true)
        ToolWindowManager.getInstance(project).getToolWindow("TestGenie")?.hide()
        val coverageVisualisationService = project.service<CoverageVisualisationService>()
        coverageVisualisationService.closeToolWindowTab()
    }

    /**
     * Creates a button to remove a test from the cache and from the UI.
     *
     * @param test the test case
     * @param editor the currently opened editor
     * @param testCasePanel the test case panel
     * @return the created button
     */
    private fun createRemoveButton(test: CompactTestCase, editor: Editor, testCasePanel: JPanel): JButton {
        val removeFromCacheButton = JButton("Remove")
        removeFromCacheButton.addActionListener {
            // Remove the highlighting of the test
            project.messageBus.syncPublisher(COVERAGE_SELECTION_TOGGLE_TOPIC)
                .testGenerationResult(test.testName, false, editor)

            // Update the number of selected test cases if necessary
            if ((testCasePanel.getComponent(0) as JCheckBox).isSelected) testsSelected -= 1

            // Remove the test case from the cache
            removeTestCase(test.testName)

            // Update the UI of the tool window tab
            allTestCasePanel.updateUI()
            updateTestsSelectedLabel()

            // If no more tests are remaining, close the tool window
            if (testCasePanels.size == 0) closeToolWindow()
        }
        return removeFromCacheButton
    }

    /**
     * Removes the selected tests from the cache, removes all the highlights from the editor and closes the tool window.
     * This function is called when the user clicks "Apply to test suite" button,
     *  and it is also called with all test cases as selected when the user clicks "Remove All" button.
     *
     * @param selectedTestCasePanels the panels of the selected tests
     */
    private fun removeSelectedTestCases(selectedTestCasePanels: Map<String, JPanel>) {
        selectedTestCasePanels.forEach { removeTestCase(it.key) }
        removeAllHighlights()
        closeToolWindow()
    }

    /**
     * Removes all test cases from the cache and tool window UI.
     */
    private fun removeAllTestCases() {
        // Ask the user for the confirmation
        val choice: Int = Messages.showYesNoCancelDialog(
            TestGenieBundle.message("removeAllMessage"),
            TestGenieBundle.message("confirmationTitle"),
            Messages.getQuestionIcon(),
        )
        // Cancel the operation if the user did not press "Yes"
        if (choice != 0) return

        // Remove the tests
        val testCasePanelsToRemove = testCasePanels.toMap()
        removeSelectedTestCases(testCasePanelsToRemove)
    }

    /**
     * A helper method to remove a test case from the cache and from the UI.
     *
     * @param testName the name of the test
     */
    private fun removeTestCase(testName: String) {
        // Remove the test from the cache
        project.service<TestCaseCachingService>().invalidateFromCache(fileUrl, originalTestCases[testName]!!)

        // Remove the test panel from the UI
        allTestCasePanel.remove(testCasePanels[testName])

        // Remove the test panel
        testCasePanels.remove(testName)
    }

    /**
     * Creates a button to reset the changes in the test source code.
     *
     * @param document the document with the test
     * @param textFieldEditor the text field editor with the test
     * @param testCode the source code of the test
     * @return the created button
     */
    private fun createResetButton(document: Document, textFieldEditor: EditorTextField, testCode: String): JButton {
        val resetButton = JButton(TestGenieLabelsBundle.defaultValue("resetButton"))
        resetButton.isEnabled = false
        resetButton.addActionListener {
            WriteCommandAction.runWriteCommandAction(project) {
                document.setText(testCode)
                resetButton.isEnabled = false
                textFieldEditor.border = JBUI.Borders.empty()
                textFieldEditor.editor!!.markupModel.removeAllHighlighters()
            }
        }
        return resetButton
    }

    /**
     * A helper method to add a listener to the test document (in the tool window panel)
     *   that enables reset button when the editor is changed.
     *
     * @param document the document of the test case
     * @param resetButton the button to reset changes in the test
     * @param textFieldEditor the text field editor with the test
     * @param checkbox the checkbox to select the test
     */
    private fun addListenerToTestDocument(
        document: Document,
        resetButton: JButton,
        textFieldEditor: EditorTextField,
        checkbox: JCheckBox,
    ) {
        document.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                resetButton.isEnabled = true

                // add border highlight
                val settingsProjectState = project.service<SettingsProjectService>().state
                val borderColor = Color(
                    settingsProjectState.colorRed,
                    settingsProjectState.colorGreen,
                    settingsProjectState.colorBlue,
                )
                textFieldEditor.border = BorderFactory.createLineBorder(borderColor)

                // add line highlighting
                if (event.newRange.startOffset + 1 >= document.textLength ||
                    event.newRange.endOffset >= document.textLength
                ) {
                    return
                }
                val newLine = event.newFragment.contains('\n')
                val startLine = document.getLineNumber(
                    event.newRange.startOffset +
                        (if (newLine) 1 else 0),
                )
                val endLine = document.getLineNumber(event.newRange.endOffset)
                for (lineNumber in startLine..endLine) {
                    textFieldEditor.editor!!.markupModel.addLineHighlighter(
                        if (newLine) DiffColors.DIFF_INSERTED else DiffColors.DIFF_MODIFIED,
                        lineNumber,
                        HighlighterLayer.FIRST,
                    )
                }

                // Highlight if line has been deleted
                if (event.oldFragment.contains('\n')) {
                    textFieldEditor.editor!!.markupModel.addLineHighlighter(
                        DiffColors.DIFF_MODIFIED,
                        endLine,
                        HighlighterLayer.FIRST,
                    )
                }

                // select checkbox
                checkbox.isSelected = true
            }
        })
    }

    /**
     * Method to show notification that there are no tests to verify
     */
    private fun showEmptyTests() {
        NotificationGroupManager.getInstance().getNotificationGroup("Test Validation Error").createNotification(
            TestGenieBundle.message("emptyTestCasesTitle"),
            TestGenieBundle.message("emptyTestCasesText"),
            NotificationType.ERROR,
        ).notify(project)
    }

    /**
     * Schedules the telemetry for the selected and modified tests.
     *
     * @param selectedTestCases the test cases selected by the user
     */
    private fun scheduleTelemetry(selectedTestCases: List<String>) {
        val telemetryService = project.service<TestGenieTelemetryService>()
        telemetryService.scheduleTestCasesForTelemetry(
            selectedTestCases.map {
                val modified = getEditor(it)!!.text
                val original = originalTestCases[it]!!

                TestGenieTelemetryService.ModifiedTestCase(original, modified)
            }.filter { it.modified != it.original },
        )
    }

    /**
     * Display the button to actually invoke EvoSuite if the tests are cached.
     */
    private fun displayLazyRunnerButton() {
        cacheLazyPipeline ?: return

        val lazyRunnerPanel = JPanel()
        lazyRunnerPanel.layout = BoxLayout(lazyRunnerPanel, BoxLayout.Y_AXIS)
        val lazyRunnerLabel = JLabel("Showing previously generated test cases from the cache.")
        lazyRunnerLabel.alignmentX = Component.CENTER_ALIGNMENT
        lazyRunnerPanel.add(lazyRunnerLabel)

        val lazyRunnerButton = JButton("Generate new tests")

        lazyRunnerButton.addActionListener {
            lazyRunnerButton.isEnabled = false
            cacheLazyPipeline!!
                .withoutCache()
                .runTestGeneration()
        }

        lazyRunnerButton.alignmentX = Component.CENTER_ALIGNMENT
        lazyRunnerPanel.add(lazyRunnerButton)

        allTestCasePanel.add(Box.createRigidArea(Dimension(0, 50)))
        allTestCasePanel.add(lazyRunnerPanel)
        allTestCasePanel.add(Box.createRigidArea(Dimension(0, 50)))
    }
}
