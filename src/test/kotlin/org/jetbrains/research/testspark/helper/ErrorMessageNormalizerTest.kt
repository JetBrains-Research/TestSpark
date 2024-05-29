package org.jetbrains.research.testspark.display

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ErrorMessageNormalizerTest {

    /**
     * Test with a string length less than BLOCK_SIZE and no separators.
     */
    @Test
    fun testNormalizeWithNoSeparator() {
        val input = "a".repeat(ErrorMessageNormalizer.BLOCK_SIZE - 1)
        val expected = "a".repeat(ErrorMessageNormalizer.BLOCK_SIZE - 1) + ErrorMessageNormalizer.SEPARATOR
        val result = ErrorMessageNormalizer.normalize(input)
        assertEquals(expected, result)
    }

    /**
     * Test with a string length exactly equal to BLOCK_SIZE and no separators.
     */
    @Test
    fun testNormalizeWithExactBlockSizeNoSeparator() {
        val input = "a".repeat(ErrorMessageNormalizer.BLOCK_SIZE)
        val expected = "a".repeat(ErrorMessageNormalizer.BLOCK_SIZE)
        val result = ErrorMessageNormalizer.normalize(input)
        assertEquals(expected, result)
    }

    /**
     * Test with a string length greater than BLOCK_SIZE and no separators.
     */
    @Test
    fun testNormalizeWithMultipleBlocksNoSeparator() {
        val input = "a".repeat(ErrorMessageNormalizer.BLOCK_SIZE * 2 + 50)
        val expected = "a".repeat(ErrorMessageNormalizer.BLOCK_SIZE) + ErrorMessageNormalizer.SEPARATOR +
            "a".repeat(ErrorMessageNormalizer.BLOCK_SIZE) + ErrorMessageNormalizer.SEPARATOR +
            "a".repeat(50) + ErrorMessageNormalizer.SEPARATOR
        val result = ErrorMessageNormalizer.normalize(input)
        assertEquals(expected, result)
    }

    /**
     * Test with an empty string.
     */
    @Test
    fun testNormalizeEmptyString() {
        val input = ""
        val expected = ""
        val result = ErrorMessageNormalizer.normalize(input)
        assertEquals(expected, result)
    }

    /**
     * Test with a string containing multiple separators.
     */
    @Test
    fun testNormalizeWithMultipleSeparators() {
        val input = "a".repeat(40) + ErrorMessageNormalizer.SEPARATOR +
            "b".repeat(40) + ErrorMessageNormalizer.SEPARATOR +
            "c".repeat(20)
        val expected = "a".repeat(40) + ErrorMessageNormalizer.SEPARATOR +
            "b".repeat(40) + ErrorMessageNormalizer.SEPARATOR +
            "c".repeat(20) + ErrorMessageNormalizer.SEPARATOR
        val result = ErrorMessageNormalizer.normalize(input)
        assertEquals(expected, result)
    }
}
