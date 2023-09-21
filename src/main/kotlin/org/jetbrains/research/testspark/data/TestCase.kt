package org.jetbrains.research.testspark.data

import org.evosuite.result.BranchInfo
import org.evosuite.result.MutationInfo
import org.evosuite.utils.CompactTestCase

/**
 * Test case storage, implemented based on the org.evosuite.utils.CompactTestCase structure.
 */
class TestCase(
    val id: Int,
    var testName: String,
    var testCode: String,
    var coveredLines: Set<Int>,
    var coveredBranches: Set<BranchInfo>,
    var coveredMutants: Set<MutationInfo>,
) {

    /**
     * Transformation CompactTestCase to TestCase
     *
     * @param compactTestCase is org.evosuite.utils.CompactTestCase object
     */
    constructor(id: Int, compactTestCase: CompactTestCase) : this(
        id,
        compactTestCase.testName,
        compactTestCase.testCode,
        compactTestCase.coveredLines,
        compactTestCase.coveredBranches,
        compactTestCase.coveredMutants,
    )
}
