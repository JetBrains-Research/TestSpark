package org.jetbrains.research.testgenie.data

import org.evosuite.result.BranchInfo
import org.evosuite.result.MutationInfo
import org.evosuite.utils.CompactTestCase

class TestCase(
    var testName: String,
    var testCode: String,
    var coveredLines: Set<Int>,
    var coveredBranches: Set<BranchInfo>,
    var coveredMutants: Set<MutationInfo>
) {
    constructor(compactTestCase: CompactTestCase) : this(
        compactTestCase.testName,
        compactTestCase.testCode,
        compactTestCase.coveredLines,
        compactTestCase.coveredBranches,
        compactTestCase.coveredMutants
    )
}
