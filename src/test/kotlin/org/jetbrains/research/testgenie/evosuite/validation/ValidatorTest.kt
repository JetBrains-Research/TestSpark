package org.jetbrains.research.testgenie.evosuite.validation

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ValidatorTest {
    @Test
    fun testMatchFail() {
        val result = "Time: 0.443\n" +
            "There were 3 failures:\n" +
            "1) testNotStuff(demo.TestClass_ESTest)\n" +
            "java.lang.AssertionError\n" +
            "2) testComplexTerribleMethodReturningPositive(demo.TestClass_ESTest)\n" +
            "java.lang.AssertionError: expected:<2> but was:<1>\n" +
            "3) testDoStuff(demo.TestClass_ESTest)\n" +
            "java.lang.AssertionError\n" +
            "\n" +
            "FAILURES!!!\n" +
            "Tests run: 7,  Failures: 3\n" +
            "\n"

        val res = Validator.parseJunitResult(result)

        assertEquals(7, res.totalTests)
        assertEquals(3, res.failedTests)
    }

    @Test
    fun testMatchOK() {
        val result = ".......\n" +
            "Time: 0.505\n" +
            "\n" +
            "OK (7 tests)"

        val res = Validator.parseJunitResult(result)

        assertEquals(7, res.totalTests)
        assertEquals(0, res.failedTests)
        assertTrue(res.failedTestNames.isEmpty())
    }
}
