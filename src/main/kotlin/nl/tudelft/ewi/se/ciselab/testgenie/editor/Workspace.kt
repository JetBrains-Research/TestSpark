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
 */
class Workspace(private val project: Project) {
    data class TestJobKey(val filename: String, var targetUnit: String, val modificationTS: Long, val jobId: String)

    private val log = Logger.getInstance(this.javaClass)

    private val testGenerationResults: HashMap<String, ArrayList<Pair<TestJobKey, CompactReport>>> = HashMap()
    private var pendingTestResults: HashMap<String, TestJobKey> = HashMap()

    init {
        val connection = project.messageBus.connect()

        // listen for editor editor switches
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
                        updateEditorCoverageDisplay(lastTest.second, editor)
                    }
                }
            }
        )

        // listen for document changes
        EditorFactory.getInstance().eventMulticaster.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                super.documentChanged(event)

                val file = FileDocumentManager.getInstance().getFile(event.document) ?: return
                val fileName = file.presentableUrl
                val modTs = event.document.modificationStamp

                val job = lastTestGeneration(fileName) ?: return

                if (job.first.modificationTS == modTs) {
                    val editor = editorForVFile(file) ?: return

                    updateEditorCoverageDisplay(job.second, editor)
                } else {
                    val editor = editorForVFile(file)

                    editor?.markupModel?.removeAllHighlighters()
                }
            }
        }) {}
    }

    fun lastTestGeneration(fileName: String): Pair<TestJobKey, CompactReport>? {
        return testGenerationResults[fileName]?.last()
    }

    fun addPendingResult(id: String, jobKey: TestJobKey) {
        pendingTestResults[id] = jobKey
    }

    fun cancelPendingResult(id: String) {
        pendingTestResults.remove(id)
    }

    fun receiveGenerationResult(id: String, testReport: CompactReport) {
        val jobKey = pendingTestResults.remove(id)!!

        val resultsForFile = testGenerationResults.getOrPut(jobKey.filename) { ArrayList() }
        resultsForFile.add(Pair(jobKey, testReport))

        val editor = editorForFileUrl(jobKey.filename)

        if (editor != null) {
            if (editor.document.modificationStamp == jobKey.modificationTS) {
                updateEditorCoverageDisplay(testReport, editor)
            }
        } else {
            log.info("No editor opened for received test result")
        }
    }

    // Returns the instance of the currently opened editor for a virtual file
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

    // Returns the instance of the currently opened editor for a virtual file
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

    private fun updateEditorCoverageDisplay(testReport: CompactReport, editor: Editor) {
        val visualizationService = project.service<CoverageVisualisationService>()
        visualizationService.showCoverage(testReport, editor)
    }
}
