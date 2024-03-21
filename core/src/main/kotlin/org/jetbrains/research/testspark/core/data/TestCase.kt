package org.jetbrains.research.testspark.core.data

/**
 * Test case storage, implemented based on the org.evosuite.utils.CompactTestCase structure.
 */
open class TestCase(
    val id: Int,
    var testName: String,
    var testCode: String,
    var coveredLines: Set<Int>,
)
