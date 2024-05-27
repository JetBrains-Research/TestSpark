package org.jetbrains.research.testspark.display.coverage

import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import org.evosuite.result.MutationInfo
import org.jetbrains.research.testspark.bundles.plugin.PluginLabelsBundle
import org.jetbrains.research.testspark.bundles.plugin.PluginSettingsBundle
import org.jetbrains.research.testspark.core.data.Report
import org.jetbrains.research.testspark.data.IJReport
import org.jetbrains.research.testspark.data.IJTestCase
import org.jetbrains.research.testspark.display.generatedTestsTab.GeneratedTestsTabData
import org.jetbrains.research.testspark.helpers.CoverageToolWindowDisplayHelper
import org.jetbrains.research.testspark.services.PluginSettingsService
import java.awt.Color
import javax.swing.JScrollPane
import kotlin.math.roundToInt

/**
 * Service used to visualise the coverage and inject data in the toolWindow tab.
 *
 * @param project the project
 */
class CoverageVisualisationTabBuilder(private val project: Project, private val editor: Editor) {
    // Variable to keep reference to the coverage visualisation content
    private var content: Content? = null
    private val textAttribute = TextAttributes()

    private var currentHighlightedData: HighlightedData? = null

    private var mainScrollPane: JScrollPane? = null

    /**
     * Represents highlighted data in the editor.
     *
     * @property linesToCover a set of line numbers to be highlighted as coverage lines
     * @property selectedTests a set of selected test names
     * @property testReport the test report associated with the highlighted data
     * @property editor the editor instance where the data is highlighted
     */
    data class HighlightedData(
        val linesToCover: Set<Int>,
        val selectedTests: HashSet<Int>,
        val testReport: Report,
        val editor: Editor,
    )

    /**
     * Clears all highlighters from the list of editors.
     */
    fun clear() {
        currentHighlightedData?.editor?.markupModel?.removeAllHighlighters()
    }

    /**
     * Instantiates tab for coverage table and calls function to update coverage.
     *
     * @param testReport the generated tests summary
     */
    fun show(testReport: Report, generatedTestsTabData: GeneratedTestsTabData) {
        // Show toolWindow statistics
        fillToolWindowContents(testReport)
        createToolWindowTab(generatedTestsTabData)

        updateCoverage(
            testReport.allCoveredLines,
            testReport.testCaseList.values.stream().map { it.id }.toList().toHashSet(),
            testReport,
            generatedTestsTabData,
        )
    }

    /**
     * Highlights lines covered by selected tests.
     * Shows coverage on the gutter next to the covered lines.
     *
     * @param linesToCover total set of lines  to cover
     * @param testReport report used for gutter information
     * @param selectedTests hash set of selected test names
     */
    private fun updateCoverage(
        linesToCover: Set<Int>,
        selectedTests: HashSet<Int>,
        testReport: Report,
        generatedTestsTabData: GeneratedTestsTabData,
    ) {
        currentHighlightedData = HighlightedData(linesToCover, selectedTests, testReport, editor)
        clear()

        val settingsProjectState = project.service<PluginSettingsService>().state

        if (settingsProjectState.showCoverageCheckboxSelected) {
            val color = JBColor(
                PluginSettingsBundle.get("colorName"),
                Color(settingsProjectState.colorRed, settingsProjectState.colorGreen, settingsProjectState.colorBlue),
            )
            val colorForLines = JBColor(
                PluginSettingsBundle.get("colorName"),
                Color(
                    settingsProjectState.colorRed,
                    settingsProjectState.colorGreen,
                    settingsProjectState.colorBlue,
                    30,
                ),
            )

            // Update the color used for highlighting if necessary
            textAttribute.backgroundColor = colorForLines

            // map of mutant operations -> List of names of tests which cover the mutant
            val mapMutantsToTests = HashMap<String, MutableList<String>>()

            testReport.testCaseList.values.forEach { compactTestCase ->
                // Since we are in the IntelliJ plugin's visualizer, all test cases should be an instance of IJTestCase
                if (compactTestCase is IJTestCase) {
                    val mutantsCovered = compactTestCase.coveredMutants
                    val testName = compactTestCase.testName
                    mutantsCovered.forEach {
                        val testCasesCoveringMutant = mapMutantsToTests.getOrPut(it.replacement) { ArrayList() }
                        testCasesCoveringMutant.add(testName)
                    }
                } else {
                    throw IllegalStateException("all test cases passed to the plugin visualizer in IDEA should be an instance of IJTestCase")
                }
            }

            // get a list of mutants covered by each test
            val mutationCovered = getCoveredMutants(testReport, selectedTests)
            // get uncovered mutants for each test case
            val mutationNotCovered = getUncoveredMutants(testReport, selectedTests)

            for (i in linesToCover) {
                val line = i - 1

                val hl = editor.markupModel.addLineHighlighter(
                    line,
                    HighlighterLayer.ADDITIONAL_SYNTAX,
                    textAttribute,
                )

                // get tests that are covering the current line
                val testsCoveringLine = getCoveringLines(testReport, selectedTests, i)
                // get the list of killed and survived mutants in the current line
                val mutationCoveredLine = mutationCovered.getOrDefault(i, listOf()).map { x -> x.replacement }
                val mutationNotCoveredLine = mutationNotCovered.getOrDefault(i, listOf()).map { x -> x.replacement }

                hl.lineMarkerRenderer = CoverageRenderer(
                    color,
                    line,
                    testsCoveringLine,
                    mutationCoveredLine,
                    mutationNotCoveredLine,
                    mapMutantsToTests,
                    project,
                    generatedTestsTabData,
                )
            }
        }
    }

