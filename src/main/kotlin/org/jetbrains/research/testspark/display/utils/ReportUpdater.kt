package org.jetbrains.research.testspark.display.utils

import org.jetbrains.research.testspark.core.data.Report
import org.jetbrains.research.testspark.core.data.TestCase
import org.jetbrains.research.testspark.display.coverage.CoverageVisualisationTabBuilder
import org.jetbrains.research.testspark.display.generatedTests.GeneratedTestsTabData

object ReportUpdater {
    fun updateTestCase(
        report: Report,
        testCase: TestCase,
        coverageVisualisationTabBuilder: CoverageVisualisationTabBuilder,
        generatedTestsTabData: GeneratedTestsTabData,
    ) {
        report.testCaseList.remove(testCase.id)
        report.testCaseList[testCase.id] = testCase
        report.normalized()
        coverageVisualisationTabBuilder.show(report, generatedTestsTabData)
    }

    fun removeTestCase(
        report: Report,
        testCase: TestCase,
        coverageVisualisationTabBuilder: CoverageVisualisationTabBuilder,
        generatedTestsTabData: GeneratedTestsTabData,
    ) {
        report.testCaseList.remove(testCase.id)
        report.normalized()
        coverageVisualisationTabBuilder.show(report, generatedTestsTabData)
    }

    fun unselectTestCase(
        report: Report,
        unselectedTestCases: HashMap<Int, TestCase>,
        testCaseId: Int,
        coverageVisualisationTabBuilder: CoverageVisualisationTabBuilder,
        generatedTestsTabData: GeneratedTestsTabData,
    ) {
        unselectedTestCases[testCaseId] = report.testCaseList[testCaseId]!!
        removeTestCase(report, report.testCaseList[testCaseId]!!, coverageVisualisationTabBuilder, generatedTestsTabData)
    }

    fun selectTestCase(
        report: Report,
        unselectedTestCases: HashMap<Int, TestCase>,
        testCaseId: Int,
        coverageVisualisationTabBuilder: CoverageVisualisationTabBuilder,
        generatedTestsTabData: GeneratedTestsTabData,
    ) {
        report.testCaseList[testCaseId] = unselectedTestCases[testCaseId]!!
        unselectedTestCases.remove(testCaseId)
        report.normalized()
        coverageVisualisationTabBuilder.show(report, generatedTestsTabData)
    }
}
