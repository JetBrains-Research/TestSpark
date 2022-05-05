package com.github.mitchellolsthoorn.testgenie.services

import com.github.mitchellolsthoorn.testgenie.coverage.TestGenieCoverageRenderer
import com.intellij.openapi.diff.DiffColors
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import org.evosuite.utils.CompactReport
import java.awt.Color

class CoverageVisualisationService(private val project: Project) {

    /**
     * Shows coverage on the gutter next to the covered lines.
     *
     * @param testReport the generated tests summary
     */
    fun showCoverage(testReport: CompactReport) {
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