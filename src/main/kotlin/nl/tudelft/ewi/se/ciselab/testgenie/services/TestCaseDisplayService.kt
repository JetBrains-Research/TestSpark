package nl.tudelft.ewi.se.ciselab.testgenie.services

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaCodeFragmentFactory
import com.intellij.psi.PsiDocumentManager
import com.intellij.ui.EditorTextField
import com.intellij.ui.components.JBScrollPane
import org.evosuite.utils.CompactReport
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import javax.swing.*

class TestCaseDisplayService(private val project: Project) {

    val mainPanel: JPanel = JPanel()
    private val applyButton: JButton = JButton("Apply")
    private val allTestCasePanel: JPanel = JPanel()
    private val scrollPane: JBScrollPane = JBScrollPane(allTestCasePanel)
    private var editorList: MutableList<Pair<String, EditorTextField>> = arrayListOf()

    init {
        allTestCasePanel.layout = BoxLayout(allTestCasePanel, BoxLayout.Y_AXIS)
        mainPanel.layout = BorderLayout()
        mainPanel.add(applyButton, BorderLayout.SOUTH)
        mainPanel.add(scrollPane, BorderLayout.CENTER)
    }

    /**
     * Fill the panel with the generated test cases. Remove all previously shown test cases.
     * Add Tests and their names to a List of pairs (used for highlighting)
     *
     * @param testReport The report from which each testcase should be displayed
     */
    fun displayTestCases(testReport: CompactReport) {
        allTestCasePanel.removeAll()
        testReport.testCaseList.values.forEach {
            val testCode = it.testCode
            val testName = it.testName
            val testCasePanel = JPanel()
            testCasePanel.layout = BorderLayout()

            val checkbox = JCheckBox()
            checkbox.isSelected = true
            testCasePanel.add(checkbox, BorderLayout.WEST)

            val code = JavaCodeFragmentFactory.getInstance(project)
                    .createExpressionCodeFragment(testCode, null, null, true)
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
                editor.background = Color(100, 150, 20, 30)
                Thread {
                    Thread.sleep(10000)
                    editor.background = backgroundDefault
                }.start()
                return
            }
        }
    }
}
