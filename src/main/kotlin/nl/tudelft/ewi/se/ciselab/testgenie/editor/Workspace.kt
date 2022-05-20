package nl.tudelft.ewi.se.ciselab.testgenie.editor

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import nl.tudelft.ewi.se.ciselab.testgenie.services.CoverageVisualisationService
import org.evosuite.utils.CompactReport

/**
 * Workspace state service
 *
 * Handles user workspace state and modifications of that state
 * related to test generation.
 *
 */
class Workspace(private val project: Project) {
    data class TestJobInfo(val filename: String, var targetUnit: String, val modificationTS: Long, val jobId: String)

    private val log = Logger.getInstance(this.javaClass)

    /**
     * Maps a workspace file to the test generation jobs that were triggered on it.
     * Currently, the file key is represented by its presentableUrl
     */
    private val testGenerationResults: HashMap<String, ArrayList<Pair<TestJobInfo, CompactReport>>> = HashMap()

    /**
     * Maps a test generation job id to its corresponding test job information
     */
    private var pendingTestResults: HashMap<String, TestJobInfo> = HashMap()

    init {
        val connection = project.messageBus.connect()

        // Set event listener for changes to the VFS. The overridden event is
        // triggered whenever the user switches their editor window selection inside the IDE
        connection.subscribe(
            FileEditorManagerListener.FILE_EDITOR_MANAGER,
            object : FileEditorManagerListener {
                override fun selectionChanged(event: FileEditorManagerEvent) {
                    super.selectionChanged(event)
                    val file = event.newFile
                    val fileUrl = file.presentableUrl
                    // check if file has any tests generated for it
                    val list = testGenerationResults[fileUrl] ?: return
                    val lastTest = list.lastOrNull() ?: return

                    // check if file is in same state so that coverage visualization is valid
                    if (lastTest.first.modificationTS == file.modificationStamp) {
                        // get editor for file
                        val editor = (event.newEditor as TextEditor).editor
                        showCoverage(lastTest.second, editor)
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

                val job = lastTestGeneration(fileName) ?: return

                if (job.first.modificationTS == modTs) {
                    val editor = editorForVFile(file) ?: return

                    showCoverage(job.second, editor)
                } else {
                    val editor = editorForVFile(file)

                    editor?.markupModel?.removeAllHighlighters()
                }
            }
        }) {}
    }

    fun lastTestGeneration(fileName: String): Pair<TestJobInfo, CompactReport>? {
        return testGenerationResults[fileName]?.last()
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
     */
    fun receiveGenerationResult(testResultName: String, testReport: CompactReport) {
        val jobKey = pendingTestResults.remove(testResultName)!!

        val resultsForFile = testGenerationResults.getOrPut(jobKey.filename) { ArrayList() }
        resultsForFile.add(Pair(jobKey, testReport))

        val editor = editorForFileUrl(jobKey.filename)

        if (editor != null) {
            if (editor.document.modificationStamp == jobKey.modificationTS) {
                showCoverage(testReport, editor)
            }
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
    fun editorForVFile(file: VirtualFile): Editor? {
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

    private fun showCoverage(testReport: CompactReport, editor: Editor) {
        val visualizationService = project.service<CoverageVisualisationService>()
        visualizationService.showCoverage(testReport, editor)
    }
}
