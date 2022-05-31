package nl.tudelft.ewi.se.ciselab.testgenie.services

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.evosuite.utils.CompactReport
import org.evosuite.utils.CompactTestCase

class TestCaseCachingService(private val project: Project) {
    private val log: Logger = Logger.getInstance(this.javaClass)
    private val files = mutableMapOf<String, FileTestCaseCache>()
    private val filesLock = Object()

    fun putIntoCache(fileUrl: String, report: CompactReport) {
        val file = getFileTestCaseCache(fileUrl)
        file.putIntoCache(report)
    }

    fun retrieveFromCache(fileUrl: String, lineFrom: Int, lineTo: Int): List<CompactTestCase> {
        val fileTestCaseCache = getFileTestCaseCache(fileUrl)

        return fileTestCaseCache.retrieveFromCache(lineFrom, lineTo)
    }

    private fun getFileTestCaseCache(fileUrl: String): FileTestCaseCache {
        synchronized(filesLock) {
            return files.getOrPut(fileUrl) { FileTestCaseCache() }
        }
    }

    private class FileTestCaseCache {
        private val lines = mutableMapOf<Int, LineTestCaseCache>()
        private val linesLock = Object()

        fun putIntoCache(report: CompactReport) {
            report.testCaseList.values.forEach { testCase ->
                testCase.coveredLines.forEach { lineNumber ->
                    val line: LineTestCaseCache = getLineTestCaseCache(lineNumber)

                    val cachedCompactTestCase = CachedCompactTestCase.fromCompactTestCase(testCase, this)
                    line.putIntoCache(cachedCompactTestCase)
                }
            }
        }

        fun retrieveFromCache(lineFrom: Int, lineTo: Int): List<CompactTestCase> {
            val result = mutableListOf<CachedCompactTestCase>()
            for (lineNumber in lineFrom..lineTo) {
                val line: LineTestCaseCache = getLineTestCaseCache(lineNumber)
                result.addAll(line.getTestCases())
            }

            return result.map { it.toCompactTestCase() }
        }

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

    private class LineTestCaseCache(var lineNumber: Int) {
        private val testCases = mutableListOf<CachedCompactTestCase>()
        private val testCasesLock = Object()

        fun putIntoCache(testCase: CachedCompactTestCase) {
            synchronized(testCasesLock) {
                testCases.add(testCase)
            }
        }

        fun getTestCases(): List<CachedCompactTestCase> {
            synchronized(testCasesLock) {
                return testCases.toList()
            }
        }
    }

    private class CachedCompactTestCase(
        val testName: String,
        val testCode: String,
        private val coveredLines: Set<LineTestCaseCache>
    ) {

        fun toCompactTestCase(): CompactTestCase {
            return CompactTestCase(
                "$testName (cached, ${testCode.hashCode()})",
                testCode,
                coveredLines.map { it.lineNumber }.toSet(),
                // empty mutation and branch coverage as this is not calculated dynamically
                setOf(),
                setOf()
            )
        }

        companion object {
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
