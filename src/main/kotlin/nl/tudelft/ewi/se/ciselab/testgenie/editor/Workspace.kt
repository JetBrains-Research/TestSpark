package nl.tudelft.ewi.se.ciselab.testgenie.editor

import com.intellij.openapi.project.Project
import org.evosuite.utils.CompactReport

/**
 * Workspace state service
 */
class Workspace(private val project: Project) {
    data class TestJobKey(val filename: String, var targetUnit: String, val modificationTS: Long, val jobId: String)

    private val testGenerationResults: HashMap<String, ArrayList<Pair<TestJobKey, CompactReport>>> = HashMap()
    private var pendingTestResults: HashMap<String, TestJobKey> = HashMap()

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

        for (e in testGenerationResults) {
            println("File/editor ${e.key}")
            e.value.forEach {
                println("     ${it.first.jobId} ${it.first.targetUnit}")
            }
        }
    }
}
