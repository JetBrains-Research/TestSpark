package nl.tudelft.ewi.se.ciselab.testgenie.services

import com.google.gson.Gson
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.ide.util.TreeClassChooserFactory
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDocumentManager
import com.intellij.ui.EditorTextField
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import nl.tudelft.ewi.se.ciselab.testgenie.editor.Workspace
import org.evosuite.utils.CompactReport
import org.evosuite.utils.CompactTestCase
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JPanel
import kotlin.collections.HashMap

class TestCaseDisplayService(private val project: Project) {

    private val mainPanel: JPanel = JPanel()
    private val applyButton: JButton = JButton("Apply")
    private val allTestCasePanel: JPanel = JPanel()
    private val scrollPane: JBScrollPane = JBScrollPane(allTestCasePanel)
    private var testCasePanels: HashMap<String, JPanel> = HashMap()
    private var testCheckboxes: MutableList<Pair<JCheckBox, String>> = mutableListOf()
    private var unhighlightList: MutableList<String> = mutableListOf()
    private val originalCompactReports: HashMap<String, CompactReport> = HashMap()

    // Variable to keep reference to the coverage visualisation content
    private var content: Content? = null

    init {
        allTestCasePanel.layout = BoxLayout(allTestCasePanel, BoxLayout.Y_AXIS)
        mainPanel.layout = BorderLayout()
        applyButton.addActionListener { applyTests() }
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
    private fun displayTestCases(testReport: CompactReport) {
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
            testCheckboxes.add(Pair(checkbox, it.testName))

            val document = EditorFactory.getInstance().createDocument(testCodeFormatted)
            val editor = EditorTextField(document, project, JavaFileType.INSTANCE)

            editor.setOneLineMode(false)

            testCasePanel.add(editor, BorderLayout.CENTER)

            testCasePanel.maximumSize = Dimension(Short.MAX_VALUE.toInt(), Short.MAX_VALUE.toInt())
            allTestCasePanel.add(testCasePanel)
            testCasePanels[testName] = testCasePanel
            allTestCasePanel.add(Box.createRigidArea(Dimension(0, 5)))
        }
        applyListenersToCheckBoxes(testReport)
    }

    /**
     * Highlight the mini-editor in the toolwindow whose name corresponds with the name of the test provided
     *
     * @param name name of the test whose editor should be highlighted
     */
    fun highlight(name: String) {
        if (unhighlightList.contains(name)) {
            return
        }
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
     * Applies the same listener to all checkboxes
     * Logic found in addCheckboxListener
     * @param report compactReport with tests
     */
    private fun applyListenersToCheckBoxes(report: CompactReport) {
        testCheckboxes.forEach {
            val checkbox = it.first
            val testName = it.second
            checkbox.addActionListener {
                addCheckboxListener(checkbox, testName, report)(it)
            }
        }
    }

    /**
     * Adds listener to checkbox
     * If a test is selected, the lines it covers will be highlighted. Otherwise they will not.
     * Works by replicating the original file within the Workspace, then changing its values and placing the new version in Workspace
     * @param checkbox the checkbox to add listener to
     * @param testName the name of the test to add/remove from unhighlightList
     * @param reportFetched the compactReport with the tests
     */
    private fun addCheckboxListener(checkbox: JCheckBox, testName: String, reportFetched: CompactReport): (ActionEvent) -> Unit = {
        if (checkbox.isSelected) {
            unhighlightList.remove(testName)
        } else {
            unhighlightList.add(testName)
        }

        val originalResult = project.service<Workspace>().getTestGenerationResult(reportFetched)

        var report = reportFetched

        // Always use the root report. That's the report with all the tests still in it
        // Otherwise, we will lose information if we deselect multiple tests
        if (originalCompactReports.containsKey(originalResult.first)) {
            // Deep copy required
            report = deepCopy(originalCompactReports.get(originalResult.first) ?: reportFetched)
        } else {
            originalCompactReports.put(originalResult.first, deepCopy(reportFetched))
        }

        // copy report
        val reportNew: CompactReport = report

        val removedNames = mutableListOf<Pair<String, CompactTestCase?>>()

        // remove tests which are not selected
        unhighlightList.forEach {
            val test = reportNew.testCaseList.remove(it)
            removedNames.add(Pair(it, test))
        }
        val coveredLinesNew: MutableSet<Int> = mutableSetOf()

        // only count the covered lines of selected tests
        report.testCaseList.forEach {
            coveredLinesNew.addAll(it.value.coveredLines)
        }

        reportNew.allCoveredLines = coveredLinesNew

        // replace original file in workplace
        project.service<Workspace>().addPendingResult(originalResult.first, originalResult.second)
        project.service<Workspace>().receiveGenerationResult(originalResult.first, reportNew)
    }

    /**
     * Makes a deep copy of the compact report
     * Credits to https://medium.com/@gugabernardo/here-is-the-easiest-way-to-deep-copy-an-object-in-kotlin-cf200c2130b
     * @param report the compactReport to make the copy of
     * @return copy of compactReport
     */
    private fun deepCopy(report: CompactReport): CompactReport {
        val gson = Gson()
        val json = gson.toJson(report)
        return gson.fromJson(json, CompactReport::class.java)
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
        chooser.showDialog()

        // get selected class or return if no class was selected
        val selectedClass = chooser.selected ?: return

        // insert test case components into selected class
        appendTestsToClass(testCaseComponents, selectedClass)
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
