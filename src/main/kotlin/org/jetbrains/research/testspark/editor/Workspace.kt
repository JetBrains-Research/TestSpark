package org.jetbrains.research.testspark.editor

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import org.jetbrains.research.testspark.DataFilesUtil
import org.jetbrains.research.testspark.data.Report
import org.jetbrains.research.testspark.data.TestCase
import org.jetbrains.research.testspark.data.TestGenerationData
import org.jetbrains.research.testspark.services.CoverageVisualisationService
import org.jetbrains.research.testspark.services.ErrorService
import org.jetbrains.research.testspark.services.TestCaseDisplayService
import org.jetbrains.research.testspark.services.TestsExecutionResultService

/**
 * Workspace state service
 *
 * Handles user workspace state and modifications of that state
 * related to test generation.
 *
 */
@Service(Service.Level.PROJECT)
class Workspace(private val project: Project) {
    // final report
    var report: Report? = null

    var editor: Editor? = null

    // The class path of the project.
    var projectClassPath: String? = null
    var testResultDirectory: String? = null
    var testResultName: String? = null

    // The path to save the generated test results.
    var resultPath: String? = null

    // The base directory of the project.
    var baseDir: String? = null
    var vFile: VirtualFile? = null

    // The URL of the file being tested.
    var fileUrl: String? = null

    // The modification stamp of the file being tested.
    var modificationStamp: Long? = null
    var cutPsiClass: PsiClass? = null

    // The module to cut.
    var cutModule: Module? = null

    // The fully qualified name of the class being tested.
    var classFQN: String? = null

    private val log = Logger.getInstance(this.javaClass)

    var testGenerationData = TestGenerationData()

    /**
     * Clears the given project's test-related data, including test case display,
     * error service, coverage visualization, and test generation data.
     *
     * @param project the project to clear the test-related data for
     */
    fun clear(project: Project) {
        project.service<TestCaseDisplayService>().clear()
        project.service<ErrorService>().clear()
        project.service<CoverageVisualisationService>().clear()
        testGenerationData.clear()
        DataFilesUtil.cleanFolder(resultPath!!)
        project.service<TestsExecutionResultService>().clear()
    }

    /**
     * Updates the state after the action of publishing results.
     * @param report the generated test suite
     */
    fun receiveGenerationResult(report: Report) {
        this.report = report

        project.service<TestCaseDisplayService>().updateEditorForFileUrl(testGenerationData.fileUrl)

        if (editor != null) {
            project.service<TestCaseDisplayService>().showGeneratedTests(editor!!)
            project.service<CoverageVisualisationService>().showCoverage(report, editor!!)
        } else {
            log.info("No editor opened for received test result")
        }
    }

    fun updateReport(testCase: TestCase) {
        report!!.testCaseList.remove(testCase.id)
        report!!.testCaseList[testCase.id] = testCase
        report!!.normalized()
        project.service<CoverageVisualisationService>().showCoverage(report!!, editor!!)
    }

    fun removeTestCase(testCase: TestCase) {
        report!!.testCaseList.remove(testCase.id)
        report!!.normalized()
        project.service<CoverageVisualisationService>().showCoverage(report!!, editor!!)
    }
}
