package nl.tudelft.ewi.se.ciselab.testgenie.services

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.ide.util.TreeClassChooserFactory
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.diff.DiffColors
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDocumentManager
import com.intellij.refactoring.suggested.newRange
import com.intellij.ui.EditorTextField
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.JBUI
import org.evosuite.utils.CompactReport
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JPanel

class TestCaseDisplayService(private val project: Project) {

    private val mainPanel: JPanel = JPanel()
    private val applyButton: JButton = JButton("Apply to test suite")
    private val validateButton: JButton = JButton("Validate tests")
    private val selectAllButton: JButton = JButton("Select All")
    private val deselectAllButton: JButton = JButton("Deselect All")

    private val allTestCasePanel: JPanel = JPanel()
    private val scrollPane: JBScrollPane = JBScrollPane(
        allTestCasePanel,
        JBScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
        JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER
    )
    private var testCasePanels: HashMap<String, JPanel> = HashMap()
    private var originalTestCases: HashMap<String, String> = HashMap()

    // Variable to keep reference to the coverage visualisation content
    private var content: Content? = null

    var fileUrl: String = ""
        get() = field
        set(newFileUrl) {
            field = newFileUrl
        }

    init {
        allTestCasePanel.layout = BoxLayout(allTestCasePanel, BoxLayout.Y_AXIS)
        mainPanel.layout = BorderLayout()

        val topButtons = JPanel()
        topButtons.layout = FlowLayout(FlowLayout.TRAILING)
        topButtons.add(validateButton)
        topButtons.add(selectAllButton)
        topButtons.add(deselectAllButton)
        mainPanel.add(topButtons, BorderLayout.NORTH)

        mainPanel.add(scrollPane, BorderLayout.CENTER)
        mainPanel.add(applyButton, BorderLayout.SOUTH)

        applyButton.addActionListener { applyTests() }
        validateButton.addActionListener { validateTests() }
        selectAllButton.addActionListener { toggleAllCheckboxes(true) }
        deselectAllButton.addActionListener { toggleAllCheckboxes(false) }
    }

    /**
     * Creates the complete panel in the "Generated Tests" tab,
     * and adds the "Generated Tests" tab to the sidebar tool window.
     */
    fun showGeneratedTests(testReport: CompactReport, editor: Editor) {
        displayTestCases(testReport, editor)
        createToolWindowTab()
    }

