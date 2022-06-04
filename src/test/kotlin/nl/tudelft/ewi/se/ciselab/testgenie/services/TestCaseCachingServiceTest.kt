package nl.tudelft.ewi.se.ciselab.testgenie.services

import org.assertj.core.api.Assertions.assertThat
import org.evosuite.result.TestGenerationResultImpl
import org.evosuite.utils.CompactReport
import org.evosuite.utils.CompactTestCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestCaseCachingServiceTest {

    private lateinit var testCaseCachingService: TestCaseCachingService

    @BeforeEach
    fun setUp() {
        testCaseCachingService = TestCaseCachingService()
    }

    @Test
    fun singleFileSingleLine() {
        val report = CompactReport(TestGenerationResultImpl())
        val test1 = CompactTestCase("a", "aa", setOf(1, 2), setOf(), setOf())
        val test2 = CompactTestCase("b", "bb", setOf(2, 3), setOf(), setOf())
        report.testCaseList = hashMapOf(
            createPair(test1),
            createPair(test2)
        )

        val file = "file"

        testCaseCachingService.putIntoCache(file, report)

        val result = testCaseCachingService.retrieveFromCache(file, 2, 2)

        assertThat(result)
            .extracting<Triple<String, String, Set<Int>>> {
                createTriple(it)
            }
            .containsExactlyInAnyOrder(
                createTriple(test1),
                createTriple(test2)
            )
    }

    @Test
    fun addingNewTestWithSameCodeInvalidatesPreviousOne() {
        val report = CompactReport(TestGenerationResultImpl())
        val test1 = CompactTestCase("a", "aa", setOf(1, 2), setOf(), setOf())
        val test2 = CompactTestCase("b", "bb", setOf(2, 3), setOf(), setOf())
        report.testCaseList = hashMapOf(
            createPair(test1),
            createPair(test2)
        )
        val report1a = CompactReport(TestGenerationResultImpl())
        val test1a = CompactTestCase("a2", "aa", setOf(1, 2, 3), setOf(), setOf())
        report1a.testCaseList = hashMapOf(
            createPair(test1a)
        )

        val file = "file"

        testCaseCachingService.putIntoCache(file, report)
        testCaseCachingService.putIntoCache(file, report1a)

        val result = testCaseCachingService.retrieveFromCache(file, 2, 2)

        assertThat(result)
            .extracting<Triple<String, String, Set<Int>>> {
                createTriple(it)
            }
            .containsExactlyInAnyOrder(
                createTriple(test1a),
                createTriple(test2)
            )
    }

    @Test
    fun invalidateSingleFileSingleLine() {
        val report = CompactReport(TestGenerationResultImpl())
        val test1 = CompactTestCase("a", "aa", setOf(1, 2), setOf(), setOf())
        val test2 = CompactTestCase("b", "bb", setOf(2, 3), setOf(), setOf())
        report.testCaseList = hashMapOf(
            createPair(test1),
            createPair(test2)
        )

        val file = "file"

        testCaseCachingService.putIntoCache(file, report)

        testCaseCachingService.invalidateFromCache(file, 1, 1)

        val result = testCaseCachingService.retrieveFromCache(file, 2, 2)

        assertThat(result)
            .extracting<Triple<String, String, Set<Int>>> {
                createTriple(it)
            }
            .containsExactlyInAnyOrder(
                createTriple(test2)
            )
    }

    @Test
    fun invalidateSingleTest() {
        val report = CompactReport(TestGenerationResultImpl())
        val test1 = CompactTestCase("a", "aa", setOf(1, 2), setOf(), setOf())
        val test2 = CompactTestCase("b", "bb", setOf(2, 3), setOf(), setOf())
        report.testCaseList = hashMapOf(
            createPair(test1),
            createPair(test2)
        )

        val file = "file"

        testCaseCachingService.putIntoCache(file, report)

        testCaseCachingService.invalidateFromCache(file, test2.testCode)

        val result = testCaseCachingService.retrieveFromCache(file, 2, 2)

        assertThat(result)
            .extracting<Triple<String, String, Set<Int>>> {
                createTriple(it)
            }
            .containsExactlyInAnyOrder(
                createTriple(test1)
            )
    }

    @Test
    fun invalidateNonexistentSingleTest() {
        val report = CompactReport(TestGenerationResultImpl())
        val test1 = CompactTestCase("a", "aa", setOf(1, 2), setOf(), setOf())
        val test2 = CompactTestCase("b", "bb", setOf(2, 3), setOf(), setOf())
        report.testCaseList = hashMapOf(
            createPair(test1),
            createPair(test2)
        )

        val file = "file"

        testCaseCachingService.putIntoCache(file, report)
        testCaseCachingService.invalidateFromCache(file, "invaid")

        val result = testCaseCachingService.retrieveFromCache(file, 2, 2)

        assertThat(result)
            .extracting<Triple<String, String, Set<Int>>> {
                createTriple(it)
            }
            .containsExactlyInAnyOrder(
                createTriple(test1),
                createTriple(test2)
            )
    }

    @Test
    fun singleFileMultipleLines() {
        val report = CompactReport(TestGenerationResultImpl())
        val test1 = CompactTestCase("a", "aa", setOf(1, 2), setOf(), setOf())
        val test2 = CompactTestCase("b", "bb", setOf(2, 3), setOf(), setOf())
        val test3 = CompactTestCase("c", "cc", setOf(1, 4), setOf(), setOf())
        val test4 = CompactTestCase("d", "dd", setOf(8), setOf(), setOf())
        val test5 = CompactTestCase("e", "ee", setOf(11), setOf(), setOf())
        report.testCaseList = hashMapOf(
            createPair(test1),
            createPair(test2),
            createPair(test3),
            createPair(test4),
            createPair(test5)
        )

        val file = "file"

        testCaseCachingService.putIntoCache(file, report)

        val result = testCaseCachingService.retrieveFromCache(file, 4, 10)

        assertThat(result)
            .extracting<Triple<String, String, Set<Int>>> {
                createTriple(it)
            }
            .containsExactlyInAnyOrder(
                createTriple(test3),
                createTriple(test4)
            )
    }

    @Test
    fun invalidateSingleFileMultipleLines() {
        val report = CompactReport(TestGenerationResultImpl())
        val test1 = CompactTestCase("a", "aa", setOf(1, 2), setOf(), setOf())
        val test2 = CompactTestCase("b", "bb", setOf(2, 3), setOf(), setOf())
        val test3 = CompactTestCase("c", "cc", setOf(1, 4), setOf(), setOf())
        val test4 = CompactTestCase("d", "dd", setOf(8), setOf(), setOf())
        val test5 = CompactTestCase("e", "ee", setOf(11), setOf(), setOf())
        report.testCaseList = hashMapOf(
            createPair(test1),
            createPair(test2),
            createPair(test3),
            createPair(test4),
            createPair(test5)
        )

        val file = "file"

        testCaseCachingService.putIntoCache(file, report)

        testCaseCachingService.invalidateFromCache(file, 3, 9)

        val result = testCaseCachingService.retrieveFromCache(file, 1, 11)

        assertThat(result)
            .extracting<Triple<String, String, Set<Int>>> {
                createTriple(it)
            }
            .containsExactlyInAnyOrder(
                createTriple(test1),
                createTriple(test5)
            )
    }

    @Test
    fun multipleFiles() {
        val report = CompactReport(TestGenerationResultImpl())
        val test1 = CompactTestCase("a", "aa", setOf(1, 2), setOf(), setOf())
        val test2 = CompactTestCase("b", "bb", setOf(2, 3), setOf(), setOf())
        val test3 = CompactTestCase("c", "cc", setOf(1, 4), setOf(), setOf())
        val test4 = CompactTestCase("d", "dd", setOf(8), setOf(), setOf())
        val test5 = CompactTestCase("e", "ee", setOf(11), setOf(), setOf())
        report.testCaseList = hashMapOf(
            createPair(test1),
            createPair(test2),
            createPair(test3),
            createPair(test4),
            createPair(test5)
        )

        val report2 = CompactReport(TestGenerationResultImpl())
        report2.testCaseList = hashMapOf(
            createPair(CompactTestCase("0a", "aa", setOf(1, 2), setOf(), setOf())),
            createPair(CompactTestCase("0b", "bb", setOf(2, 3), setOf(), setOf())),
            createPair(CompactTestCase("0c", "cc", setOf(1, 4), setOf(), setOf())),
            createPair(CompactTestCase("0d", "dd", setOf(8), setOf(), setOf())),
            createPair(CompactTestCase("0e", "ee", setOf(11), setOf(), setOf()))
        )

        val file = "file"

        testCaseCachingService.putIntoCache(file, report)
        testCaseCachingService.putIntoCache("file 2", report2)

        val result = testCaseCachingService.retrieveFromCache(file, 4, 10)

        assertThat(result)
            .extracting<Triple<String, String, Set<Int>>> {
                createTriple(it)
            }
            .containsExactlyInAnyOrder(
                createTriple(test3),
                createTriple(test4)
            )
    }

    @Test
    fun multipleFilesMultipleInsertions() {
        val report = CompactReport(TestGenerationResultImpl())
        val test1 = CompactTestCase("a", "aa", setOf(1, 2), setOf(), setOf())
        val test2 = CompactTestCase("b", "bb", setOf(2, 3), setOf(), setOf())
        val test3 = CompactTestCase("c", "cc", setOf(1, 4), setOf(), setOf())
        val test5 = CompactTestCase("e", "ee", setOf(11), setOf(), setOf())
        report.testCaseList = hashMapOf(
            createPair(test1),
            createPair(test2),
            createPair(test3),
            createPair(test5)
        )

        val report2 = CompactReport(TestGenerationResultImpl())
        report2.testCaseList = hashMapOf(
            createPair(CompactTestCase("0a", "aa", setOf(1, 2), setOf(), setOf())),
            createPair(CompactTestCase("0b", "bb", setOf(2, 3), setOf(), setOf())),
            createPair(CompactTestCase("0c", "cc", setOf(1, 4), setOf(), setOf())),
            createPair(CompactTestCase("0d", "dd", setOf(8), setOf(), setOf())),
            createPair(CompactTestCase("0e", "ee", setOf(11), setOf(), setOf()))
        )

        val report3 = CompactReport(TestGenerationResultImpl())
        val test4 = CompactTestCase("d", "dd", setOf(8), setOf(), setOf())
        report3.testCaseList = hashMapOf(
            createPair(test4)
        )

        val file = "file"

        testCaseCachingService.putIntoCache(file, report)
        testCaseCachingService.putIntoCache("file 2", report2)
        testCaseCachingService.putIntoCache(file, report3)

        val result = testCaseCachingService.retrieveFromCache(file, 4, 10)

        assertThat(result)
            .extracting<Triple<String, String, Set<Int>>> {
                createTriple(it)
            }
            .containsExactlyInAnyOrder(
                createTriple(test3),
                createTriple(test4)
            )
    }

    @Test
    fun testCoversMultipleLinesInRange() {
        val report = CompactReport(TestGenerationResultImpl())
        val test1 = CompactTestCase("a", "aa", setOf(4, 5), setOf(), setOf())
        report.testCaseList = hashMapOf(
            createPair(test1)
        )

        val file = "file"

        testCaseCachingService.putIntoCache(file, report)

        val result = testCaseCachingService.retrieveFromCache(file, 1, 10)

        assertThat(result)
            .extracting<Triple<String, String, Set<Int>>> {
                createTriple(it)
            }
            .containsExactlyInAnyOrder(
                createTriple(test1)
            )
    }

    @Test
    fun nonexistentFile() {
        val report = CompactReport(TestGenerationResultImpl())
        val test1 = CompactTestCase("a", "aa", setOf(1, 2), setOf(), setOf())
        val test2 = CompactTestCase("b", "bb", setOf(2, 3), setOf(), setOf())
        report.testCaseList = hashMapOf(
            createPair(test1),
            createPair(test2)
        )

        testCaseCachingService.putIntoCache("aa", report)

        val result = testCaseCachingService.retrieveFromCache("bb", 2, 2)

        assertThat(result)
            .isEmpty()
    }

    @Test
    fun noMatchingTests() {
        val report = CompactReport(TestGenerationResultImpl())
        val test1 = CompactTestCase("a", "aa", setOf(1, 2), setOf(), setOf())
        val test2 = CompactTestCase("b", "bb", setOf(2, 3), setOf(), setOf())
        report.testCaseList = hashMapOf(
            createPair(test1),
            createPair(test2)
        )

        val file = "file"

        testCaseCachingService.putIntoCache(file, report)

        val result = testCaseCachingService.retrieveFromCache(file, 4, 50)

        assertThat(result)
            .isEmpty()
    }

    @Test
    fun invalidateNoMatchingTests() {
        val report = CompactReport(TestGenerationResultImpl())
        val test1 = CompactTestCase("a", "aa", setOf(1, 2), setOf(), setOf())
        val test2 = CompactTestCase("b", "bb", setOf(2, 3), setOf(), setOf())
        report.testCaseList = hashMapOf(
            createPair(test1),
            createPair(test2)
        )

        val file = "file"

        testCaseCachingService.putIntoCache(file, report)
        testCaseCachingService.invalidateFromCache(file, 4, 50)
        val result = testCaseCachingService.retrieveFromCache(file, 1, 50)

        assertThat(result)
            .extracting<Triple<String, String, Set<Int>>> {
                createTriple(it)
            }
            .containsExactlyInAnyOrder(
                createTriple(test1),
                createTriple(test2)
            )
    }

    @Test
    fun invalidInputLines() {
        val report = CompactReport(TestGenerationResultImpl())
        val test1 = CompactTestCase("a", "aa", setOf(1, 2), setOf(), setOf())
        val test2 = CompactTestCase("b", "bb", setOf(2, 3), setOf(), setOf())
        report.testCaseList = hashMapOf(
            createPair(test1),
            createPair(test2)
        )

        val file = "file"

        testCaseCachingService.putIntoCache(file, report)

        val result = testCaseCachingService.retrieveFromCache(file, 4, 1)

        assertThat(result)
            .isEmpty()
    }

    @Test
    fun invalidateInvalidInputLines() {
        val report = CompactReport(TestGenerationResultImpl())
        val test1 = CompactTestCase("a", "aa", setOf(1, 2), setOf(), setOf())
        val test2 = CompactTestCase("b", "bb", setOf(2, 3), setOf(), setOf())
        report.testCaseList = hashMapOf(
            createPair(test1),
            createPair(test2)
        )

        val file = "file"

        testCaseCachingService.putIntoCache(file, report)
        testCaseCachingService.invalidateFromCache(file, 4, 1)

        val result = testCaseCachingService.retrieveFromCache(file, 1, 50)

        assertThat(result)
            .extracting<Triple<String, String, Set<Int>>> {
                createTriple(it)
            }
            .containsExactlyInAnyOrder(
                createTriple(test1),
                createTriple(test2)
            )
    }

    companion object {
        fun createPair(testCase: CompactTestCase): Pair<String, CompactTestCase> {
            return Pair(testCase.testName, testCase)
        }

        fun createTriple(testCase: CompactTestCase): Triple<String, String, Set<Int>> {
            return Triple(testCase.testName.split(' ')[0], testCase.testCode, testCase.coveredLines)
        }
    }
}
