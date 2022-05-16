package nl.tudelft.ewi.se.ciselab.testgenie.services

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.JavaCodeFragmentFactory
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

class TestCaseDisplayService(private val project: Project) {

    val mainPanel: JPanel = JPanel()
    private val applyButton: JButton = JButton("Apply")
    private val allTestCasePanel: JPanel = JPanel()
    private val scrollPane: JBScrollPane = JBScrollPane(allTestCasePanel)
    private var editorList: MutableList<Pair<String, EditorTextField>> = arrayListOf()
    private val highlightColor: Color = Color(100, 150, 20, 30)

    // Variable to keep reference to the coverage visualisation content
    private var content: Content? = null

    init {
        allTestCasePanel.layout = BoxLayout(allTestCasePanel, BoxLayout.Y_AXIS)
        mainPanel.layout = BorderLayout()
        mainPanel.add(applyButton, BorderLayout.SOUTH)
        mainPanel.add(scrollPane, BorderLayout.CENTER)
    }

    /**
     * Creates the complete panel in the "Generated Tests" tab,
     * and adds the "Generated Tests" tab to the sidebar tool window.
     */
    fun showGeneratedTests(testReport: CompactReport) {
        displayTestCases(testReport)
        createToolWindowTab()
    }

    /**
     * Fill the panel with the generated test cases. Remove all previously shown test cases.
     * Add Tests and their names to a List of pairs (used for highlighting)
     *
     * @param testReport The report from which each testcase should be displayed
     */
    fun displayTestCases(testReport: CompactReport) {
        allTestCasePanel.removeAll()
        editorList = arrayListOf()
        testReport.testCaseList.values.forEach {
            val testCode = it.testCode
            val testName = it.testName
            val testCasePanel = JPanel()
            testCasePanel.layout = BorderLayout()

            val checkbox = JCheckBox()
            checkbox.isSelected = true
            testCasePanel.add(checkbox, BorderLayout.WEST)

            val code =
                JavaCodeFragmentFactory.getInstance(project).createExpressionCodeFragment(testCode, null, null, true)
            val document = PsiDocumentManager.getInstance(project).getDocument(code)
            val editor = EditorTextField(document, project, JavaFileType.INSTANCE)
            editorList.add(Pair(testName, editor))

            editor.setOneLineMode(false)
            editor.isViewer = true

            testCasePanel.add(editor, BorderLayout.CENTER)

            testCasePanel.maximumSize = Dimension(Short.MAX_VALUE.toInt(), Short.MAX_VALUE.toInt())
            allTestCasePanel.add(testCasePanel)
            allTestCasePanel.add(Box.createRigidArea(Dimension(0, 5)))
        }
    }

    /**
     * Highlight the mini-editor in the toolwindow whose name corresponds with the name of the test provided
     *
     * @param name name of the test whose editor should be highlighted
     */
    fun highlight(name: String) {
        for (i in editorList) {
            val testCase = i.first
            if (testCase == name) {
                val editor = i.second
                val backgroundDefault = editor.background
                editor.background = highlightColor
                Thread {
                    Thread.sleep(10000)
                    editor.background = backgroundDefault
                }.start()
                return
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
