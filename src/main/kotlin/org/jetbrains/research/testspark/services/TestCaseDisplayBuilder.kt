package org.jetbrains.research.testspark.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.EditorTextField
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.content.ContentManager
import org.jetbrains.research.testspark.bundles.plugin.PluginLabelsBundle
import org.jetbrains.research.testspark.bundles.plugin.PluginSettingsBundle
import org.jetbrains.research.testspark.core.data.Report
import org.jetbrains.research.testspark.core.data.TestCase
import org.jetbrains.research.testspark.core.test.SupportedLanguage
import org.jetbrains.research.testspark.data.UIContext
import org.jetbrains.research.testspark.display.generatedTests.TestCasePanelFactory
import org.jetbrains.research.testspark.display.generatedTests.TopButtonsPanelFactory
import org.jetbrains.research.testspark.display.java.JavaDisplayUtils
import org.jetbrains.research.testspark.display.kotlin.KotlinDisplayUtils
import org.jetbrains.research.testspark.display.template.DisplayUtils
import org.jetbrains.research.testspark.display.utils.ReportUpdater
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JSeparator
import javax.swing.SwingConstants
import kotlin.collections.HashMap
import org.jetbrains.research.testspark.display.coverage.CoverageVisualisationTabBuilder

@Service(Service.Level.PROJECT)
class TestCaseDisplayBuilder(private val project: Project) {
    private var report: Report? = null

    private val unselectedTestCases = HashMap<Int, TestCase>()

    private var mainPanel: JPanel = JPanel()

    private val topButtonsPanelFactory = TopButtonsPanelFactory(project)

    private var applyButton: JButton = JButton(PluginLabelsBundle.get("applyButton"))

    private var allTestCasePanel: JPanel = JPanel()

    private var scrollPane: JBScrollPane = JBScrollPane(
        allTestCasePanel,
        JBScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
        JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER,
    )

    private var testCasePanels: HashMap<String, JPanel> = HashMap()

    private var testsSelected: Int = 0

    /**
     * Default color for the editors in the tool window
     */
    private var defaultEditorColor: Color? = null

    /**
     * Content Manager to be able to add / remove tabs from tool window
     */
    private var contentManager: ContentManager? = null

    /**
     * Variable to keep reference to the coverage visualisation content
     */
    private var content: Content? = null

    private var uiContext: UIContext? = null

    private var displayUtils: DisplayUtils? = null

    private var coverageVisualisationTabBuilder: CoverageVisualisationTabBuilder? = null

    init {
        allTestCasePanel.layout = BoxLayout(allTestCasePanel, BoxLayout.Y_AXIS)
        mainPanel.layout = BorderLayout()

        mainPanel.add(topButtonsPanelFactory.getPanel(), BorderLayout.NORTH)
        mainPanel.add(scrollPane, BorderLayout.CENTER)

        applyButton.isOpaque = false
        applyButton.isContentAreaFilled = false
        mainPanel.add(applyButton, BorderLayout.SOUTH)

        applyButton.addActionListener { applyTests() }
    }

