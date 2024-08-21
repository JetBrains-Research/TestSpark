package org.jetbrains.research.testspark.display.generatedTests

import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.content.ContentManager
import org.jetbrains.research.testspark.bundles.plugin.PluginLabelsBundle
import org.jetbrains.research.testspark.core.data.Report
import org.jetbrains.research.testspark.core.test.SupportedLanguage
import org.jetbrains.research.testspark.data.UIContext
import org.jetbrains.research.testspark.display.coverage.CoverageVisualisationTabBuilder
import org.jetbrains.research.testspark.display.java.JavaDisplayUtils
import org.jetbrains.research.testspark.display.kotlin.KotlinDisplayUtils
import org.jetbrains.research.testspark.display.template.DisplayUtils
import org.jetbrains.research.testspark.display.utils.ReportUpdater
import org.jetbrains.research.testspark.services.EditorService
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JSeparator
import javax.swing.SwingConstants

class GeneratedTestsTabBuilder(
    private val project: Project,
    private val report: Report,
    private val editor: Editor,
    private val uiContext: UIContext,
    private val coverageVisualisationTabBuilder: CoverageVisualisationTabBuilder,
) {
    private val generatedTestsTabData: GeneratedTestsTabData = GeneratedTestsTabData()

    private var mainPanel: JPanel = JPanel()

    private var applyButton: JButton = JButton(PluginLabelsBundle.get("applyButton"))

    private var displayUtils: DisplayUtils? = null

    init {
        generatedTestsTabData.allTestCasePanel.layout =
            BoxLayout(generatedTestsTabData.allTestCasePanel, BoxLayout.Y_AXIS)
        mainPanel.layout = BorderLayout()

        mainPanel.add(
            generatedTestsTabData.topButtonsPanelBuilder.getPanel(project, generatedTestsTabData),
            BorderLayout.NORTH,
        )
        mainPanel.add(generatedTestsTabData.scrollPane, BorderLayout.CENTER)

        applyButton.isOpaque = false
        applyButton.isContentAreaFilled = false
        mainPanel.add(applyButton, BorderLayout.SOUTH)

        applyButton.addActionListener { applyTests() }
    }

    fun generatedTestsTabData() = generatedTestsTabData

    fun getTestCaseNameToPanel() = generatedTestsTabData.testCaseNameToPanel

    fun getRemoveAllButton() = generatedTestsTabData.topButtonsPanelBuilder.getRemoveAllButton()

    fun show(contentManager: ContentManager, language: SupportedLanguage) {
        generatedTestsTabData.allTestCasePanel.removeAll()
        generatedTestsTabData.testCaseNameToPanel.clear()

        displayUtils = when (language) {
            SupportedLanguage.Java -> {
                JavaDisplayUtils()
            }

            SupportedLanguage.Kotlin -> {
                KotlinDisplayUtils()
            }
        }

        val editor = project.service<EditorService>().editor!!

        generatedTestsTabData.contentManager = contentManager

        addSeparator()

        // TestCasePanelFactories array
        val testCasePanelFactories = arrayListOf<TestCasePanelBuilder>()

        report.testCaseList.values.forEach {
            val testCase = it
            val testCasePanel = JPanel()
            testCasePanel.layout = BorderLayout()

            // Add a checkbox to select the test
            val checkbox = JCheckBox()
            checkbox.isSelected = true
            checkbox.addItemListener {
                // Update the number of selected tests
                generatedTestsTabData.testsSelected -= (1 - 2 * checkbox.isSelected.compareTo(false))

                if (checkbox.isSelected) {
                    ReportUpdater.selectTestCase(
                        report,
                        generatedTestsTabData.unselectedTestCases,
                        testCase.id,
                        coverageVisualisationTabBuilder,
                        generatedTestsTabData,
                    )
                } else {
                    ReportUpdater.unselectTestCase(
                        report,
                        generatedTestsTabData.unselectedTestCases,
                        testCase.id,
                        coverageVisualisationTabBuilder,
                        generatedTestsTabData,
                    )
                }

                GenerateTestsTabHelper.update(generatedTestsTabData)
            }
            testCasePanel.add(checkbox, BorderLayout.WEST)

            val testCasePanelBuilder =
                TestCasePanelBuilder(
                    project, language, testCase, editor, checkbox, uiContext, report,
                    coverageVisualisationTabBuilder, generatedTestsTabData,
                )
            testCasePanel.add(testCasePanelBuilder.getUpperPanel(), BorderLayout.NORTH)
            testCasePanel.add(testCasePanelBuilder.getMiddlePanel(), BorderLayout.CENTER)
            testCasePanel.add(testCasePanelBuilder.getBottomPanel(), BorderLayout.SOUTH)

            testCasePanelFactories.add(testCasePanelBuilder)

            testCasePanel.add(Box.createRigidArea(Dimension(12, 0)), BorderLayout.EAST)

            // Add panel to parent panel
            testCasePanel.maximumSize = Dimension(Short.MAX_VALUE.toInt(), Short.MAX_VALUE.toInt())
            generatedTestsTabData.allTestCasePanel.add(testCasePanel)
            addSeparator()

            generatedTestsTabData.testCaseNameToPanel[testCase.testName] = testCasePanel
            generatedTestsTabData.testCaseNameToSelectedCheckbox[testCase.testName] = checkbox
            generatedTestsTabData.testCaseNameToEditorTextField[testCase.testName] = testCasePanelBuilder.getEditorTextField()
        }

        // Update the number of selected tests (all tests are selected by default)
        generatedTestsTabData.testsSelected = generatedTestsTabData.testCaseNameToPanel.size

        generatedTestsTabData.testCasePanelFactories.addAll(testCasePanelFactories)
        generatedTestsTabData.topButtonsPanelBuilder.update(generatedTestsTabData)

        createToolWindowTab()
    }

    private fun addSeparator() {
        generatedTestsTabData.allTestCasePanel.add(Box.createRigidArea(Dimension(0, 10)))
        generatedTestsTabData.allTestCasePanel.add(JSeparator(SwingConstants.HORIZONTAL))
        generatedTestsTabData.allTestCasePanel.add(Box.createRigidArea(Dimension(0, 10)))
    }

    private fun removeAllHighlights() {
        project.service<EditorService>().editor?.markupModel?.removeAllHighlighters()
    }

    private fun applyTests() {
        // Filter the selected test cases
        val selectedTestCasePanels =
            generatedTestsTabData.testCaseNameToPanel.filter { (it.value.getComponent(0) as JCheckBox).isSelected }
        val selectedTestCases = selectedTestCasePanels.map { it.key }

        // Get the test case components (source code of the tests)
        val testCaseComponents = selectedTestCases
            .map { generatedTestsTabData.testCaseNameToEditorTextField[it]!! }
            .map { it.document.text }

        displayUtils!!.applyTests(project, uiContext, testCaseComponents)

        // Remove the selected test cases from the cache and the tool window UI
        removeSelectedTestCases(selectedTestCasePanels)
    }

    private fun createToolWindowTab() {
        // Remove generated tests tab from content manager if necessary
        val toolWindowManager = ToolWindowManager.getInstance(project).getToolWindow("TestSpark")
        generatedTestsTabData.contentManager = toolWindowManager!!.contentManager
        if (generatedTestsTabData.content != null) {
            generatedTestsTabData.contentManager!!.removeContent(generatedTestsTabData.content!!, true)
        }

        // If there is no generated tests tab, make it
        val contentFactory: ContentFactory = ContentFactory.getInstance()
        generatedTestsTabData.content = contentFactory.createContent(
            mainPanel,
            PluginLabelsBundle.get("generatedTests"),
            true,
        )
        generatedTestsTabData.contentManager!!.addContent(generatedTestsTabData.content!!)

        // Focus on generated tests tab and open toolWindow if not opened already
        generatedTestsTabData.contentManager!!.setSelectedContent(generatedTestsTabData.content!!)
        toolWindowManager.show()
    }

    private fun closeToolWindow() {
        generatedTestsTabData.contentManager?.removeContent(generatedTestsTabData.content!!, true)
        ToolWindowManager.getInstance(project).getToolWindow("TestSpark")?.hide()
        coverageVisualisationTabBuilder.closeToolWindowTab()
    }

    private fun removeSelectedTestCases(selectedTestCasePanels: Map<String, JPanel>) {
        selectedTestCasePanels.forEach { GenerateTestsTabHelper.removeTestCase(it.key, generatedTestsTabData) }
        removeAllHighlights()
        closeToolWindow()
    }

    fun clear() {
        // Remove the tests
        val testCasePanelsToRemove = generatedTestsTabData.testCaseNameToPanel.toMap()
        removeSelectedTestCases(testCasePanelsToRemove)

        generatedTestsTabData.topButtonsPanelBuilder.clear(generatedTestsTabData)

        coverageVisualisationTabBuilder.clear()
    }
}
