package nl.tudelft.ewi.se.ciselab.testgenie.services

import nl.tudelft.ewi.se.ciselab.testgenie.coverage.CoverageRenderer
import nl.tudelft.ewi.se.ciselab.testgenie.settings.TestGenieSettingsService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diff.DiffColors
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import org.evosuite.utils.CompactReport
import java.awt.Color
import kotlin.math.roundToInt

class CoverageVisualisationService(private val project: Project) {

    // Variable to keep reference to the coverage visualisation content
    var content: Content? = null

    /**
     * Shows coverage on the gutter next to the covered lines.
     *
     * @param testReport the generated tests summary
     */
    fun showCoverage(testReport: CompactReport) {
        // Show toolWindow statistics
        fillToolWindowContents(testReport)
        createToolWindowTab()

        // Show in-line coverage only if enabled in settings
        val state = ApplicationManager.getApplication().getService(TestGenieSettingsService::class.java).state
        if (state.showCoverage) {
            val editor = FileEditorManager.getInstance(project).selectedTextEditor!!

            val color = Color(100, 150, 20)

            for (i in testReport.allCoveredLines) {
                val line = i - 1
                val hl = editor.markupModel.addLineHighlighter(DiffColors.DIFF_INSERTED, line, HighlighterLayer.LAST)
                hl.lineMarkerRenderer = CoverageRenderer(color,
                    line,
                    testReport.testCaseList.filter { x -> i in x.value.coveredLines }.map { x -> x.key })
            }
        }
    }

    /**
     * Fill the toolWindow to contain the coverage in the labels.
     *
     * @param testReport the generated tests summary
     */
    private fun fillToolWindowContents(testReport: CompactReport) {

        // Calculate line coverage
        val coveredLines = testReport.allCoveredLines.size
        val allLines = testReport.allUncoveredLines.size + coveredLines
        var relativeLines = 100
        if (allLines != 0) {
            relativeLines = (coveredLines.toDouble() / allLines * 100).roundToInt()
        }

        // Call branch coverage
        val coveredBranches = testReport.allCoveredBranches.size
        val allBranches = testReport.allUncoveredBranches.size + coveredBranches
        var relativeBranch = 100
        if (allBranches != 0) {
            relativeBranch = (coveredBranches.toDouble() / allBranches * 100).roundToInt()
        }

        // Call mutations coverage
        val coveredMutations = testReport.allCoveredMutation.size
        val allMutations = testReport.allUncoveredMutation.size + coveredMutations
        var relativeMutations = 100
        if (allMutations != 0) {
            relativeMutations = (coveredMutations.toDouble() / allMutations * 100).roundToInt()
        }

        // Change the values in the table
        val coverageToolWindowDisplayService = project.service<CoverageToolWindowDisplayService>()
        coverageToolWindowDisplayService.data[4] = testReport.UUT
        coverageToolWindowDisplayService.data[5] = "$relativeLines% ($coveredLines/$allLines)"
        coverageToolWindowDisplayService.data[6] = "$relativeBranch% ($coveredBranches/$allBranches)"
        coverageToolWindowDisplayService.data[7] = "$relativeMutations% ($coveredMutations/ $allMutations)"
    }

    /**
     * Creates a new toolWindow tab for the coverage visualisation.
     */
    private fun createToolWindowTab() {
        val visualisationService = project.service<CoverageToolWindowDisplayService>()

        // Remove coverage visualisation from content manager if necessary
        val toolWindowManager = ToolWindowManager.getInstance(project).getToolWindow("TestGenie")
        val contentManager = toolWindowManager!!.contentManager
        if (content != null) {
            contentManager.removeContent(content!!, true)
        }

        // If there is no coverage visualisation tab, make it
        val contentFactory: ContentFactory = ContentFactory.SERVICE.getInstance()
        content = contentFactory.createContent(
            visualisationService.mainPanel, "Coverage Visualisation", true
        )
        contentManager.addContent(content!!)

        // Focus on coverage tab and open toolWindow if not opened already
        contentManager.setSelectedContent(content!!)
        toolWindowManager.show()
    }
}