    private fun getCoveringLines(testReport: Report, selectedTests: HashSet<Int>, lineNumber: Int): List<String> {
        return testReport.testCaseList.filter { x -> lineNumber in x.value.coveredLines && x.value.id in selectedTests }
            .map { x -> x.value.testName }
    }

    private fun getUncoveredMutants(testReport: Report, selectedTests: HashSet<Int>): Map<Int, List<MutationInfo>> {
        if (testReport is IJReport) {
            return testReport.allUncoveredMutation.groupBy { x -> x.lineNo } + testReport.testCaseList.filter { x -> x.value.id !in selectedTests }
                .map { x -> (x.value as IJTestCase).coveredMutants }.flatten().groupBy { x -> x.lineNo }
        } else {
            throw IllegalStateException("The report provided to IDEA's UI should be an instance of IJReport")
        }
    }

    private fun getCoveredMutants(testReport: Report, selectedTests: HashSet<Int>): Map<Int, List<MutationInfo>> {
        return testReport.testCaseList.filter { x -> x.value.id in selectedTests }
            .map { x -> (x.value as IJTestCase).coveredMutants }
            .flatten().groupBy { x -> x.lineNo }
    }

    /**
     * Fill the toolWindow to contain the coverage in the labels.
     *
     * @param testReport the generated tests summary
     */
    private fun fillToolWindowContents(testReport: Report) {
        // Calculate line coverage
        val coveredLines = testReport.allCoveredLines.size
        val allLines = testReport.allUncoveredLines.size + coveredLines
        var relativeLines = 100
        if (allLines != 0) {
            relativeLines = (coveredLines.toDouble() / allLines * 100).roundToInt()
        }

        // Call branch coverage
        val coveredBranches = (testReport as IJReport).allCoveredBranches.size
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
        mainScrollPane = CoverageToolWindowDisplayHelper.getPanel(
            arrayListOf(
                testReport.UUT,
                "$relativeLines% ($coveredLines/$allLines)",
                "$relativeBranch% ($coveredBranches/$allBranches)",
                "$relativeMutations% ($coveredMutations/$allMutations)",
            ),
        )
    }

    /**
     * Creates a new toolWindow tab for the coverage visualisation.
     */
    private fun createToolWindowTab(generatedTestsTabData: GeneratedTestsTabData) {
        // Remove coverage visualisation from content manager if necessary
        if (content != null) {
            generatedTestsTabData.contentManager!!.removeContent(content!!, true)
        }

        // If there is no coverage visualisation tab, make it
        val contentFactory: ContentFactory = ContentFactory.getInstance()
        content = contentFactory.createContent(
            mainScrollPane,
            PluginLabelsBundle.get("coverageVisualisation"),
            true,
        )
        generatedTestsTabData.contentManager!!.addContent(content!!)
    }
}
