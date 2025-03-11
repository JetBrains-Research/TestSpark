package org.jetbrains.research.testspark.data

import org.evosuite.result.BranchInfo
import org.evosuite.result.MutationInfo
import org.evosuite.utils.CompactReport
import org.evosuite.utils.CompactTestCase
import org.jetbrains.research.testspark.core.data.Report

class IJReport : Report {
    var allCoveredBranches: Set<BranchInfo> = setOf()
    var allUncoveredBranches: Set<BranchInfo> = setOf()
    var allCoveredMutation: Set<MutationInfo> = setOf()
    var allUncoveredMutation: Set<MutationInfo> = setOf()

    /**
     * Default constructor for Report
     */
    constructor()

    /**
     * Transformation CompactReport to Report
     *
     * @param compactReport is org.evosuite.utils.CompactReport object
     */
    constructor(compactReport: CompactReport) {
        uut = compactReport.UUT
        allCoveredLines = compactReport.allCoveredLines
        allUncoveredLines = compactReport.allUncoveredLines
        allCoveredBranches = compactReport.allCoveredBranches
        allUncoveredBranches = compactReport.allUncoveredBranches
        allCoveredMutation = compactReport.allCoveredMutation
        allUncoveredMutation = compactReport.allUncoveredMutation
        for ((index, compactTestCase: CompactTestCase) in compactReport.testCaseList.values.withIndex()) {
            testCaseList[index] = IJTestCase(index, compactTestCase)
        }
    }
}
