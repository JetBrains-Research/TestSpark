package org.jetbrains.research.testspark.display.utils

import org.jetbrains.research.testspark.core.data.Report
import org.jetbrains.research.testspark.core.data.TestCase
import org.jetbrains.research.testspark.display.coverage.CoverageVisualisationTabBuilder

object ReportUpdater {
    fun updateTestCase(
        report: Report,
        testCase: TestCase,
        coverageVisualisationTabBuilder: CoverageVisualisationTabBuilder
    ) {
        report.testCaseList.remove(testCase.id)
        report.testCaseList[testCase.id] = testCase
        report.normalized()
        coverageVisualisationTabBuilder.showCoverage(report)
    }

    fun removeTestCase(
        report: Report,
        testCase: TestCase,
        coverageVisualisationTabBuilder: CoverageVisualisationTabBuilder
    ) {
        report.testCaseList.remove(testCase.id)
        report.normalized()
        coverageVisualisationTabBuilder.showCoverage(report)
    }

    fun unselectTestCase(
        report: Report,
        unselectedTestCases: HashMap<Int, TestCase>,
        testCaseId: Int,
        coverageVisualisationTabBuilder: CoverageVisualisationTabBuilder
    ) {
        unselectedTestCases[testCaseId] = report.testCaseList[testCaseId]!!
        removeTestCase(report, report.testCaseList[testCaseId]!!, coverageVisualisationTabBuilder)
    }

    fun selectTestCase(
        report: Report,
        unselectedTestCases: HashMap<Int, TestCase>,
        testCaseId: Int,
        coverageVisualisationTabBuilder: CoverageVisualisationTabBuilder
    ) {
        report.testCaseList[testCaseId] = unselectedTestCases[testCaseId]!!
        unselectedTestCases.remove(testCaseId)
        report.normalized()
        coverageVisualisationTabBuilder.showCoverage(report)
    }
}
