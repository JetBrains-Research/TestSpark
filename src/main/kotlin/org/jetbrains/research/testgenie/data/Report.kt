package org.jetbrains.research.testgenie.data

import org.evosuite.result.BranchInfo
import org.evosuite.result.MutationInfo
import org.evosuite.utils.CompactReport

/**
 * Storage of generated tests. Implemented on the basis of org.evosuite.utils.CompactReport structure.
 */
class Report {
    // Fields were created based on the fields in org.evosuite.utils.CompactReport for easier transformation
    var UUT: String = ""
    var allCoveredLines: Set<Int> = setOf()
    var allUncoveredLines: Set<Int> = setOf()
    var allCoveredBranches: Set<BranchInfo> = setOf()
    var allUncoveredBranches: Set<BranchInfo> = setOf()
    var allCoveredMutation: Set<MutationInfo> = setOf()
    var allUncoveredMutation: Set<MutationInfo> = setOf()
    var testCaseList: HashMap<String, TestCase> = hashMapOf()

    /**
     * Transformation CompactReport to Report
     *
     * @param compactReport is org.evosuite.utils.CompactReport object
     */
    constructor(compactReport: CompactReport) {
        UUT = compactReport.UUT
        allCoveredLines = compactReport.allCoveredLines
        allUncoveredLines = compactReport.allUncoveredLines
        allCoveredBranches = compactReport.allCoveredBranches
        allUncoveredBranches = compactReport.allUncoveredBranches
        allCoveredMutation = compactReport.allCoveredMutation
        allUncoveredMutation = compactReport.allUncoveredMutation
        testCaseList = HashMap(compactReport.testCaseList.map { (key, value) -> key to TestCase(value) }.toMap())
    }

    /**
     * Default constructor for Report
     */
    constructor()

    /**
     * AllCoveredLines update
     */
    fun normalized(): Report {
        allCoveredLines = testCaseList.values.map { it.coveredLines }.flatten().toSet()
        return this
    }
}
