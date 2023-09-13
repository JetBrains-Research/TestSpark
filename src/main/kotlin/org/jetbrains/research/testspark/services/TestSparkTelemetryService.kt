package org.jetbrains.research.testspark.services

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDeclarationStatement
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiExpressionStatement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiMethodCallExpression
import org.jetbrains.research.testspark.data.TestCase
import org.jetbrains.research.testspark.data.TestCaseRate
import java.io.File
import java.io.File.separator
import java.text.SimpleDateFormat
import java.util.Date

class TestSparkTelemetryService(project: Project) {
    private val projectDuplicate: Project = project

    private val modifiedTestCases = mutableListOf<ModifiedTestCase>()
    private val modifiedTestCasesLock = Object()

    private val feedbackTestCases: HashMap<String, FeedbackTestCaseInfo> = HashMap()
    private val feedbackTestCasesLock = Object()

    private val log: Logger = Logger.getInstance(this.javaClass)
    private val dateFormatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")

    private val telemetryEnabled: Boolean
        get() = projectDuplicate.service<SettingsProjectService>().state.telemetryEnabled

    private val feedbackTelemetryEnabled: Boolean
        get() = projectDuplicate.service<SettingsProjectService>().state.feedbackTelemetryEnabled

    private val likedSuffix: String = ".liked"
    private val dislikedSuffix: String = ".disliked"

    /**
     * Update an entry in the hash table that stores information about liked/disliked test cases.
     *
     * @param testCase source test case to save info about
     * @param rate rate provided by user
     */
    fun updateFeedbackEntry(testCase: TestCase, rate: TestCaseRate) {
        if (feedbackTestCases.containsKey(testCase.testName)) {
            // we want to analyze initially generated code,
            // so no need to update anything except rate if the entry exists
            feedbackTestCases[testCase.testName]!!.rate = rate
        } else {
            feedbackTestCases[testCase.testName] = FeedbackTestCaseInfo(testCase.testName, testCase.testCode, rate)
        }
    }

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
    fun submitModificationTelemetry() {
        val rawTestCasesToSubmit = moveTelemetryToRawList(telemetryEnabled, modifiedTestCasesLock, modifiedTestCases)

        // If there are no tests to submit, do not create a file
        if (rawTestCasesToSubmit.size == 0) {
            return
        }

        ApplicationManager.getApplication().runReadAction {
            val testCasesToSubmit = rawTestCasesToSubmit.map { it.convertToModifiedTestCaseSerializable(projectDuplicate) }

            log.info("Submitting ${testCasesToSubmit.size} test cases to a file")

            val gson = Gson()
            val json = gson.toJson(testCasesToSubmit)
            log.info("Submitting test cases: $json")

            writeTelemetryToFile(json)
        }
    }

    fun submitFeedbackTelemetry() {
        val rawFeedbackToSubmit = moveTelemetryToRawList(feedbackTelemetryEnabled, feedbackTestCasesLock, feedbackTestCases.values)

        // If there are no tests to submit, do not create a file
        if (rawFeedbackToSubmit.size == 0) {
            return
        }

        val writer = { fileSuffix: String, rate: TestCaseRate ->
            ApplicationManager.getApplication().runReadAction {
                val ratedTestCases = rawFeedbackToSubmit.filter { it.rate == rate }

                log.info("Submitting ${ratedTestCases.size} feedback test cases to a file")

                // expose annotation hides "rate" field of FeedbackTelemetryInfo,
                // since the data is anyway serialized into separate files
                val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                val json = gson.toJson(ratedTestCases)
                log.info("Submitting test cases: $json")

                writeTelemetryToFile(json, fileSuffix)
            }
        }

        writer(likedSuffix, TestCaseRate.LIKE)
        writer(dislikedSuffix, TestCaseRate.DISLIKE)
    }

    /**
     * Move all data from source list to a copy and clean the source list. It is a first step of submitting the telemetry data.
     *
     * @param isTelemetryEnabled boolean flag defining whether the certain telemetry is enabled in settings
     * @param lock object used for synchronization
     * @param sourceList list with telemetry data to move values from
     */
    private fun <T> moveTelemetryToRawList(isTelemetryEnabled: Boolean, lock: Any, sourceList: MutableCollection<T>): MutableList<T> {
        if (!isTelemetryEnabled) {
            return mutableListOf()
        }

        val rawTelemetryToSubmit = mutableListOf<T>()

        synchronized(lock) {
            rawTelemetryToSubmit.addAll(sourceList)
            sourceList.clear()
        }

        return rawTelemetryToSubmit
    }

