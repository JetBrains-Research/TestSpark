package nl.tudelft.ewi.se.ciselab.testgenie.services

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.ui.EditorTextField
import com.intellij.ui.components.JBScrollPane
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
    private var testCasePanels: HashMap<String, JPanel> = HashMap()
    private val highlightColor: Color = Color(100, 150, 20, 30)

    init {
        allTestCasePanel.layout = BoxLayout(allTestCasePanel, BoxLayout.Y_AXIS)
        mainPanel.layout = BorderLayout()
        applyButton.addActionListener { applyTests() }
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
        testCasePanels.clear()
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

            editor.setOneLineMode(false)
            editor.isViewer = true

            testCasePanel.add(editor, BorderLayout.CENTER)

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
    fun highlight(name: String) {
        val editor = testCasePanels[name]!!.getComponent(1) as EditorTextField
        val backgroundDefault = editor.background
        editor.background = highlightColor
        Thread {
            Thread.sleep(10000)
            editor.background = backgroundDefault
        }.start()
    }

    private fun applyTests() {
        val selectedTestCases = testCasePanels.filter { (it.value.getComponent(0) as JCheckBox).isSelected }
            .map { it.key }


        val testCaseComponents = selectedTestCases.map {
            testCasePanels[it]!!.getComponent(1) as EditorTextField
        }.map {
//            val code = it.document.text
//            JavaCodeFragmentFactory.getInstance(project)
//                .createExpressionCodeFragment(code, null, null, true)
            it.document.text
        }

        println(testCaseComponents)

//        val testSuiteFile = PsiFileFactory.getInstance(project)
//            .createFileFromText("test_suite.java", JavaFileType.INSTANCE, testSuiteClass) as PsiJavaFile
//        val testSuiteFileDirectory = testSuiteFile.containingDirectory
//        val testSuiteFileDirectoryVirtualFile = testSuiteFileDirectory.virtualFile
//        val testSuiteFileDirectoryPsiDirectory =
//            PsiManager.getInstance(project).findDirectory(testSuiteFileDirectoryVirtualFile)!!
//        val testSuiteFilePsiDirectory = testSuiteFileDirectoryPsiDirectory.findSubdirectory("test")!!
//        val testSuiteFilePsiFile = testSuiteFilePsiDirectory.findFile("TestSuite.java")!!
//        val testSuiteFilePsiFileVirtualFile = testSuiteFilePsiFile.virtualFile
//        val testSuiteFilePsiFilePsiFile = PsiManager.getInstance(project).findFile(testSuiteFilePsiFileVirtualFile)!!
//        val testSuiteFilePsiFilePsiDirectory = testSuiteFilePsiFilePsiFile.parent!!
//        val testSuiteFilePsiFilePsiFileDirectoryVirtualFile = testSuiteFilePsiFilePsiDirectory.virtualFile
//        val testSuiteFilePsiFilePsiFileDirectoryPsiDirectory =
//            PsiManager.getInstance(project).findDirectory(testSuiteFilePsiFilePsiDirectory.virtualFile)!!
    }
}