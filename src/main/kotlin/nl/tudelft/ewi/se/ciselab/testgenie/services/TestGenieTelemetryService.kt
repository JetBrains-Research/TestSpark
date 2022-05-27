package nl.tudelft.ewi.se.ciselab.testgenie.services


class TestGenieTelemetryService() {
    private val modifiedTestCases = mutableListOf<ModifiedTestCase>()

    class ModifiedTestCase(val original: String, val modified: String)
}