    /**
     * Fill the panel with the generated test cases. Remove all previously shown test cases.
     * Add Tests and their names to a List of pairs (used for highlighting)
     *
     * @param testReport The report from which each testcase should be displayed
     */
    private fun displayTestCases(testReport: CompactReport, editor: Editor) {
        allTestCasePanel.removeAll()
        testCasePanels.clear()
        testReport.testCaseList.values.forEach {
            val testCode = it.testCode
            val testName = it.testName
            val testCasePanel = JPanel()
            testCasePanel.layout = BorderLayout()

            // fix Windows line separators
            val testCodeFormatted = testCode.replace("\r\n", "\n")

            originalTestCases[testName] = testCodeFormatted

            // Add checkbox
            val checkbox = JCheckBox()
            checkbox.isSelected = true
            testCasePanel.add(checkbox, BorderLayout.WEST)

            // Toggle coverage when checkbox is clicked
            checkbox.addItemListener {
                project.messageBus.syncPublisher(COVERAGE_SELECTION_TOGGLE_TOPIC)
                    .testGenerationResult(testName, checkbox.isSelected, editor)
            }

            // Add editor
            val document = EditorFactory.getInstance().createDocument(testCodeFormatted)
            val textFieldEditor = EditorTextField(document, project, JavaFileType.INSTANCE)
            textFieldEditor.setOneLineMode(false)
            testCasePanel.add(textFieldEditor, BorderLayout.CENTER)

            // Add top buttons
            val topButtons = JPanel()
            topButtons.layout = FlowLayout(FlowLayout.TRAILING)

            // Add "Remove From Cache"  button
            val removeFromCacheButton = JButton("Remove From Cache")
            removeFromCacheButton.addActionListener {
                // TODO: Update gutters when you remove tests from cache
                val cache = project.service<TestCaseCachingService>()
                cache.invalidateFromCache(fileUrl, testCode)

                //
                project.messageBus.syncPublisher(COVERAGE_SELECTION_TOGGLE_TOPIC)
                    .testGenerationResult(testName, false, editor)

                testCasePanels.remove(testName)

                allTestCasePanel.remove(testCasePanel)
                allTestCasePanel.updateUI()
            }

            // Add "Reset" button
            val resetButton = JButton("Reset")
            resetButton.isEnabled = false
            resetButton.addActionListener {
                WriteCommandAction.runWriteCommandAction(project) {
                    document.setText(testCodeFormatted)
                    resetButton.isEnabled = false
                    textFieldEditor.border = JBUI.Borders.empty()
                    textFieldEditor.editor!!.markupModel.removeAllHighlighters()
                }
            }
            // enable reset button when editor is changed
            document.addDocumentListener(object : DocumentListener {
                override fun documentChanged(event: DocumentEvent) {
                    resetButton.isEnabled = true

                    // add border highlight
                    val service = TestGenieSettingsService.getInstance().state
                    val borderColor = Color(service!!.colorRed, service.colorGreen, service.colorBlue)
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
                            (if (newLine) 1 else 0)
                    )
                    val endLine = document.getLineNumber(event.newRange.endOffset)
                    for (lineNumber in startLine..endLine) {
                        textFieldEditor.editor!!.markupModel.addLineHighlighter(
                            if (newLine) DiffColors.DIFF_INSERTED else DiffColors.DIFF_MODIFIED,
                            lineNumber,
                            HighlighterLayer.FIRST
                        )
                    }

                    // Highlight if line has been deleted
                    if (event.oldFragment.contains('\n')) {
                        textFieldEditor.editor!!.markupModel.addLineHighlighter(
                            DiffColors.DIFF_MODIFIED,
                            endLine,
                            HighlighterLayer.FIRST
                        )
                    }

                    // select checkbox
                    checkbox.isSelected = true
                }
            })
            topButtons.add(removeFromCacheButton)
            topButtons.add(resetButton)
            testCasePanel.add(topButtons, BorderLayout.NORTH)

            // Add panel to parent panel
            testCasePanel.maximumSize = Dimension(Short.MAX_VALUE.toInt(), Short.MAX_VALUE.toInt())
            allTestCasePanel.add(testCasePanel)
            testCasePanels[testName] = testCasePanel
            allTestCasePanel.add(Box.createRigidArea(Dimension(0, 5)))
        }
    }

    /**
     * Highlight the mini-editor in the toolwindow whose name corresponds with the name of the test provided
     *
     * @param name name of the test whose editor should be highlighted
     */
    fun highlightTestCase(name: String) {
        val editor = testCasePanels[name]!!.getComponent(1) as EditorTextField
        val backgroundDefault = editor.background
        val service = TestGenieSettingsService.getInstance().state
        val highlightColor = Color(service!!.colorRed, service.colorGreen, service.colorBlue, 30)
        editor.background = highlightColor
        Thread {
            Thread.sleep(10000)
            editor.background = backgroundDefault
        }.start()
    }

    /**
     * Show a dialog where the user can select what test class the tests should be applied to,
     * and apply the selected tests to the test class.
     */
    private fun applyTests() {
        val selectedTestCases = testCasePanels.filter { (it.value.getComponent(0) as JCheckBox).isSelected }
            .map { it.key }

        val testCaseComponents = selectedTestCases.map {
            testCasePanels[it]!!.getComponent(1) as EditorTextField
        }.map {
            it.document.text
        }

        // show chooser dialog to select test file
        val chooser = TreeClassChooserFactory.getInstance(project)
            .createProjectScopeChooser(
                "Insert Test Cases into Class"
            )

        // Warning: The following code is extremely cursed.
        // It is a workaround for an oversight in the IntelliJ TreeJavaClassChooserDialog.
        // This is necessary in order to set isShowLibraryContents to false in
        // the AbstractTreeClassChooserDialog (parent of the TreeJavaClassChooserDialog).
        // If this is not done, the user can pick a non-project class (e.g. a class from a library).
        // See https://github.com/ciselab/TestGenie/issues/102
        // TODO: In the future, this should be replaced with a custom dialog (which can also create new classes).
        try {
            val showLibraryContentsField = chooser.javaClass.superclass.getDeclaredField("myIsShowLibraryContents")
            showLibraryContentsField.isAccessible = true
            showLibraryContentsField.set(chooser, false)
        } catch (_: Exception) {
            // could not set field
            // ignoring the exception is acceptable as this part is not critical
        }

        chooser.showDialog()

        // get selected class or return if no class was selected
        val selectedClass = chooser.selected ?: return

        // insert test case components into selected class
        appendTestsToClass(testCaseComponents, selectedClass)

        // schedule telemetry
        val telemetryService = ApplicationManager.getApplication().getService(TestGenieTelemetryService::class.java)
        telemetryService.scheduleTestCasesForTelemetry(
            selectedTestCases.map {
                val modified = (testCasePanels[it]!!.getComponent(1) as EditorTextField).text
                val original = originalTestCases[it]!!

                TestGenieTelemetryService.ModifiedTestCase(
                    original,
                    modified
                )
            }.filter {
                it.modified != it.original
            }
        )

        // The scheduled tests will be submitted in the background
        // (they will be checked every 5 minutes and also when the project is closed)
    }

    private fun validateTests() {}

    private fun toggleAllCheckboxes(selected: Boolean) {
        testCasePanels.forEach { (_, jPanel) ->
            val checkBox = jPanel.getComponent(0) as JCheckBox
            checkBox.isSelected = selected
        }
    }

    /**
     * Append the provided test cases to the provided class.
     *
     * @param testCaseComponents the test cases to be appended
     * @param selectedClass the class which the test cases should be appended to
     */
    private fun appendTestsToClass(testCaseComponents: List<String>, selectedClass: PsiClass) {
        WriteCommandAction.runWriteCommandAction(project) {
            testCaseComponents.forEach {
                PsiDocumentManager.getInstance(project)
                    .getDocument(selectedClass.containingFile)!!
                    .insertString(
                        selectedClass.rBrace!!.textRange.startOffset,
                        // Fix Windows line separators
                        it.replace("\r\n", "\n")
                    )
            }
        }
    }

    /**
     * Creates a new toolWindow tab for the coverage visualisation.
     */
    private fun createToolWindowTab() {

        // Remove generated tests tab from content manager if necessary
        val toolWindowManager = ToolWindowManager.getInstance(project).getToolWindow("TestGenie")
        val contentManager = toolWindowManager!!.contentManager
        if (content != null) {
            contentManager.removeContent(content!!, true)
        }

        // If there is no generated tests tab, make it
        val contentFactory: ContentFactory = ContentFactory.SERVICE.getInstance()
        content = contentFactory.createContent(
            mainPanel, "Generated Tests", true
        )
        contentManager.addContent(content!!)

        // Focus on generated tests tab and open toolWindow if not opened already
        contentManager.setSelectedContent(content!!)
        toolWindowManager.show()
    }
}
