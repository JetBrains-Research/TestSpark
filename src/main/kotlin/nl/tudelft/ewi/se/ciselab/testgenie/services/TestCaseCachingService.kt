package nl.tudelft.ewi.se.ciselab.testgenie.services

import com.intellij.openapi.diagnostic.Logger
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
     */
    fun putIntoCache(fileUrl: String, report: CompactReport) {
        log.info("Inserting ${report.testCaseList.size} test cases into cache for $fileUrl")
        val file = getFileTestCaseCache(fileUrl)
        file.putIntoCache(report)
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

        /**
         * Insert test cases into the file cache.
         *
         * @param report the report containing the test cases
         */
        fun putIntoCache(report: CompactReport) {
            report.testCaseList.values.forEach { testCase ->
                testCase.coveredLines.forEach { lineNumber ->
                    val line: LineTestCaseCache = getLineTestCaseCache(lineNumber)

                    val cachedCompactTestCase = CachedCompactTestCase.fromCompactTestCase(testCase, this)
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
    }

    /**
     * A data structure keeping track of a cached test case.
     */
    private class CachedCompactTestCase(
        val testName: String,
        val testCode: String,
        private val coveredLines: Set<LineTestCaseCache>
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

        companion object {

            /**
             * Create a cached test case from a compact test case.
             *
             * @param testCase the compact test case
             * @param fileTestCaseCache the file test case cache (in order to retrieve line cache references)
             * @return the cached test case
             */
            fun fromCompactTestCase(
                testCase: CompactTestCase,
                fileTestCaseCache: FileTestCaseCache
            ): CachedCompactTestCase {
                return CachedCompactTestCase(
                    testCase.testName,
                    testCase.testCode,
                    testCase.coveredLines.map {
                        fileTestCaseCache.getLineTestCaseCache(it)
                    }.toSet()
                )
            }
        }
    }
}
