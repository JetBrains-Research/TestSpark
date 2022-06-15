package nl.tudelft.ewi.se.ciselab.testgenie.services

import com.intellij.openapi.diagnostic.Logger
import nl.tudelft.ewi.se.ciselab.testgenie.editor.Workspace
import org.evosuite.utils.CompactReport
import org.evosuite.utils.CompactTestCase

class TestCaseCachingService {
    private val log: Logger = Logger.getInstance(this.javaClass)
    private val files = mutableMapOf<String, FileTestCaseCache>()
    private val filesLock = Object()

    /**
     * Insert test cases into the cache.
     *
     * @param fileUrl the URL of the file that the test cases belong to
     * @param report the report containing the test cases
     * @param jobInfo the TestJobInfo for this report
     */
    fun putIntoCache(fileUrl: String, report: CompactReport, jobInfo: Workspace.TestJobInfo) {
        log.info("Inserting ${report.testCaseList.size} test cases into cache for $fileUrl")
        val file = getFileTestCaseCache(fileUrl)
        file.putIntoCache(report, jobInfo)
    }

    /**
     * Retrieve test cases from the cache that cover at least one line in the specified range.
     *
     * @param fileUrl the URL of the file that the test cases belong to
     * @param lineFrom the start of the range
     * @param lineTo the end of the range
     * @return a list of test cases
     */
    fun retrieveFromCache(fileUrl: String, lineFrom: Int, lineTo: Int): List<CompactTestCase> {
        val fileTestCaseCache = getFileTestCaseCache(fileUrl)
        val result = fileTestCaseCache.retrieveFromCache(lineFrom, lineTo)
        log.info("Retrieved ${result.size} test cases from cache for $fileUrl")
        return result
    }

    /**
     * Invalidate test cases from the cache.
     *
     * @param fileUrl the URL of the file that the test cases belong to
     * @param lineFrom the start of the range
     * @param lineTo the end of the ranges
     */
    fun invalidateFromCache(fileUrl: String, lineFrom: Int, lineTo: Int) {
        val fileTestCaseCache = getFileTestCaseCache(fileUrl)
        fileTestCaseCache.invalidateFromCache(lineFrom, lineTo)
    }

    /**
     * Invalidate test case from the cache.
     *
     * @param fileUrl the URL of the file that the test cases belong to
     * @param testCode the code of the test case
     */
    fun invalidateFromCache(fileUrl: String, testCode: String) {
        val fileTestCaseCache = getFileTestCaseCache(fileUrl)
        fileTestCaseCache.invalidateFromCache(testCode)
    }

    /**
     * Retrieve the TestJobInfo for the specified test.
     *
     * @param fileUrl the URL of the file that the test cases belong to
     * @param testCode the code of the test case
     */
    fun getTestJobInfo(fileUrl: String, testCode: String): Workspace.TestJobInfo? {
        val fileTestCaseCache = getFileTestCaseCache(fileUrl)
        return fileTestCaseCache.getTestJobInfo(testCode)
    }

    /**
     * Get the file test case cache for the specified file.
     *
     * @param fileUrl the URL of the file
     * @return the file test case cache
     */
    private fun getFileTestCaseCache(fileUrl: String): FileTestCaseCache {
        synchronized(filesLock) {
            return files.getOrPut(fileUrl) { FileTestCaseCache() }
        }
    }

    /**
     * A data structure keeping track of the cached test cases in a file.
     */
    private class FileTestCaseCache {
        private val lines = mutableMapOf<Int, LineTestCaseCache>()
        private val linesLock = Object()

        // Used for retrieving references of unique test cases
        private val caseIndex = mutableMapOf<String, CachedCompactTestCase>()
        private val caseIndexLock = Object()

        /**
         * Insert test cases into the file cache.
         *
         * @param report the report containing the test cases
         * @param jobInfo the TestJobInfo for this test report
         */
        fun putIntoCache(report: CompactReport, jobInfo: Workspace.TestJobInfo) {
            report.testCaseList.values.forEach { testCase ->
                val cachedCompactTestCase = CachedCompactTestCase.fromCompactTestCase(testCase, this, jobInfo)

                synchronized(caseIndexLock) {
                    // invalidate existing test with the same code if one exists
                    caseIndex[cachedCompactTestCase.testCode]?.invalidate()

                    // save new test in index
                    caseIndex[cachedCompactTestCase.testCode] = cachedCompactTestCase
                }

                testCase.coveredLines.forEach { lineNumber ->
                    val line: LineTestCaseCache = getLineTestCaseCache(lineNumber)
                    line.putIntoCache(cachedCompactTestCase)
                }
            }
        }

        /**
         * Retrieve test cases from the file cache that cover at least one line in the specified range.
         *
         * @param lineFrom the start of the range
         * @param lineTo the end of the range
         * @return the test cases that cover at least one line in the specified range
         */
        fun retrieveFromCache(lineFrom: Int, lineTo: Int): List<CompactTestCase> {
            val result = mutableSetOf<CachedCompactTestCase>()
            for (lineNumber in lineFrom..lineTo) {
                val line: LineTestCaseCache = getLineTestCaseCache(lineNumber)
                result.addAll(line.getTestCases())
            }

            return result.map { it.toCompactTestCase() }
        }

