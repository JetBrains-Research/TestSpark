package nl.tudelft.ewi.se.ciselab.testgenie.services

import com.google.gson.Gson
import com.intellij.openapi.diagnostic.Logger
import java.io.File
import java.sql.Timestamp

class TestGenieTelemetryService {
    private val modifiedTestCases = mutableListOf<ModifiedTestCase>()
    private val modifiedTestCasesLock = Object()

    private val log: Logger = Logger.getInstance(this.javaClass)

    private val telemetryEnabled: Boolean
        get() = TestGenieSettingsService.getInstance().state?.telemetryEnabled ?: false

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
     * Sends the telemetry to the TestGenie server.
     */
    fun uploadTelemetry() {
        if (!telemetryEnabled) {
            return
        }

        val testCasesToUpload = mutableListOf<ModifiedTestCase>()

        synchronized(modifiedTestCasesLock) {
            testCasesToUpload.addAll(modifiedTestCases)
            modifiedTestCases.clear()
        }

        log.info("Uploading ${testCasesToUpload.size} test cases to server")

        val gson = Gson()
        val json = gson.toJson(testCasesToUpload)
        log.info("Uploading test cases: $json")

        marti(json)
    }

    /**
     * Writes a json with the telemetry to a file.
     *
     * @param json a json object with the telemetry
     */
    private fun marti(json: String) {
        // Get the separator depending on the underlying OS
        val separator: String = if (System.getProperty("os.name").contains("Windows")) "\\" else "/"
        // Get the telemetry path
        var dirName: String = TestGenieSettingsService.getInstance().state?.telemetryPath
            ?: System.getProperty("user.dir")
        if (!dirName.endsWith(separator)) dirName = dirName.plus(separator)

        // Create the directory if it does not exist
        val dir = File(dirName)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        // Get the file name based on the current timestamp
        val telemetryFileName: String = dirName.plus(Timestamp(System.currentTimeMillis()).toString())

        log.info("log4j: Saving telemetry into ".plus(telemetryFileName))

        // Write the json to the file
        File(telemetryFileName).bufferedWriter().use { out -> out.write(json) }
    }

    class ModifiedTestCase(val original: String, val modified: String)
}
