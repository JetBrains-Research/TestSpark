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
        report.testCaseList = mapOf(
            createPair(test1),
            createPair(test2)
        ) as HashMap<String, CompactTestCase>

        val file = "file"

        testCaseCachingService.putIntoCache(file, report)

        val result = testCaseCachingService.retrieveFromCache(file, 2, 2)

        assertThat(result)
            .extracting<Triple<String, String, Set<Int>>> {
                Triple(it.testName.split(' ')[0], it.testCode, it.coveredLines)
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
        report.testCaseList = mapOf(
            createPair(test1),
            createPair(test2),
            createPair(test3),
            createPair(test4),
            createPair(test5)
        ) as HashMap<String, CompactTestCase>

        val file = "file"

        testCaseCachingService.putIntoCache(file, report)

        val result = testCaseCachingService.retrieveFromCache(file, 4, 10)

        assertThat(result)
            .extracting<Triple<String, String, Set<Int>>> {
                Triple(it.testName.split(' ')[0], it.testCode, it.coveredLines)
            }
            .containsExactlyInAnyOrder(
                createTriple(test3),
                createTriple(test4)
            )
    }

    private fun createPair(testCase: CompactTestCase): Pair<String, CompactTestCase> {
        return Pair(testCase.testName, testCase)
    }

    private fun createTriple(testCase: CompactTestCase): Triple<String, String, Set<Int>> {
        return Triple(testCase.testName, testCase.testCode, testCase.coveredLines)
    }
}
