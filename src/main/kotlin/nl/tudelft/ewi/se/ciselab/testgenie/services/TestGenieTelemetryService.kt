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

        // TODO: Actually upload test cases to a file
        File(Timestamp(System.currentTimeMillis()).toString()).bufferedWriter().use { out -> out.write(json) }
    }

    class ModifiedTestCase(val original: String, val modified: String)
}
