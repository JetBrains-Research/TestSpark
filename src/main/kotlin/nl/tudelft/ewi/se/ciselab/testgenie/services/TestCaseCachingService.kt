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

    private fun getFileTestCaseCache(fileUrl: String): FileTestCaseCache {
        synchronized(filesLock) {
            return files.getOrPut(fileUrl) { FileTestCaseCache() }
        }
    }

    private class FileTestCaseCache {
        val lines = mutableMapOf<Int, LineTestCaseCache>()
        val linesLock = Object()

        fun putIntoCache(report: CompactReport) {
            report.testCaseList.values.forEach { testCase ->
                testCase.coveredLines.forEach { lineNumber ->
                    val line: LineTestCaseCache = getLineTestCaseCache(lineNumber)

                    val cachedCompactTestCase = CachedCompactTestCase.fromCompactTestCase(testCase, this)
                    line.putIntoCache(cachedCompactTestCase)
                }
            }
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
        val testCases = mutableListOf<CachedCompactTestCase>()
        val testCasesLock = Object()

        fun putIntoCache(testCase: CachedCompactTestCase) {
            synchronized(testCasesLock) {
                testCases.add(testCase)
            }
        }
    }

    private class CachedCompactTestCase(testName: String, testCode: String, coveredLines: Set<LineTestCaseCache>) {

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
