package org.jetbrains.research.testspark.core.parsing.test

/**
 * Represents a line in a test case or test suite.
 *
 * @property type The type of the test line.
 * @property text The text of the test line.
 */
data class TestLine(
    val type: TestLineType,
    val text: String,
)
