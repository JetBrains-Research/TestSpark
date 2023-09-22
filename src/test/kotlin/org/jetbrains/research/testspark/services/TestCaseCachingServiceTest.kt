package org.jetbrains.research.testspark.services

import org.assertj.core.api.Assertions.assertThat
import org.evosuite.result.TestGenerationResultImpl
import org.evosuite.utils.CompactReport
import org.jetbrains.research.testspark.data.Report
import org.jetbrains.research.testspark.data.TestCase
import org.jetbrains.research.testspark.editor.Workspace
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestCaseCachingServiceTest {

    private lateinit var testCaseCachingService: TestCaseCachingService

    private val testJobInfo = Workspace.TestJobInfo("", "", 0, "", "")

    @BeforeEach
    fun setUp() {
        testCaseCachingService = TestCaseCachingService()
    }

    @Test
    fun singleFileSingleLine() {
        val report = Report(CompactReport(TestGenerationResultImpl()))
        val test1 = TestCase(0, "a", "aa", setOf(1, 2), setOf(), setOf())
        val test2 = TestCase(1, "b", "bb", setOf(2, 3), setOf(), setOf())
        report.testCaseList = hashMapOf(
            createPair(test1),
            createPair(test2),
        )

        val file = "file"

        testCaseCachingService.putIntoCache(file, report, testJobInfo)

        val result = testCaseCachingService.retrieveFromCache(file, 2, 2)

        assertThat(result)
            .extracting<Triple<String, String, Set<Int>>> {
                createTriple(it)
            }
            .containsExactlyInAnyOrder(
                createTriple(test1),
                createTriple(test2),
            )
    }

    @Test
    fun addingNewTestWithSameCodeInvalidatesPreviousOne() {
        val report = Report(CompactReport(TestGenerationResultImpl()))
        val test1 = TestCase(0, "a", "aa", setOf(1, 2), setOf(), setOf())
        val test2 = TestCase(1, "b", "bb", setOf(2, 3), setOf(), setOf())
        report.testCaseList = hashMapOf(
            createPair(test1),
            createPair(test2),
        )
        val report1a = Report(CompactReport(TestGenerationResultImpl()))
        val test1a = TestCase(0, "a2", "aa", setOf(1, 2, 3), setOf(), setOf())
        report1a.testCaseList = hashMapOf(
            createPair(test1a),
        )

        val file = "file"

        testCaseCachingService.putIntoCache(file, report, testJobInfo)
        testCaseCachingService.putIntoCache(file, report1a, testJobInfo)

        val result = testCaseCachingService.retrieveFromCache(file, 2, 2)

        assertThat(result)
            .extracting<Triple<String, String, Set<Int>>> {
                createTriple(it)
            }
            .containsExactlyInAnyOrder(
                createTriple(test1a),
                createTriple(test2),
            )
    }

    @Test
    fun invalidateSingleFileSingleLine() {
        val report = Report(CompactReport(TestGenerationResultImpl()))
        val test1 = TestCase(0, "a", "aa", setOf(1, 2), setOf(), setOf())
        val test2 = TestCase(1, "b", "bb", setOf(2, 3), setOf(), setOf())
        report.testCaseList = hashMapOf(
            createPair(test1),
            createPair(test2),
        )

        val file = "file"

        testCaseCachingService.putIntoCache(file, report, testJobInfo)

        testCaseCachingService.invalidateFromCache(file, 1, 1)

        val result = testCaseCachingService.retrieveFromCache(file, 2, 2)

        assertThat(result)
            .extracting<Triple<String, String, Set<Int>>> {
                createTriple(it)
            }
            .containsExactlyInAnyOrder(
                createTriple(test2),
            )
    }

    @Test
    fun invalidateSingleTest() {
        val report = Report(CompactReport(TestGenerationResultImpl()))
        val test1 = TestCase(0, "a", "aa", setOf(1, 2), setOf(), setOf())
        val test2 = TestCase(1, "b", "bb", setOf(2, 3), setOf(), setOf())
        report.testCaseList = hashMapOf(
            createPair(test1),
            createPair(test2),
        )

        val file = "file"

        testCaseCachingService.putIntoCache(file, report, testJobInfo)

        testCaseCachingService.invalidateFromCache(file, test2.testCode)

        val result = testCaseCachingService.retrieveFromCache(file, 2, 2)

        assertThat(result)
            .extracting<Triple<String, String, Set<Int>>> {
                createTriple(it)
            }
            .containsExactlyInAnyOrder(
                createTriple(test1),
            )
    }

    @Test
    fun invalidateNonexistentSingleTest() {
        val report = Report(CompactReport(TestGenerationResultImpl()))
        val test1 = TestCase(0, "a", "aa", setOf(1, 2), setOf(), setOf())
        val test2 = TestCase(1, "b", "bb", setOf(2, 3), setOf(), setOf())
        report.testCaseList = hashMapOf(
            createPair(test1),
            createPair(test2),
        )

        val file = "file"

        testCaseCachingService.putIntoCache(file, report, testJobInfo)
        testCaseCachingService.invalidateFromCache(file, "invaid")

        val result = testCaseCachingService.retrieveFromCache(file, 2, 2)

        assertThat(result)
            .extracting<Triple<String, String, Set<Int>>> {
                createTriple(it)
            }
            .containsExactlyInAnyOrder(
                createTriple(test1),
                createTriple(test2),
            )
    }

    @Test
    fun singleFileMultipleLines() {
        val report = Report(CompactReport(TestGenerationResultImpl()))
        val test1 = TestCase(0, "a", "aa", setOf(1, 2), setOf(), setOf())
        val test2 = TestCase(1, "b", "bb", setOf(2, 3), setOf(), setOf())
        val test3 = TestCase(2, "c", "cc", setOf(1, 4), setOf(), setOf())
        val test4 = TestCase(3, "d", "dd", setOf(8), setOf(), setOf())
        val test5 = TestCase(4, "e", "ee", setOf(11), setOf(), setOf())
        report.testCaseList = hashMapOf(
            createPair(test1),
            createPair(test2),
            createPair(test3),
            createPair(test4),
            createPair(test5),
        )

        val file = "file"

        testCaseCachingService.putIntoCache(file, report, testJobInfo)

        val result = testCaseCachingService.retrieveFromCache(file, 4, 10)

        assertThat(result)
            .extracting<Triple<String, String, Set<Int>>> {
                createTriple(it)
            }
            .containsExactlyInAnyOrder(
                createTriple(test3),
                createTriple(test4),
            )
    }

    @Test
    fun invalidateSingleFileMultipleLines() {
        val report = Report(CompactReport(TestGenerationResultImpl()))
        val test1 = TestCase(0, "a", "aa", setOf(1, 2), setOf(), setOf())
        val test2 = TestCase(1, "b", "bb", setOf(2, 3), setOf(), setOf())
        val test3 = TestCase(2, "c", "cc", setOf(1, 4), setOf(), setOf())
        val test4 = TestCase(3, "d", "dd", setOf(8), setOf(), setOf())
        val test5 = TestCase(4, "e", "ee", setOf(11), setOf(), setOf())
        report.testCaseList = hashMapOf(
            createPair(test1),
            createPair(test2),
            createPair(test3),
            createPair(test4),
            createPair(test5),
        )

        val file = "file"

        testCaseCachingService.putIntoCache(file, report, testJobInfo)

        testCaseCachingService.invalidateFromCache(file, 3, 9)

        val result = testCaseCachingService.retrieveFromCache(file, 1, 11)

        assertThat(result)
            .extracting<Triple<String, String, Set<Int>>> {
                createTriple(it)
            }
            .containsExactlyInAnyOrder(
                createTriple(test1),
                createTriple(test5),
            )
    }

    @Test
    fun multipleFiles() {
        val report = Report(CompactReport(TestGenerationResultImpl()))
        val test1 = TestCase(0, "a", "aa", setOf(1, 2), setOf(), setOf())
        val test2 = TestCase(1, "b", "bb", setOf(2, 3), setOf(), setOf())
        val test3 = TestCase(2, "c", "cc", setOf(1, 4), setOf(), setOf())
        val test4 = TestCase(3, "d", "dd", setOf(8), setOf(), setOf())
        val test5 = TestCase(4, "e", "ee", setOf(11), setOf(), setOf())
        report.testCaseList = hashMapOf(
            createPair(test1),
            createPair(test2),
            createPair(test3),
            createPair(test4),
            createPair(test5),
        )

        val report2 = Report(CompactReport(TestGenerationResultImpl()))
        report2.testCaseList = hashMapOf(
            createPair(TestCase(0, "0a", "aa", setOf(1, 2), setOf(), setOf())),
            createPair(TestCase(1, "0b", "bb", setOf(2, 3), setOf(), setOf())),
            createPair(TestCase(2, "0c", "cc", setOf(1, 4), setOf(), setOf())),
            createPair(TestCase(3, "0d", "dd", setOf(8), setOf(), setOf())),
            createPair(TestCase(4, "0e", "ee", setOf(11), setOf(), setOf())),
        )

        val file = "file"

        testCaseCachingService.putIntoCache(file, report, testJobInfo)
        testCaseCachingService.putIntoCache("file 2", report2, testJobInfo)

        val result = testCaseCachingService.retrieveFromCache(file, 4, 10)

        assertThat(result)
            .extracting<Triple<String, String, Set<Int>>> {
                createTriple(it)
            }
            .containsExactlyInAnyOrder(
                createTriple(test3),
                createTriple(test4),
            )
    }

    @Test
    fun multipleFilesMultipleInsertions() {
        val report = Report(CompactReport(TestGenerationResultImpl()))
        val test1 = TestCase(0, "a", "aa", setOf(1, 2), setOf(), setOf())
        val test2 = TestCase(1, "b", "bb", setOf(2, 3), setOf(), setOf())
        val test3 = TestCase(2, "c", "cc", setOf(1, 4), setOf(), setOf())
        val test5 = TestCase(3, "e", "ee", setOf(11), setOf(), setOf())
        report.testCaseList = hashMapOf(
            createPair(test1),
            createPair(test2),
            createPair(test3),
            createPair(test5),
        )

        val report2 = Report(CompactReport(TestGenerationResultImpl()))
        report2.testCaseList = hashMapOf(
            createPair(TestCase(0, "0a", "aa", setOf(1, 2), setOf(), setOf())),
            createPair(TestCase(1, "0b", "bb", setOf(2, 3), setOf(), setOf())),
            createPair(TestCase(2, "0c", "cc", setOf(1, 4), setOf(), setOf())),
            createPair(TestCase(3, "0d", "dd", setOf(8), setOf(), setOf())),
            createPair(TestCase(4, "0e", "ee", setOf(11), setOf(), setOf())),
        )

        val report3 = Report(CompactReport(TestGenerationResultImpl()))
        val test4 = TestCase(0, "d", "dd", setOf(8), setOf(), setOf())
        report3.testCaseList = hashMapOf(
            createPair(test4),
        )

        val file = "file"

        testCaseCachingService.putIntoCache(file, report, testJobInfo)
        testCaseCachingService.putIntoCache("file 2", report2, testJobInfo)
        testCaseCachingService.putIntoCache(file, report3, testJobInfo)

        val result = testCaseCachingService.retrieveFromCache(file, 4, 10)

        assertThat(result)
            .extracting<Triple<String, String, Set<Int>>> {
                createTriple(it)
            }
            .containsExactlyInAnyOrder(
                createTriple(test3),
                createTriple(test4),
            )
    }

    @Test
    fun testCoversMultipleLinesInRange() {
        val report = Report(CompactReport(TestGenerationResultImpl()))
        val test1 = TestCase(0,"a", "aa", setOf(4, 5), setOf(), setOf())
        report.testCaseList = hashMapOf(
            createPair(test1),
        )

        val file = "file"

        testCaseCachingService.putIntoCache(file, report, testJobInfo)

        val result = testCaseCachingService.retrieveFromCache(file, 1, 10)

        assertThat(result)
            .extracting<Triple<String, String, Set<Int>>> {
                createTriple(it)
            }
            .containsExactlyInAnyOrder(
                createTriple(test1),
            )
    }

    @Test
    fun nonexistentFile() {
        val report = Report(CompactReport(TestGenerationResultImpl()))
        val test1 = TestCase(0, "a", "aa", setOf(1, 2), setOf(), setOf())
        val test2 = TestCase(1, "b", "bb", setOf(2, 3), setOf(), setOf())
        report.testCaseList = hashMapOf(
            createPair(test1),
            createPair(test2),
        )

        testCaseCachingService.putIntoCache("aa", report, testJobInfo)

        val result = testCaseCachingService.retrieveFromCache("bb", 2, 2)

        assertThat(result)
            .isEmpty()
    }

    @Test
    fun noMatchingTests() {
        val report = Report(CompactReport(TestGenerationResultImpl()))
        val test1 = TestCase(0, "a", "aa", setOf(1, 2), setOf(), setOf())
        val test2 = TestCase(1, "b", "bb", setOf(2, 3), setOf(), setOf())
        report.testCaseList = hashMapOf(
            createPair(test1),
            createPair(test2),
        )

        val file = "file"

        testCaseCachingService.putIntoCache(file, report, testJobInfo)

        val result = testCaseCachingService.retrieveFromCache(file, 4, 50)

        assertThat(result)
            .isEmpty()
    }

    @Test
    fun invalidateNoMatchingTests() {
        val report = Report(CompactReport(TestGenerationResultImpl()))
        val test1 = TestCase(0, "a", "aa", setOf(1, 2), setOf(), setOf())
        val test2 = TestCase(1, "b", "bb", setOf(2, 3), setOf(), setOf())
        report.testCaseList = hashMapOf(
            createPair(test1),
            createPair(test2),
        )

        val file = "file"

        testCaseCachingService.putIntoCache(file, report, testJobInfo)
        testCaseCachingService.invalidateFromCache(file, 4, 50)
        val result = testCaseCachingService.retrieveFromCache(file, 1, 50)

        assertThat(result)
            .extracting<Triple<String, String, Set<Int>>> {
                createTriple(it)
            }
            .containsExactlyInAnyOrder(
                createTriple(test1),
                createTriple(test2),
            )
    }

    @Test
    fun invalidInputLines() {
        val report = Report(CompactReport(TestGenerationResultImpl()))
        val test1 = TestCase(0, "a", "aa", setOf(1, 2), setOf(), setOf())
        val test2 = TestCase(1, "b", "bb", setOf(2, 3), setOf(), setOf())
        report.testCaseList = hashMapOf(
            createPair(test1),
            createPair(test2),
        )

        val file = "file"

        testCaseCachingService.putIntoCache(file, report, testJobInfo)

        val result = testCaseCachingService.retrieveFromCache(file, 4, 1)

        assertThat(result)
            .isEmpty()
    }

    @Test
    fun invalidateInvalidInputLines() {
        val report = Report(CompactReport(TestGenerationResultImpl()))
        val test1 = TestCase(0, "a", "aa", setOf(1, 2), setOf(), setOf())
        val test2 = TestCase(1, "b", "bb", setOf(2, 3), setOf(), setOf())
        report.testCaseList = hashMapOf(
            createPair(test1),
            createPair(test2),
        )

        val file = "file"

        testCaseCachingService.putIntoCache(file, report, testJobInfo)
        testCaseCachingService.invalidateFromCache(file, 4, 1)

        val result = testCaseCachingService.retrieveFromCache(file, 1, 50)

        assertThat(result)
            .extracting<Triple<String, String, Set<Int>>> {
                createTriple(it)
            }
            .containsExactlyInAnyOrder(
                createTriple(test1),
                createTriple(test2),
            )
    }

    companion object {
        fun createPair(testCase: TestCase): Pair<Int, TestCase> {
            return Pair(testCase.id, testCase)
        }

        fun createTriple(testCase: TestCase): Triple<String, String, Set<Int>> {
            return Triple(testCase.testName.split(' ')[0], testCase.testCode, testCase.coveredLines)
        }
    }
}
