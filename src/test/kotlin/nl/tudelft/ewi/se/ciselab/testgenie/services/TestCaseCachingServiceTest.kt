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
            Pair("a", test1),
            Pair("b", test2)
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

    private fun createTriple(testCase: CompactTestCase): Triple<String, String, Set<Int>> {
        return Triple(testCase.testName, testCase.testCode, testCase.coveredLines)
    }
}
