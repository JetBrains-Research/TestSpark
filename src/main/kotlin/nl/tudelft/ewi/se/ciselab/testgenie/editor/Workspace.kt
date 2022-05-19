package nl.tudelft.ewi.se.ciselab.testgenie.editor

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
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
        project.messageBus.connect().subscribe(
            FileEditorManagerListener.FILE_EDITOR_MANAGER,
            object : FileEditorManagerListener {
                override fun selectionChanged(event: FileEditorManagerEvent) {
                    super.selectionChanged(event)
                    val file = event.newFile
                    val fileUrl = file.presentableUrl
                    // check if file has any tests generated for it
                    val list = testGenerationResults[fileUrl] ?: return
                    val lastTest = list.lastOrNull() ?: return
                    // get editor for file
                    val editor = (event.newEditor as TextEditor).editor
                    log.info("Displaying test report on freshly opened editor for target ${lastTest.first.targetUnit}")
                    updateEditorCoverageDisplay(lastTest.second, editor)
                }
            }
        )
    }

    fun addPendingResult(id: String, jobKey: TestJobKey) {
        pendingTestResults[id] = jobKey
    }

    fun cancelPendingResult(id: String) {
        pendingTestResults.remove(id)
    }

    fun receiveGenerationResult(id: String, testReport: CompactReport) {
        val jobKey = pendingTestResults.remove(id)!! // TODO: throw exception

        val resultsForFile = testGenerationResults.getOrPut(jobKey.filename) { ArrayList() }
        resultsForFile.add(Pair(jobKey, testReport))

        val editor = editorForFileUrl(jobKey.filename)
        if (editor != null) {
            updateEditorCoverageDisplay(testReport, editor)
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
