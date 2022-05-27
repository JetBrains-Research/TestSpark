package nl.tudelft.ewi.se.ciselab.testgenie.services

import com.google.gson.Gson
import com.intellij.openapi.diagnostic.Logger

class TestGenieTelemetryService() {
    private val modifiedTestCases = mutableListOf<ModifiedTestCase>()

    private val log: Logger = Logger.getInstance(this.javaClass)

    fun scheduleTestCasesForTelemetry(testCases: List<ModifiedTestCase>) {
        modifiedTestCases.addAll(testCases)
    }

    fun uploadScheduledTestCases() {
        val testCasesToUpload = mutableListOf<ModifiedTestCase>()

        testCasesToUpload.addAll(modifiedTestCases)
        modifiedTestCases.clear()

        val gson = Gson()
        val json = gson.toJson(testCasesToUpload)

        // TODO: Actually upload test cases to server
    }

    class ModifiedTestCase(val original: String, val modified: String)
}