package org.jetbrains.research.testspark.display

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import org.jetbrains.research.testspark.core.data.Report
import org.jetbrains.research.testspark.data.UIContext
import org.jetbrains.research.testspark.display.coverage.CoverageVisualisationTabFactory
import org.jetbrains.research.testspark.display.generatedTestsTab.GeneratedTestsTabFactory

class TestSparkDisplayFactory(private val project: Project) {
    private var editor: Editor? = null

    private var coverageVisualisationTabFactory: CoverageVisualisationTabFactory? = null
    private var generatedTestsTabFactory: GeneratedTestsTabFactory? = null

    /**
     * Fill the panel with the generated test cases. Remove all previously shown test cases.
     * Add Tests and their names to a List of pairs (used for highlighting)
     */
    fun display(report: Report, editor: Editor, uiContext: UIContext) {
        this.editor = editor

        coverageVisualisationTabFactory = CoverageVisualisationTabFactory(project, editor)
        generatedTestsTabFactory = GeneratedTestsTabFactory(project, report, editor, uiContext, coverageVisualisationTabFactory!!)

        coverageVisualisationTabFactory!!.show(report, generatedTestsTabFactory!!.getGeneratedTestsTabData())
        generatedTestsTabFactory!!.show()
    }

    fun clear() {
        editor?.markupModel?.removeAllHighlighters()

        coverageVisualisationTabFactory!!.clear()
        generatedTestsTabFactory!!.clear()

        closeToolWindow()
    }

    /**
     * Closes the tool window and destroys the content of the tab.
     */
    private fun closeToolWindow() {
        coverageVisualisationTabFactory!!.closeToolWindowTab()
        generatedTestsTabFactory!!.closeToolWindowTab()

        ToolWindowManager.getInstance(project).getToolWindow("TestSpark")?.hide()
    }
}
