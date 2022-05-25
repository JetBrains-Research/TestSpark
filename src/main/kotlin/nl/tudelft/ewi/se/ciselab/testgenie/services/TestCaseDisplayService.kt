package nl.tudelft.ewi.se.ciselab.testgenie.services

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.ide.util.TreeClassChooserFactory
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDocumentManager
import com.intellij.ui.EditorTextField
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import org.evosuite.utils.CompactReport
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JPanel
import kotlin.collections.HashMap

class TestCaseDisplayService(private val project: Project) {

    private val mainPanel: JPanel = JPanel()
    private val applyButton: JButton = JButton("Apply to test suite")
    private val validateButton: JButton = JButton("Validate tests")
    private val allTestCasePanel: JPanel = JPanel()
    private val scrollPane: JBScrollPane = JBScrollPane(allTestCasePanel)
    private var testCasePanels: HashMap<String, JPanel> = HashMap()

    // Variable to keep reference to the coverage visualisation content
    private var content: Content? = null

    init {
        allTestCasePanel.layout = BoxLayout(allTestCasePanel, BoxLayout.Y_AXIS)
        mainPanel.layout = BorderLayout()

        val buttons = JPanel()
        buttons.add(applyButton)
        buttons.add(validateButton)

        mainPanel.add(buttons, BorderLayout.SOUTH)

        mainPanel.add(scrollPane, BorderLayout.CENTER)

        applyButton.addActionListener { applyTests() }
        validateButton.addActionListener { validateTests() }
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

            val checkbox = JCheckBox()
            checkbox.isSelected = true
            testCasePanel.add(checkbox, BorderLayout.WEST)

            checkbox.addActionListener {
                project.messageBus.syncPublisher(COVERAGE_SELECTION_TOGGLE_TOPIC)
                    .testGenerationResult(testName, checkbox.isSelected, editor)
            }

            val document = EditorFactory.getInstance().createDocument(testCodeFormatted)
            val textFieldEditor = EditorTextField(document, project, JavaFileType.INSTANCE)

            textFieldEditor.setOneLineMode(false)

            testCasePanel.add(textFieldEditor, BorderLayout.CENTER)

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
    }

    private fun validateTests() {}

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
