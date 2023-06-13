package org.jetbrains.research.testgenie.tools

import org.evosuite.result.BranchInfo
import org.evosuite.result.MutationInfo
import org.evosuite.utils.CompactReport
import org.evosuite.utils.CompactTestCase
import org.jetbrains.research.testgenie.tools.llm.test.TestSuiteGeneratedByLLM

class TestsResult {
    // TODO think about list of fields
    var UUT: String = ""
    var testSuiteCode: String = ""
    var testScaffoldCode: String = ""
    var allCoveredLines: Set<Int> = emptySet()
    var allUncoveredLines: Set<Int> = emptySet()
    var allCoveredBranches: Set<BranchInfo> = emptySet()
    var allUncoveredBranches: Set<BranchInfo> = emptySet()
    var allCoveredMutation: Set<MutationInfo> = emptySet()
    var allUncoveredMutation: Set<MutationInfo> = emptySet()
    var testCaseList: HashMap<String, CompactTestCase> = hashMapOf()

    constructor(compactReport: CompactReport) {
        // TODO add better transformation
        this.UUT = compactReport.UUT
        this.testSuiteCode += compactReport.testSuiteCode
        this.testScaffoldCode += compactReport.testScaffoldCode
        this.allCoveredLines = this.allCoveredLines.plus(compactReport.allCoveredLines)
        this.allUncoveredLines = this.allUncoveredLines.plus(compactReport.allUncoveredLines)
        this.allCoveredBranches = this.allCoveredBranches.plus(compactReport.allCoveredBranches)
        this.allUncoveredBranches = this.allUncoveredBranches.plus(compactReport.allUncoveredBranches)
        this.allCoveredMutation = this.allCoveredMutation.plus(compactReport.allCoveredMutation)
        this.allUncoveredMutation = this.allUncoveredMutation.plus(compactReport.allUncoveredMutation)
        this.testCaseList.putAll(compactReport.testCaseList)
    }

    constructor(testSuiteGeneratedByLLM: TestSuiteGeneratedByLLM) {
        TODO("add better transformation")
    }
}
