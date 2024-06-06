package org.jetbrains.research.testspark.data

import org.evosuite.result.BranchInfo
import org.evosuite.result.MutationInfo
import org.evosuite.utils.CompactTestCase
import org.jetbrains.research.testspark.core.data.TestCase

class IJTestCase(
    id: Int,
    testName: String,
    testCode: String,
    coveredLines: Set<Int>,
) : TestCase(id, testName, testCode, coveredLines) {

    var coveredBranches: Set<BranchInfo> = setOf()
    var coveredMutants: Set<MutationInfo> = setOf()

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
    ) {
        coveredBranches = compactTestCase.coveredBranches
        coveredMutants = compactTestCase.coveredMutants
    }

    constructor(
        id: Int,
        testName: String,
        testCode: String,
        coveredLines: Set<Int>,
        coveredBranches: Set<BranchInfo>,
        coveredMutants: Set<MutationInfo>,
    ) : this(
        id,
        testName,
        testCode,
        coveredLines,
    ) {
        this.coveredBranches = coveredBranches
        this.coveredMutants = coveredMutants
    }
}
