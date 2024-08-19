package org.jetbrains.research.testspark.display.utils

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.core.data.Report
import org.jetbrains.research.testspark.core.data.TestCase
import org.jetbrains.research.testspark.services.CoverageVisualisationService

object ReportUpdater {
    fun updateTestCase(project: Project, report: Report, testCase: TestCase) {
        report.testCaseList.remove(testCase.id)
        report.testCaseList[testCase.id] = testCase
        report.normalized()
        project.service<CoverageVisualisationService>().showCoverage(report)
    }

    fun removeTestCase(project: Project, report: Report, testCase: TestCase) {
        report.testCaseList.remove(testCase.id)
        report.normalized()
        project.service<CoverageVisualisationService>().showCoverage(report)
    }

    fun unselectTestCase(project: Project, report: Report, unselectedTestCases: HashMap<Int, TestCase>, testCaseId: Int) {
        unselectedTestCases[testCaseId] = report.testCaseList[testCaseId]!!
        removeTestCase(project, report, report.testCaseList[testCaseId]!!)
    }

    fun selectTestCase(project: Project, report: Report, unselectedTestCases: HashMap<Int, TestCase>, testCaseId: Int) {
        report.testCaseList[testCaseId] = unselectedTestCases[testCaseId]!!
        unselectedTestCases.remove(testCaseId)
        report.normalized()
        project.service<CoverageVisualisationService>().showCoverage(report)
    }
}
