package nl.tudelft.ewi.se.ciselab.testgenie.editor

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
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import nl.tudelft.ewi.se.ciselab.testgenie.evosuite.Runner
import nl.tudelft.ewi.se.ciselab.testgenie.services.COVERAGE_SELECTION_TOGGLE_TOPIC
import nl.tudelft.ewi.se.ciselab.testgenie.services.CoverageSelectionToggleListener
import nl.tudelft.ewi.se.ciselab.testgenie.services.CoverageVisualisationService
import nl.tudelft.ewi.se.ciselab.testgenie.services.TestCaseDisplayService
import org.evosuite.utils.CompactReport
import org.evosuite.utils.CompactTestCase

/**
 * Workspace state service
 *
 * Handles user workspace state and modifications of that state
 * related to test generation.
 *
 */
class Workspace(private val project: Project) {
    data class TestJobInfo(val fileUrl: String, var targetUnit: String, val modificationTS: Long, val jobId: String)

    private class TestJob(val info: TestJobInfo, val report: CompactReport, val selectedTests: HashSet<String>) {
        fun getSelectedTests(): List<CompactTestCase> {
            return report.testCaseList.filter { selectedTests.contains(it.key) }.map { it.value }
        }

        fun getSelectedLines(): HashSet<Int> {
            val lineSet: HashSet<Int> = HashSet()
            getSelectedTests().map { lineSet.addAll(it.coveredLines) }
            return lineSet
        }
    }

    private val log = Logger.getInstance(this.javaClass)

    /**
     * Maps a workspace file to the test generation jobs that were triggered on it.
     * Currently, the file key is represented by its presentableUrl
     */
    private val testGenerationResults: HashMap<String, ArrayList<TestJob>> = HashMap()

    /**
     * Maps a test generation job id to its corresponding test job information
     */
    private var pendingTestResults: HashMap<String, TestJobInfo> = HashMap()

    init {
        val connection = project.messageBus.connect()

        // Set event listener for changes to the VFS. The overridden event is
        // triggered whenever the user switches their editor window selection inside the IDE
        connection.subscribe(
            COVERAGE_SELECTION_TOGGLE_TOPIC,
            object : CoverageSelectionToggleListener {
                override fun testGenerationResult(testName: String, selected: Boolean, editor: Editor) {
                    val vFile = vFileForDocument(editor.document) ?: return
                    val fileKey = vFile.presentableUrl
                    val testJob = testGenerationResults[fileKey]?.last() ?: return
                    val modTs = editor.document.modificationStamp

                    if (selected) {
                        testJob.selectedTests.add(testName)
                    } else {
                        testJob.selectedTests.remove(testName)
                    }

                    // update coverage only if the modification timestamp is the same
                    if (testJob.info.modificationTS == modTs) {
                        updateCoverage(testJob.getSelectedLines(), testJob.report, editor)
                    }
                }
            }
        )

        // Set event listener for document changes. These are triggered whenever the user changes
        // the contents of the editor.
        EditorFactory.getInstance().eventMulticaster.addDocumentListener(object : DocumentListener {
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
        }) {}
    }

    /**
     * @param testResultName the test result job id, which is also its file name
     */
    fun addPendingResult(testResultName: String, jobKey: TestJobInfo) {
        pendingTestResults[testResultName] = jobKey
    }

    fun cancelPendingResult(id: String) {
        pendingTestResults.remove(id)
    }

    /**
     * Updates the state after the action of publishing results.
     *
     * @param testResultName the test result job id which was received
     * @param testReport the generated test suite
     * @param cacheLazyRunner the runner that was instantiated but not used to create the test suite
     *                        due to a cache hit, or null if there was a cache miss
     */
    fun receiveGenerationResult(testResultName: String, testReport: CompactReport, cacheLazyRunner: Runner?) {
        val jobKey = pendingTestResults.remove(testResultName)!!

        val resultsForFile = testGenerationResults.getOrPut(jobKey.fileUrl) { ArrayList() }
        val displayedSet = HashSet<String>()
        displayedSet.addAll(testReport.testCaseList.keys)
        resultsForFile.add(TestJob(jobKey, testReport, displayedSet))

        val editor = editorForFileUrl(jobKey.fileUrl)

        if (editor != null) {
            showReport(testReport, editor, cacheLazyRunner)
        } else {
            log.info("No editor opened for received test result")
        }
    }

    /**
     * Utility function that returns the editor for a specific file url,
     * in case it is opened in the IDE
     */
    private fun editorForFileUrl(fileUrl: String): Editor? {
        val documentManager = FileDocumentManager.getInstance()
        // https://intellij-support.jetbrains.com/hc/en-us/community/posts/360004480599/comments/360000703299
        FileEditorManager.getInstance(project).allEditors.map { it as TextEditor }.map { it.editor }.map {
            val currentFile = documentManager.getFile(it.document)
            if (currentFile != null) {
                if (currentFile.presentableUrl == fileUrl) {
                    return it
                }
            }
        }
        return null
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

    /**
     * Function that calls the services responsible for visualizing
     * coverage and displaying the generated test cases. This
     * is used whenever a new test generation result gets published.
     *
     * @param testReport the new test report
     * @param editor editor instance where coverage should be
     *               visualized
     * @param cacheLazyRunner the runner that was instantiated but not used to create the test suite
     *                        due to a cache hit, or null if there was a cache miss
     */
    private fun showReport(testReport: CompactReport, editor: Editor, cacheLazyRunner: Runner?) {
        val visualizationService = project.service<CoverageVisualisationService>()
        val testCaseDisplayService = project.service<TestCaseDisplayService>()
        testCaseDisplayService.showGeneratedTests(testReport, editor, cacheLazyRunner)
        visualizationService.showCoverage(testReport, editor)
    }

    private fun updateCoverage(
        linesToCover: Set<Int>,
        testCaseList: CompactReport,
        editor: Editor
    ) {
        val visualizationService = project.service<CoverageVisualisationService>()
        visualizationService.updateCoverage(linesToCover, testCaseList, editor)
    }

    private fun lastTestGeneration(fileName: String): TestJob? {
        return testGenerationResults[fileName]?.last()
    }
}
