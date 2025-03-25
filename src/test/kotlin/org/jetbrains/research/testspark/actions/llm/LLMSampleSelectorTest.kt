package org.jetbrains.research.testspark.actions.llm

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach

class LLMSampleSelectorTest {
    private lateinit var sampleSelector: LLMSampleSelector

    @BeforeEach
    fun setUp() {
        sampleSelector = LLMSampleSelector()
    }

    @Test
    fun testSamplesCodeInitiallyEmpty() {
        assertEquals("", sampleSelector.getTestSamplesCode())
    }

    @Test
    fun testTestNamesInitiallyEmpty() {
        assertTrue(sampleSelector.getTestNames().isEmpty())
    }

    @Test
    fun testInitialTestCodesEmpty() {
        assertTrue(sampleSelector.getInitialTestCodes().isEmpty())
    }

    @Test
    fun testAppendTestSampleCode() {
        val testCode = "public void testExample() {}"
        sampleSelector.appendTestSampleCode(0, testCode)

        val expected = "Test sample number 1\n```\n$testCode\n```\n"
        assertEquals(expected, sampleSelector.getTestSamplesCode())
    }

    @Test
    fun testMultipleAppendTestSampleCode() {
        val testCode1 = "void test1() {}"
        val testCode2 = "void test2() {}"

        sampleSelector.appendTestSampleCode(0, testCode1)
        sampleSelector.appendTestSampleCode(1, testCode2)

        val expected = """
            Test sample number 1
            ```
            $testCode1
            ```
            Test sample number 2
            ```
            $testCode2
            ```
            """.trimIndent() + "\n"

        assertEquals(expected, sampleSelector.getTestSamplesCode())
    }
}
