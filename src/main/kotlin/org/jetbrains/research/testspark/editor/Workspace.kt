package org.jetbrains.research.testspark.editor

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import org.jetbrains.research.testspark.data.Report
import org.jetbrains.research.testspark.data.TestCase
import org.jetbrains.research.testspark.data.TestGenerationData
import org.jetbrains.research.testspark.services.*
import org.jetbrains.research.testspark.tools.evosuite.validation.VALIDATION_RESULT_TOPIC
import org.jetbrains.research.testspark.tools.evosuite.validation.ValidationResultListener
import org.jetbrains.research.testspark.tools.evosuite.validation.Validator

/**
 * Workspace state service
 *
 * Handles user workspace state and modifications of that state
 * related to test generation.
 *
 */
class Workspace(private val project: Project) : Disposable {
    data class TestJobInfo(
        val fileUrl: String,
        var targetUnit: String,
        val modificationTS: Long,
        val jobId: String,
        val targetClassPath: String,
    )

    class TestJob(
        val info: TestJobInfo,
        val report: Report,
        val selectedTests: HashSet<String>,
    ) {
        private fun getSelectedTests(): List<TestCase> {
            return report.testCaseList.filter { selectedTests.contains(it.key) }.map { it.value }
        }

        fun getSelectedLines(): HashSet<Int> {
            val lineSet: HashSet<Int> = HashSet()
            getSelectedTests().map { lineSet.addAll(it.coveredLines) }
            return lineSet
        }
    }

    var editor: Editor? = null
    private var testJob: TestJob? = null

    var projectClassPath: String? = null
    var testResultDirectory: String? = null
    var testResultName: String? = null
    var resultPath: String? = null
    var baseDir: String? = null
    var serializeResultPath: String? = null
    var vFile: VirtualFile? = null
    var fileUrl: String? = null
    var modificationStamp: Long? = null
    var cutPsiClass: PsiClass? = null
    var cutModule: Module? = null
    var classFQN: String? = null

    private val log = Logger.getInstance(this.javaClass)

    private var listenerDisposable: Disposable? = null

    var testGenerationData = TestGenerationData()

    var key: TestJobInfo? = null