        /**
         * Invalidate test cases from cache.
         *
         * @param lineFrom the start of the range
         * @param lineTo the end of the range
         */
        fun invalidateFromCache(lineFrom: Int, lineTo: Int) {
            for (lineNumber in lineFrom..lineTo) {
                val tests: LineTestCaseCache = getLineTestCaseCache(lineNumber)
                for (test in tests.getTestCases()) {
                    test.invalidate()
                }
            }
        }

        /**
         * Invalidate test cases from cache.
         *
         * @param testCode the code of the test case
         */
        fun invalidateFromCache(testCode: String) {
            synchronized(caseIndexLock) {
                caseIndex[testCode]?.invalidate()
            }
        }

        /**
         * Retrieve the TestJobInfo for the specified test.
         *
         * @param testCode the code of the test case
         */
        fun getTestJobInfo(testCode: String): Workspace.TestJobInfo? {
            synchronized(caseIndexLock) {
                return caseIndex[testCode]?.jobInfo
            }
        }

        /**
         * Get the line test case cache for the specified line.
         * @return the line test case cache
         */
        fun getLineTestCaseCache(lineNumber: Int): LineTestCaseCache {
            synchronized(linesLock) {
                return lines.getOrPut(lineNumber) {
                    // The line number is included here so that it can easily be modified
                    // in all instances when line numbers change
                    LineTestCaseCache(lineNumber)
                }
            }
        }

        /**
         * Remove a test case from the test case index.
         *
         * @param cachedCompactTestCase the test case to remove
         */
        fun removeTestCaseFromIndex(cachedCompactTestCase: CachedCompactTestCase) {
            synchronized(caseIndexLock) {
                caseIndex.remove(cachedCompactTestCase.testCode)
            }
        }
    }

    /**
     * A data structure keeping track of the cached test cases in a line.
     * Furthermore, this structure keeps track of the line number of a particular line as it changes.
     * If the line number of this structure is updated, this will automatically be reflected in all
     * cached test cases.
     */
    private class LineTestCaseCache(var lineNumber: Int) {
        private val testCases = mutableListOf<CachedCompactTestCase>()
        private val testCasesLock = Object()

        /**
         * Insert a test case into the line cache.
         *
         * @param testCase the test case to insert
         */
        fun putIntoCache(testCase: CachedCompactTestCase) {
            synchronized(testCasesLock) {
                testCases.add(testCase)
            }
        }

        /**
         * Get the test cases that cover this line.
         *
         * @return the test cases that cover this line
         */
        fun getTestCases(): List<CachedCompactTestCase> {
            synchronized(testCasesLock) {
                return testCases.toList()
            }
        }

        /**
         * Remove a test case from this line cache.
         *
         * @param cachedCompactTestCase the test case to remove
         */
        fun removeTestCase(cachedCompactTestCase: CachedCompactTestCase) {
            synchronized(testCasesLock) {
                testCases.remove(cachedCompactTestCase)
            }
        }
    }

    /**
     * A data structure keeping track of a cached test case.
     */
    private class CachedCompactTestCase(
        val testName: String,
        val testCode: String,
        val jobInfo: Workspace.TestJobInfo,
        private val coveredLines: Set<LineTestCaseCache>,
        private val fileTestCaseCache: FileTestCaseCache
    ) {
        /**
         * Convert this cached test case back to a compact test case.
         *
         * @return the compact test case
         */
        fun toCompactTestCase(): CompactTestCase {
            return CompactTestCase(
                "$testName (from cache ${testCode.hashCode()})",
                testCode,
                coveredLines.map { it.lineNumber }.toSet(),
                // empty mutation and branch coverage as this is not calculated dynamically
                setOf(),
                setOf()
            )
        }

        /**
         * Remove this test case from the cache.
         */
        fun invalidate() {
            // Remove from index
            fileTestCaseCache.removeTestCaseFromIndex(this)

            // Remove from line caches
            this.coveredLines.forEach {
                it.removeTestCase(this)
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CachedCompactTestCase

            if (testName != other.testName) return false
            if (testCode != other.testCode) return false
            if (coveredLines != other.coveredLines) return false

            return true
        }

        override fun hashCode(): Int {
            var result = testName.hashCode()
            result = 31 * result + testCode.hashCode()
            result = 31 * result + coveredLines.hashCode()
            return result
        }

        companion object {

            /**
             * Create a cached test case from a compact test case.
             *
             * @param testCase the compact test case
             * @param fileTestCaseCache the file test case cache (in order to retrieve line cache references)
             * @param jobInfo the TestJobInfo for this test
             * @return the cached test case
             */
            fun fromCompactTestCase(
                testCase: CompactTestCase,
                fileTestCaseCache: FileTestCaseCache,
                jobInfo: Workspace.TestJobInfo
            ): CachedCompactTestCase {
                return CachedCompactTestCase(
                    testCase.testName,
                    testCase.testCode.replace("\r\n", "\n"),
                    jobInfo,
                    testCase.coveredLines.map {
                        fileTestCaseCache.getLineTestCaseCache(it)
                    }.toSet(),
                    fileTestCaseCache
                )
            }
        }
    }
}