    fun displayTestCases(report: Report, uiContext: UIContext, language: SupportedLanguage) {
        this.report = report
        this.uiContext = uiContext

        coverageVisualisationTabBuilder = CoverageVisualisationTabBuilder(project)

        coverageVisualisationTabBuilder!!.showCoverage(report)

        displayUtils = when (language) {
            SupportedLanguage.Java -> {
                JavaDisplayUtils()
            }

            SupportedLanguage.Kotlin -> {
                KotlinDisplayUtils()
            }
        }

        val editor = project.service<EditorService>().editor!!

        allTestCasePanel.removeAll()
        testCasePanels.clear()

        addSeparator()

        // TestCasePanelFactories array
        val testCasePanelFactories = arrayListOf<TestCasePanelFactory>()

        report.testCaseList.values.forEach {
            val testCase = it
            val testCasePanel = JPanel()
            testCasePanel.layout = BorderLayout()

            // Add a checkbox to select the test
            val checkbox = JCheckBox()
            checkbox.isSelected = true
            checkbox.addItemListener {
                // Update the number of selected tests
                testsSelected -= (1 - 2 * checkbox.isSelected.compareTo(false))

                if (checkbox.isSelected) {
                    ReportUpdater.selectTestCase(report, unselectedTestCases, testCase.id,
                        coverageVisualisationTabBuilder!!
                    )
                } else {
                    ReportUpdater.unselectTestCase(report, unselectedTestCases, testCase.id,
                        coverageVisualisationTabBuilder!!
                    )
                }

                updateUI()
            }
            testCasePanel.add(checkbox, BorderLayout.WEST)

            val testCasePanelFactory =
                TestCasePanelFactory(project, language, testCase, editor, checkbox, uiContext, report,
                    coverageVisualisationTabBuilder!!
                )
            testCasePanel.add(testCasePanelFactory.getUpperPanel(), BorderLayout.NORTH)
            testCasePanel.add(testCasePanelFactory.getMiddlePanel(), BorderLayout.CENTER)
            testCasePanel.add(testCasePanelFactory.getBottomPanel(), BorderLayout.SOUTH)

            testCasePanelFactories.add(testCasePanelFactory)

            testCasePanel.add(Box.createRigidArea(Dimension(12, 0)), BorderLayout.EAST)

            // Add panel to parent panel
            testCasePanel.maximumSize = Dimension(Short.MAX_VALUE.toInt(), Short.MAX_VALUE.toInt())
            allTestCasePanel.add(testCasePanel)
            addSeparator()
            testCasePanels[testCase.testName] = testCasePanel
        }

        // Update the number of selected tests (all tests are selected by default)
        testsSelected = testCasePanels.size

        topButtonsPanelFactory.setTestCasePanelFactoriesArray(testCasePanelFactories)
        topButtonsPanelFactory.updateTopLabels()

        createToolWindowTab()
    }

    private fun addSeparator() {
        allTestCasePanel.add(Box.createRigidArea(Dimension(0, 10)))
        allTestCasePanel.add(JSeparator(SwingConstants.HORIZONTAL))
        allTestCasePanel.add(Box.createRigidArea(Dimension(0, 10)))
    }

    fun highlightTestCase(name: String) {
        val myPanel = testCasePanels[name] ?: return
        openToolWindowTab()
        scrollToPanel(myPanel)

        val editor = getEditor(name) ?: return
        val settingsProjectState = project.service<PluginSettingsService>().state
        val highlightColor =
            JBColor(
                PluginSettingsBundle.get("colorName"),
                Color(
                    settingsProjectState.colorRed,
                    settingsProjectState.colorGreen,
                    settingsProjectState.colorBlue,
                    30,
                ),
            )
        if (editor.background.equals(highlightColor)) return
        defaultEditorColor = editor.background
        editor.background = highlightColor
        returnOriginalEditorBackground(editor)
    }

    private fun openToolWindowTab() {
        val toolWindowManager = ToolWindowManager.getInstance(project).getToolWindow("TestSpark")
        contentManager = toolWindowManager!!.contentManager
        if (content != null) {
            toolWindowManager.show()
            toolWindowManager.contentManager.setSelectedContent(content!!)
        }
    }

    private fun scrollToPanel(myPanel: JPanel) {
        var sum = 0
        for (component in allTestCasePanel.components) {
            if (component == myPanel) {
                break
            } else {
                sum += component.height
            }
        }
        val scroll = scrollPane.verticalScrollBar
        scroll.value = (scroll.minimum + scroll.maximum) * sum / allTestCasePanel.height
    }

    private fun removeAllHighlights() {
        project.service<EditorService>().editor?.markupModel?.removeAllHighlighters()
    }

    private fun returnOriginalEditorBackground(editor: EditorTextField) {
        Thread {
            Thread.sleep(10000)
            editor.background = defaultEditorColor
        }.start()
    }

