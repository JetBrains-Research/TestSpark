package nl.tudelft.ewi.se.ciselab.testgenie.services

import com.google.gson.Gson
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class TestGenieTelemetryService(_project: Project) {
    private val project: Project = _project

    private val modifiedTestCases = mutableListOf<ModifiedTestCase>()
    private val modifiedTestCasesLock = Object()

    private val log: Logger = Logger.getInstance(this.javaClass)
    private val dateFormatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")

    private val telemetryEnabled: Boolean
        get() = project.service<SettingsProjectService>().state.telemetryEnabled

    /**
     * Adds test cases to the list of test cases scheduled for telemetry.
     *
     * @param testCases the test cases to add
     */
    fun scheduleTestCasesForTelemetry(testCases: List<ModifiedTestCase>) {
        if (!telemetryEnabled) {
            return
        }

        synchronized(modifiedTestCasesLock) {
            modifiedTestCases.addAll(testCases)
        }
    }

    /**
     * Saves the telemetry to a file.
     */
    fun submitTelemetry() {
        if (!telemetryEnabled) {
            return
        }

        val testCasesToSubmit = mutableListOf<ModifiedTestCase>()

        synchronized(modifiedTestCasesLock) {
            testCasesToSubmit.addAll(modifiedTestCases)
            modifiedTestCases.clear()
        }

        // If there are no tests to submit, do not create a file
        if (testCasesToSubmit.size == 0) {
            return
        }

        log.info("Submiting ${testCasesToSubmit.size} test cases to a file")

        val gson = Gson()
        val json = gson.toJson(testCasesToSubmit)
        log.info("Submiting test cases: $json")

        writeTelemetryToFile(json)
    }

    /**
     * Writes a json with the telemetry to a file.
     *
     * @param json a json object with the telemetry
     */
    private fun writeTelemetryToFile(json: String) {
        // Get the separator depending on the underlying OS
        val separator: String = java.io.File.separator
        // Get the telemetry path
        var dirName: String = project.service<SettingsProjectService>().state.telemetryPath
        if (!dirName.endsWith(separator)) dirName = dirName.plus(separator)

        // Create the directory if it does not exist
        val dir = File(dirName)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        // Get the file name based on the current timestamp
        val currentTime: String = dateFormatter.format(Date())
        val telemetryFileName: String = dirName.plus(currentTime).plus(".json")

        log.info("Saving telemetry into ".plus(telemetryFileName))

        // Write the json to the file
        File(telemetryFileName).bufferedWriter().use { out -> out.write(json) }
    }

    class ModifiedTestCase(val original: String, val modified: String)
}
