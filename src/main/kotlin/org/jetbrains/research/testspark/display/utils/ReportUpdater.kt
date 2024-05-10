package org.jetbrains.research.testspark.display.utils

import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.core.data.Report
import org.jetbrains.research.testspark.core.data.TestCase
import org.jetbrains.research.testspark.display.GeneratedTestsTabData
import org.jetbrains.research.testspark.display.coverage.CoverageVisualisationTabFactory

object ReportUpdater {
    fun updateTestCase(
        project: Project,
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
        project: Project,
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
        project: Project,
        report: Report,
        unselectedTestCases: HashMap<Int, TestCase>,
        testCaseId: Int,
        coverageVisualisationTabFactory: CoverageVisualisationTabFactory,
        generatedTestsTabData: GeneratedTestsTabData,
    ) {
        unselectedTestCases[testCaseId] = report.testCaseList[testCaseId]!!
        removeTestCase(project, report, report.testCaseList[testCaseId]!!, coverageVisualisationTabFactory, generatedTestsTabData)
    }

    fun selectTestCase(
        project: Project,
        report: Report,
        unselectedTestCases: HashMap<Int, TestCase>,
        testCaseId: Int,
        coverageVisualisationTabFactory: CoverageVisualisationTabFactory,
        generatedTestsTabData: GeneratedTestsTabData,
    ) {
        report.testCaseList[testCaseId] = unselectedTestCases[testCaseId]!!
        unselectedTestCases.remove(testCaseId)
        report.normalized()
        coverageVisualisationTabFactory.show(report, generatedTestsTabData)
    }
}
