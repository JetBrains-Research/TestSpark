package org.jetbrains.research.testspark.display

import org.jetbrains.research.testspark.core.data.Report
import org.jetbrains.research.testspark.core.data.TestCase
import org.jetbrains.research.testspark.display.coverage.CoverageVisualisationTabFactory
import org.jetbrains.research.testspark.display.generatedTestsTab.GeneratedTestsTabData

object ReportUpdater {
    fun updateTestCase(
        report: Report,
        testCase: TestCase,
        coverageVisualisationTabFactory: CoverageVisualisationTabFactory,
        generatedTestsTabData: GeneratedTestsTabData,
    ) {
        report.testCaseList.remove(testCase.id)
        report.testCaseList[testCase.id] = testCase
        report.normalized()
        coverageVisualisationTabFactory.show(report, generatedTestsTabData)
    }

    fun removeTestCase(
        report: Report,
        testCase: TestCase,
        coverageVisualisationTabFactory: CoverageVisualisationTabFactory,
        generatedTestsTabData: GeneratedTestsTabData,
    ) {
        report.testCaseList.remove(testCase.id)
        report.normalized()
        coverageVisualisationTabFactory.show(report, generatedTestsTabData)
    }

    fun unselectTestCase(
        report: Report,
        testCaseId: Int,
        coverageVisualisationTabFactory: CoverageVisualisationTabFactory,
        generatedTestsTabData: GeneratedTestsTabData,
    ) {
        generatedTestsTabData.unselectedTestCases[testCaseId] = report.testCaseList[testCaseId]!!
        removeTestCase(report, report.testCaseList[testCaseId]!!, coverageVisualisationTabFactory, generatedTestsTabData)
    }

    fun selectTestCase(
        report: Report,
        testCaseId: Int,
        coverageVisualisationTabFactory: CoverageVisualisationTabFactory,
        generatedTestsTabData: GeneratedTestsTabData,
    ) {
        report.testCaseList[testCaseId] = generatedTestsTabData.unselectedTestCases[testCaseId]!!
        generatedTestsTabData.unselectedTestCases.remove(testCaseId)
        report.normalized()
        coverageVisualisationTabFactory.show(report, generatedTestsTabData)
    }
}