    fun highlightCoveredMutants(names: List<String>) {
        names.forEach {
            highlightTestCase(it)
        }
    }

    private fun applyTests() {
        // Filter the selected test cases
        val selectedTestCasePanels = testCasePanels.filter { (it.value.getComponent(0) as JCheckBox).isSelected }
        val selectedTestCases = selectedTestCasePanels.map { it.key }

        // Get the test case components (source code of the tests)
        val testCaseComponents = selectedTestCases
            .map { getEditor(it)!! }
            .map { it.document.text }

        displayUtils!!.applyTests(project, uiContext, testCaseComponents)

        // Remove the selected test cases from the cache and the tool window UI
        removeSelectedTestCases(selectedTestCasePanels)
    }

    fun getEditor(testCaseName: String): EditorTextField? {
        val middlePanelComponent = testCasePanels[testCaseName]?.getComponent(2) ?: return null
        val middlePanel = middlePanelComponent as JPanel
        return (middlePanel.getComponent(1) as JBScrollPane).viewport.view as EditorTextField
    }

    fun updateEditorForFileUrl(fileUrl: String) {
        val documentManager = FileDocumentManager.getInstance()
        // https://intellij-support.jetbrains.com/hc/en-us/community/posts/360004480599/comments/360000703299
        FileEditorManager.getInstance(project).selectedEditors.map { it as TextEditor }.map { it.editor }.map {
            val currentFile = documentManager.getFile(it.document)
            if (currentFile != null) {
                if (currentFile.presentableUrl == fileUrl) {
                    project.service<EditorService>().editor = it
                }
            }
        }
    }

    private fun createToolWindowTab() {
        // Remove generated tests tab from content manager if necessary
        val toolWindowManager = ToolWindowManager.getInstance(project).getToolWindow("TestSpark")
        contentManager = toolWindowManager!!.contentManager
        if (content != null) {
            contentManager!!.removeContent(content!!, true)
        }

        // If there is no generated tests tab, make it
        val contentFactory: ContentFactory = ContentFactory.getInstance()
        content = contentFactory.createContent(
            mainPanel,
            PluginLabelsBundle.get("generatedTests"),
            true,
        )
        contentManager!!.addContent(content!!)

        // Focus on generated tests tab and open toolWindow if not opened already
        contentManager!!.setSelectedContent(content!!)
        toolWindowManager.show()
    }

    private fun closeToolWindow() {
        contentManager?.removeContent(content!!, true)
        ToolWindowManager.getInstance(project).getToolWindow("TestSpark")?.hide()
        coverageVisualisationTabBuilder?.closeToolWindowTab()
    }

    private fun removeSelectedTestCases(selectedTestCasePanels: Map<String, JPanel>) {
        selectedTestCasePanels.forEach { removeTestCase(it.key) }
        removeAllHighlights()
        closeToolWindow()
    }

    fun clear() {
        // Remove the tests
        val testCasePanelsToRemove = testCasePanels.toMap()
        removeSelectedTestCases(testCasePanelsToRemove)

        topButtonsPanelFactory.clear()

        coverageVisualisationTabBuilder?.clear()
    }

    fun removeTestCase(testCaseName: String) {
        // Update the number of selected test cases if necessary
        if ((testCasePanels[testCaseName]!!.getComponent(0) as JCheckBox).isSelected) {
            testsSelected--
        }

        // Remove the test panel from the UI
        allTestCasePanel.remove(testCasePanels[testCaseName])

        // Remove the test panel
        testCasePanels.remove(testCaseName)
    }

    fun updateUI() {
        // Update the UI of the tool window tab
        allTestCasePanel.updateUI()

        topButtonsPanelFactory.updateTopLabels()

        // If no more tests are remaining, close the tool window
        if (testCasePanels.size == 0) closeToolWindow()
    }

    fun getTestCasePanels() = testCasePanels

    fun getTestsSelected() = testsSelected

    fun setTestsSelected(testsSelected: Int) {
        this.testsSelected = testsSelected
    }
}
