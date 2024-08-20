package org.jetbrains.research.testspark.display

import org.jetbrains.research.testspark.display.utils.ErrorMessageManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ErrorMessageManagerTest {

    /**
     * Test with a string length less than BLOCK_SIZE and no separators.
     */
    @Test
    fun testNormalizeWithNoSeparator() {
        val input = "a".repeat(ErrorMessageManager.BLOCK_SIZE - 1)
        val expected = "a".repeat(ErrorMessageManager.BLOCK_SIZE - 1) + ErrorMessageManager.SEPARATOR
        val result = ErrorMessageManager.normalize(input)
        assertEquals(expected, result)
    }

    /**
     * Test with a string length exactly equal to BLOCK_SIZE and no separators.
     */
    @Test
    fun testNormalizeWithExactBlockSizeNoSeparator() {
        val input = "a".repeat(ErrorMessageManager.BLOCK_SIZE)
        val expected = "a".repeat(ErrorMessageManager.BLOCK_SIZE)
        val result = ErrorMessageManager.normalize(input)
        assertEquals(expected, result)
    }

    /**
     * Test with a string length greater than BLOCK_SIZE and no separators.
     */
    @Test
    fun testNormalizeWithMultipleBlocksNoSeparator() {
        val input = "a".repeat(ErrorMessageManager.BLOCK_SIZE * 2 + 50)
        val expected = "a".repeat(ErrorMessageManager.BLOCK_SIZE) + ErrorMessageManager.SEPARATOR +
            "a".repeat(ErrorMessageManager.BLOCK_SIZE) + ErrorMessageManager.SEPARATOR +
            "a".repeat(50) + ErrorMessageManager.SEPARATOR
        val result = ErrorMessageManager.normalize(input)
        assertEquals(expected, result)
    }

    /**
     * Test with an empty string.
     */
    @Test
    fun testNormalizeEmptyString() {
        val input = ""
        val expected = ""
        val result = ErrorMessageManager.normalize(input)
        assertEquals(expected, result)
    }

    /**
     * Test with a string containing multiple separators.
     */
    @Test
    fun testNormalizeWithMultipleSeparators() {
        val input = "a".repeat(40) + ErrorMessageManager.SEPARATOR +
            "b".repeat(40) + ErrorMessageManager.SEPARATOR +
            "c".repeat(20)
        val expected = "a".repeat(40) + ErrorMessageManager.SEPARATOR +
            "b".repeat(40) + ErrorMessageManager.SEPARATOR +
            "c".repeat(20) + ErrorMessageManager.SEPARATOR
        val result = ErrorMessageManager.normalize(input)
        assertEquals(expected, result)
    }
}
