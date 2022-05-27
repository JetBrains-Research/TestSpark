package nl.tudelft.ewi.se.ciselab.testgenie.services

import com.google.gson.Gson
import com.intellij.openapi.diagnostic.Logger

class TestGenieTelemetryService() {
    private val modifiedTestCases = mutableListOf<ModifiedTestCase>()
    private val modifiedTestCasesLock = Object()

    private val log: Logger = Logger.getInstance(this.javaClass)

    fun scheduleTestCasesForTelemetry(testCases: List<ModifiedTestCase>) {
        synchronized(modifiedTestCasesLock) {
            modifiedTestCases.addAll(testCases)
        }
    }

    fun uploadScheduledTestCases() {
        val testCasesToUpload = mutableListOf<ModifiedTestCase>()

        synchronized(modifiedTestCasesLock) {
            testCasesToUpload.addAll(modifiedTestCases)
            modifiedTestCases.clear()
        }

        log.info("Uploading ${testCasesToUpload.size} test cases to server")

        val gson = Gson()
        val json = gson.toJson(testCasesToUpload)
        log.trace("Uploading test cases: $json")

        // TODO: Actually upload test cases to server
    }

    class ModifiedTestCase(val original: String, val modified: String)
}