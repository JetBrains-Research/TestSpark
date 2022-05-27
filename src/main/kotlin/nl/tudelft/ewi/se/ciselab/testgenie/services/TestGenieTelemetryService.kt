package nl.tudelft.ewi.se.ciselab.testgenie.services

class TestGenieTelemetryService() {
    private val modifiedTestCases = mutableListOf<ModifiedTestCase>()

    fun scheduleTestCasesForTelemetry(testCases: List<ModifiedTestCase>) {
        modifiedTestCases.addAll(testCases)
    }

    class ModifiedTestCase(val original: String, val modified: String)
}