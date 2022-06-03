package nl.tudelft.ewi.se.ciselab.testgenie.services

import com.intellij.util.containers.map2Array
import net.jqwik.api.Arbitraries
import net.jqwik.api.Arbitrary
import net.jqwik.api.Combinators
import net.jqwik.api.ForAll
import net.jqwik.api.Property
import net.jqwik.api.Provide
import net.jqwik.api.RandomDistribution
import net.jqwik.api.lifecycle.BeforeTry
import nl.tudelft.ewi.se.ciselab.testgenie.services.TestCaseCachingServiceTest.Companion.createPair
import nl.tudelft.ewi.se.ciselab.testgenie.services.TestCaseCachingServiceTest.Companion.createTriple
import org.assertj.core.api.Assertions.assertThat
import org.evosuite.result.TestGenerationResultImpl
import org.evosuite.utils.CompactReport
import org.evosuite.utils.CompactTestCase
import org.junit.jupiter.api.TestInstance
import java.lang.Integer.max
import java.lang.Integer.min

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestCaseCachingServicePropertyBasedTest {

    private lateinit var testCaseCachingService: TestCaseCachingService

    @BeforeTry
    fun setUp() {
        testCaseCachingService = TestCaseCachingService()
    }

    @Property
    fun singleFileMultipleLines(
        @ForAll("compactTestCaseGenerator") testCases: List<CompactTestCase>,
        @ForAll("lineRangeGenerator") lineRange: Pair<Int, Int>
    ) {
        val lowerBound = lineRange.first
        val upperBound = lineRange.second

        val report = CompactReport(TestGenerationResultImpl())
        report.testCaseList = HashMap(testCases.associate { createPair(it) })
        val file = "file"

        testCaseCachingService.putIntoCache(file, report)

        val actual = testCaseCachingService.retrieveFromCache(file, lowerBound, upperBound)
        val expected = testCases.filter { it.coveredLines.any { b -> b in lowerBound..upperBound } }

        assertThat(actual)
            .extracting<Triple<String, String, Set<Int>>> {
                createTriple(it)
            }
            .containsExactlyInAnyOrder(
                *expected.map2Array { createTriple(it) }
            )
    }

    @Provide
    private fun compactTestCaseGenerator(): Arbitrary<List<CompactTestCase>> {
        val lineNumberArbitrary = lineNumberGenerator()

        val testNameArbitrary = Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('A', 'Z')
            .withCharRange('0', '9')
            .ofMinLength(1)
            .ofMaxLength(32)

        val testCodeArbitrary = Arbitraries.strings()
            .ofMinLength(1)

        val compactTestCaseArbitrary = Combinators.combine(
            testNameArbitrary,
            testCodeArbitrary,
            lineNumberArbitrary.set()
        )
            .`as` { name, code, lineNumbers ->
                CompactTestCase(name, code, lineNumbers, setOf(), setOf())
            }
            .list()
            .uniqueElements { it.testName }

        return compactTestCaseArbitrary
    }

    @Provide
    fun lineNumberGenerator(): Arbitrary<Int> = Arbitraries.integers()
        .between(1, 10000)
        .shrinkTowards(1)
        .withDistribution(RandomDistribution.gaussian(0.1));

    @Provide
    fun lineRangeGenerator(): Arbitrary<Pair<Int, Int>> = lineNumberGenerator()
        .list()
        .ofSize(2)
        .map {
            val l1 = it[0]
            val l2 = it[1]
            Pair(min(l1, l2), max(l1, l2))
        }
}