    init {
        val connection = project.messageBus.connect()

        // Set event listener for coverage visualization toggles for specific methods.
        // These are triggered whenever the user toggles a test case's checkbox.
        connection.subscribe(
            COVERAGE_SELECTION_TOGGLE_TOPIC,
            object : CoverageSelectionToggleListener {
                override fun testGenerationResult(testName: String, selected: Boolean, editor: Editor) {
                    val vFile = vFileForDocument(editor.document) ?: return
                    val fileKey = vFile.presentableUrl
                    val testJob = testGenerationData.testGenerationResults[fileKey]?.last() ?: return
                    val modTs = editor.document.modificationStamp

                    if (selected) {
                        testJob.selectedTests.add(testName)
                    } else {
                        testJob.selectedTests.remove(testName)
                    }

                    // update coverage only if the modification timestamp is the same
                    if (testJob.info.modificationTS == modTs) {
                        updateCoverage(testJob.getSelectedLines(), testJob.selectedTests, testJob.report, editor)
                    }
                }
            },
        )

        connection.subscribe(
            VALIDATION_RESULT_TOPIC,
            object : ValidationResultListener {
                override fun validationResult(junitResult: Validator.JUnitResult) {
                    showValidationResult(junitResult)
                }
            },
        )

        val disposable =
            Disposer.newDisposable(ApplicationManager.getApplication(), "Workspace.myDocumentListenerDisposable")

        // Set event listener for document changes. These are triggered whenever the user changes
        // the contents of the editor.
        EditorFactory.getInstance().eventMulticaster.addDocumentListener(
            object : DocumentListener {
                override fun documentChanged(event: DocumentEvent) {
                    super.documentChanged(event)
                    val file = FileDocumentManager.getInstance().getFile(event.document) ?: return
                    val fileName = file.presentableUrl
                    val modTs = event.document.modificationStamp

                    val testJob = lastTestGeneration(fileName) ?: return

                    if (testJob.info.modificationTS != modTs) {
                        val editor = editorForVFile(file)
                        editor?.markupModel?.removeAllHighlighters()
                    }
                }
            },
            disposable,
        )
        listenerDisposable = disposable
    }

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
    }

    /**
     * Updates the state after the action of publishing results.
     *
     * @param testResultName the test result job id which was received
     * @param testReport the generated test suite
     * @param cacheLazyPipeline the runner that was instantiated but not used to create the test suite
     *                        due to a cache hit, or null if there was a cache miss
     * @return the test job that was generated
     */
    fun receiveGenerationResult(
        testResultName: String,
        testReport: Report,
        cachedJobKey: TestJobInfo? = null,
    ): TestJobInfo {
        val pendingJobKey = testGenerationData.pendingTestResults.remove(testResultName)!!

        val jobKey = cachedJobKey ?: pendingJobKey

        val resultsForFile = testGenerationData.testGenerationResults.getOrPut(jobKey.fileUrl) { ArrayList() }
        val displayedSet = HashSet<String>()
        displayedSet.addAll(testReport.testCaseList.keys)

        testJob = TestJob(jobKey, testReport, displayedSet)
        resultsForFile.add(testJob!!)

        updateEditorForFileUrl(jobKey.fileUrl)

        if (editor != null) {
            showReport()
        } else {
            log.info("No editor opened for received test result")
        }

        return jobKey
    }

    /**
     * Utility function that returns the editor for a specific file url,
     * in case it is opened in the IDE
     */
    private fun updateEditorForFileUrl(fileUrl: String) {
        val documentManager = FileDocumentManager.getInstance()
        // https://intellij-support.jetbrains.com/hc/en-us/community/posts/360004480599/comments/360000703299
        FileEditorManager.getInstance(project).selectedEditors.map { it as TextEditor }.map { it.editor }.map {
            val currentFile = documentManager.getFile(it.document)
            if (currentFile != null) {
                if (currentFile.presentableUrl == fileUrl) {
                    editor = it
                }
            }
        }
    }

    /**
     * Utility function that returns the editor for a specific VirtualFile
     * in case it is opened in the IDE
     */
    private fun editorForVFile(file: VirtualFile): Editor? {
        val documentManager = FileDocumentManager.getInstance()
        FileEditorManager.getInstance(project).allEditors.map { it as TextEditor }.map { it.editor }.map {
            val currentFile = documentManager.getFile(it.document)
            if (currentFile != null) {
                if (currentFile == file) {
                    return it
                }
            }
        }
        return null
    }

    /**
     * Utility function that returns the virtual file
     * for a specific document instance
     */
    private fun vFileForDocument(document: Document): VirtualFile? {
        val documentManager = FileDocumentManager.getInstance()
        return documentManager.getFile(document)
    }

    fun updateTestCase(testCase: TestCase) {
        val updatedReport = testJob!!.report
        updatedReport.testCaseList.remove(testCase.testName)
        updatedReport.testCaseList[testCase.testName] = testCase
        updatedReport.normalized()
        project.service<CoverageVisualisationService>().showCoverage(testJob!!.report, editor!!)
    }

    /**
     * Function that calls the services responsible for visualizing
     * coverage and displaying the generated test cases. This
     * is used whenever a new test generation result gets published.
     *
     * @param testJob the new test job
     * @param editor editor instance where coverage should be
     *               visualized
     * @param cacheLazyPipeline the runner that was instantiated but not used to create the test suite
     *                        due to a cache hit, or null if there was a cache miss
     */
    private fun showReport() {
        val visualizationService = project.service<CoverageVisualisationService>()
        val testCaseDisplayService = project.service<TestCaseDisplayService>()
        testCaseDisplayService.showGeneratedTests(testJob!!, editor!!)
        visualizationService.showCoverage(testJob!!.report, editor!!)
    }

    /**
     * Shows the validation result by marking failing test cases.
     *
     * @param validationResult The JUnit result of the validation.
     */
    private fun showValidationResult(validationResult: Validator.JUnitResult) {
        val testCaseDisplayService = project.service<TestCaseDisplayService>()
        testCaseDisplayService.markFailingTestCases(validationResult.failedTestNames)
    }

    /**
     * Function used to update coverage visualization information.
     * Overrides the current visualization state with the one provided.
     * Wrapper over [CoverageVisualisationService.updateCoverage]
     */
    private fun updateCoverage(
        linesToCover: Set<Int>,
        selectedTests: HashSet<String>,
        testCaseList: Report,
        editor: Editor,
    ) {
        val visualizationService = project.service<CoverageVisualisationService>()
        visualizationService.updateCoverage(linesToCover, selectedTests, testCaseList, editor)
    }

    /**
     * Retrieves the last test job from the test generation results for a given file.
     *
     * @param fileName The name of the file for which to retrieve the last test job.
     * @return The last test job generated for the specified file, or null if no test job is found.
     */
    private fun lastTestGeneration(fileName: String): TestJob? {
        return testGenerationData.testGenerationResults[fileName]?.last()
    }

    /**
     * Disposes the listenerDisposable if it is not null.
     */
    override fun dispose() {
        listenerDisposable?.let { Disposer.dispose(it) }
    }
}
