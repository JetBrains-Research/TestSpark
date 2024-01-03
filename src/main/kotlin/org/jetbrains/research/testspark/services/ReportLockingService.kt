package org.jetbrains.research.testspark.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.data.Report
import org.jetbrains.research.testspark.data.TestCase

@Service(Service.Level.PROJECT)
class ReportLockingService(private val project: Project) {
    // final report
    private var report: Report? = null

    private val unselectedTestCases = HashMap<Int, TestCase>()

    private val log = Logger.getInstance(this.javaClass)

    fun getReport() = report!!

    /**
     * Updates the state after the action of publishing results.
     * @param report the generated test suite
     */
    fun receiveReport(report: Report) {
        this.report = report

        project.service<TestCaseDisplayService>().updateEditorForFileUrl(project.service<TestGenerationDataService>().fileUrl)

        if (project.service<ProjectContextService>().editor != null) {
            project.service<TestCaseDisplayService>().displayTestCases()
            project.service<CoverageVisualisationService>().showCoverage(report)
        } else {
            log.info("No editor opened for received test result")
        }
    }

    fun updateTestCase(testCase: TestCase) {
        report!!.testCaseList.remove(testCase.id)
        report!!.testCaseList[testCase.id] = testCase
        report!!.normalized()
        project.service<CoverageVisualisationService>().showCoverage(report!!)
    }

    fun removeTestCase(testCase: TestCase) {
        report!!.testCaseList.remove(testCase.id)
        report!!.normalized()
        project.service<CoverageVisualisationService>().showCoverage(report!!)
    }

    fun unselectTestCase(testCaseId: Int) {
        unselectedTestCases[testCaseId] = report!!.testCaseList[testCaseId]!!
        removeTestCase(report!!.testCaseList[testCaseId]!!)
    }

    fun selectTestCase(testCaseId: Int) {
        report!!.testCaseList[testCaseId] = unselectedTestCases[testCaseId]!!
        unselectedTestCases.remove(testCaseId)
        report!!.normalized()
        project.service<CoverageVisualisationService>().showCoverage(report!!)
    }
}