    /**
     * Writes a json with the telemetry to a file.
     *
     * @param json a json object with the telemetry
     * @param nameSuffix optional name suffix for the resulting file name
     */
    private fun writeTelemetryToFile(json: String, nameSuffix: String = "") {
        // Get the telemetry path
        var dirName: String = projectDuplicate.service<SettingsProjectService>().state.telemetryPath
        if (!dirName.endsWith(separator)) dirName = dirName.plus(separator)

        // Create the directory if it does not exist
        val dir = File(dirName)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        // Get the file name based on the current timestamp
        val currentTime: String = dateFormatter.format(Date())
        val telemetryFileName: String = dirName.plus(currentTime).plus(nameSuffix).plus(".json")

        log.info("Saving telemetry into ".plus(telemetryFileName))

        // Write the json to the file
        File(telemetryFileName).bufferedWriter().use { out -> out.write(json) }
    }

    abstract class AbstractModifiedTestCase(val original: String, val modified: String)

    class ModifiedTestCase(original: String, modified: String) : AbstractModifiedTestCase(original, modified) {

        /**
         * Calculate the differences in the assertions and variable declarations of the original and modified test code,
         * and convert this ModifiedTestCase to a ModifiedTestCaseWithAssertions.
         *
         * @param project the current project
         * @return a ModifiedTestCaseWithAssertions class that contains:
         *          - the original test case
         *          - modified test case
         *          - removed assertions
         *          - added assertions
         *          - removed variable declarations
         *          - added variable declarations
         */
        internal fun convertToModifiedTestCaseSerializable(project: Project): ModifiedTestCaseSerializable {
            // Create a dummy class to be a context for test methods
            val testClass: PsiClass = PsiElementFactory.getInstance(project).createClass("Test")

            // Create PSI methods for the original and modified tests
            val originalTest: PsiMethod = PsiElementFactory.getInstance(project).createMethodFromText(original.trim(), testClass)
            val modifiedTest: PsiMethod = PsiElementFactory.getInstance(project).createMethodFromText(modified.trim(), testClass)

            // Get the removed and added assertions
            val originalTestAssertions = extractAssertions(originalTest)
            val modifiedTestAssertions = extractAssertions(modifiedTest)
            val removedAssertions = originalTestAssertions.minus(modifiedTestAssertions)
            val addedAssertions = modifiedTestAssertions.minus(originalTestAssertions)

            // Get the removed and added variable declarations
            val originalVariableDeclarations = extractVariableDeclarations(originalTest)
            val modifiedVariableDeclarations = extractVariableDeclarations(modifiedTest)
            val removedVariableDeclarations = originalVariableDeclarations.minus(modifiedVariableDeclarations)
            val addedVariableDeclarations = modifiedVariableDeclarations.minus(originalVariableDeclarations)

            return ModifiedTestCaseSerializable(
                this.original,
                this.modified,
                removedAssertions,
                addedAssertions,
                removedVariableDeclarations,
                addedVariableDeclarations,
            )
        }

        /**
         * Extracts assertions from a test case.
         *
         * @param testCase the test case in the form of a PSI method
         * @return the set of found assertion
         */
        private fun extractAssertions(testCase: PsiMethod): Set<String> {
            val allMethodCalls = testCase.body?.children
                ?.filterIsInstance<PsiExpressionStatement>()
                ?.map { it.firstChild }
                ?.filterIsInstance<PsiMethodCallExpression>() ?: listOf()
            val assertions = allMethodCalls.filter { it.firstChild.text.contains("assert") }
            return assertions.map { it.text }.toSet()
        }

        /**
         * Extracts variable declarations from a test case.
         *
         * @param testCase the test case in the form of a PSI method
         * @return the set of found variable declarations
         */
        private fun extractVariableDeclarations(testCase: PsiMethod): Set<String> {
            return testCase.body?.children
                ?.filterIsInstance<PsiDeclarationStatement>()
                ?.map { it.text }
                ?.toSet() ?: setOf()
        }
    }

    @Suppress("unused")
    internal class ModifiedTestCaseSerializable(
        original: String,
        modified: String,
        val removedAssertions: Set<String>,
        val addedAssertions: Set<String>,
        val removedVariableDeclarations: Set<String>,
        val addedVariableDeclarations: Set<String>,
    ) :
        AbstractModifiedTestCase(original, modified)

    data class FeedbackTestCaseInfo(@Expose val name: String, @Expose val code: String, var rate: TestCaseRate)
}
