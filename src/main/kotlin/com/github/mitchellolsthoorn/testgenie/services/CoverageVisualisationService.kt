package com.github.mitchellolsthoorn.testgenie.services

import com.github.mitchellolsthoorn.testgenie.coverage.TestGenieCoverageRenderer
import com.github.mitchellolsthoorn.testgenie.settings.TestGenieSettingsService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diff.DiffColors
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import org.evosuite.utils.CompactReport
import java.awt.Color
import kotlin.math.roundToInt

class CoverageVisualisationService(private val project: Project) {

    /**
     * Shows coverage on the gutter next to the covered lines.
     *
     * @param testReport the generated tests summary
     */
    fun showCoverage(testReport: CompactReport) {
        // Show toolWindow statistics
        fillToolWindowContents(testReport)

        // Show in-line coverage only if enabled in settings
        val state = ApplicationManager.getApplication().getService(TestGenieSettingsService::class.java).state
        if(state.showCoverage) {
            val editor = FileEditorManager.getInstance(project).selectedTextEditor!!

            val color = Color(100, 150, 20)

            for (i in testReport.allCoveredLines) {
                val line = i - 1
                val hl = editor.markupModel.addLineHighlighter(DiffColors.DIFF_INSERTED, line, HighlighterLayer.LAST)
                hl.lineMarkerRenderer = TestGenieCoverageRenderer(color, line, testReport.testCaseList
                        .filter { x -> i in x.value.coveredLines }.map{x -> x.key})
            }
        }
    }

    /**
     * Fill the toolWindow to contain the coverage in the labels.
     *
     * @param testReport the generated tests summary
     */
    private fun fillToolWindowContents(testReport: CompactReport) {
        val visualisationService = project.service<CoverageToolWindowDisplayService>()

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

        visualisationService.panelTitleAbsolute.text = "Absolute Test Coverage for ${testReport.UUT}"
        visualisationService.panelTitleRelative.text = "Relative Test Coverage for ${testReport.UUT}"

        visualisationService.absoluteLines.text = "Amount of Lines covered: $coveredLines Total: $allLines"
        visualisationService.absoluteBranch.text = "Amount of Branches covered: $coveredBranches Total: $allBranches"
        visualisationService.absoluteMutant.text = "Amount of Mutants covered: $coveredMutations Total: $allMutations"

        visualisationService.relativeLines.text = "Percentage of Lines covered: $relativeLines%"
        visualisationService.relativeBranch.text = "Percentage of Branches covered: $relativeBranch%"
        visualisationService.relativeMutant.text = "Percentage of Mutants covered: $relativeMutations%"
    }
}