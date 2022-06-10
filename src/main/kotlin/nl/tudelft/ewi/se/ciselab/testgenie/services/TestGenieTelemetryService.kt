package nl.tudelft.ewi.se.ciselab.testgenie.services

import com.google.gson.Gson
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiExpressionStatement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiMethodCallExpression
import java.io.File
import java.io.File.separator
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

        val rawTestCasesToSubmit = mutableListOf<ModifiedTestCase>()

        synchronized(modifiedTestCasesLock) {
            rawTestCasesToSubmit.addAll(modifiedTestCases)
            modifiedTestCases.clear()
        }

        // If there are no tests to submit, do not create a file
        if (rawTestCasesToSubmit.size == 0) {
            return
        }

        ApplicationManager.getApplication().runReadAction {
            val testCasesToSubmit = rawTestCasesToSubmit.map { it.convertToModifiedTestCaseWithAssertions(project) }

            log.info("Submitting ${testCasesToSubmit.size} test cases to a file")

            val gson = Gson()
            val json = gson.toJson(testCasesToSubmit)
            log.info("Submitting test cases: $json")

            writeTelemetryToFile(json)
        }
    }

    /**
     * Writes a json with the telemetry to a file.
     *
     * @param json a json object with the telemetry
     */
    private fun writeTelemetryToFile(json: String) {
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

    abstract class AbstractModifiedTestCase(val original: String, val modified: String)

    class ModifiedTestCase(original: String, modified: String) : AbstractModifiedTestCase(original, modified) {

        /**
         * Calculate the differences in the assertions of the original and modified test code,
         * and convert this ModifiedTestCase to a ModifiedTestCaseWithAssertions.
         *
         * @param project the current project
         * @return a ModifiedTestCaseWithAssertions
         */
        internal fun convertToModifiedTestCaseWithAssertions(project: Project): ModifiedTestCaseWithAssertions {
            val originalTestAssertions = extractAssertions(original, project)
            val modifiedTestAssertions = extractAssertions(modified, project)
            val removedAssertions = originalTestAssertions.minus(modifiedTestAssertions)
            val addedAssertions = modifiedTestAssertions.minus(originalTestAssertions)

            return ModifiedTestCaseWithAssertions(
                this.original,
                this.modified,
                removedAssertions,
                addedAssertions
            )
        }

        /**
         * Extracts assertions from a method.
         *
         * @param testCode the source code of the test
         * @param project the currently open project
         * @return the set of found assertion
         */
        private fun extractAssertions(testCode: String, project: Project): Set<String> {
            val testClass: PsiClass = PsiElementFactory.getInstance(project).createClass("Test")
            val psiMethod: PsiMethod =
                PsiElementFactory.getInstance(project).createMethodFromText(testCode.trim(), testClass)

            val allMethodCalls = psiMethod.body?.children
                ?.filterIsInstance<PsiExpressionStatement>()
                ?.map { it.firstChild }
                ?.filterIsInstance<PsiMethodCallExpression>() ?: listOf()
            val assertions = allMethodCalls.filter { it.firstChild.text.contains("assert") }
            return assertions.map { it.text }.toSet()
        }
    }

    internal class ModifiedTestCaseWithAssertions(
        original: String,
        modified: String,
        val removedAssertions: Set<String>,
        val addedAssertions: Set<String>
    ) :
        AbstractModifiedTestCase(original, modified)
}
