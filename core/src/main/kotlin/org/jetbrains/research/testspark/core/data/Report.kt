package org.jetbrains.research.testspark.core.data

/**
 * Storage of generated tests. Implemented on the basis of org.evosuite.utils.CompactReport structure.
 */
open class Report {
    // Fields were created based on the fields in org.evosuite.utils.CompactReport for easier transformation
    var UUT: String = "" // Unit Under Test
    var allCoveredLines: Set<Int> = setOf()
    var allUncoveredLines: Set<Int> = setOf()
    var testCaseList: HashMap<Int, TestCase> = hashMapOf()

    /**
     * AllCoveredLines update
     */
    fun normalized(): Report {
        allCoveredLines = testCaseList.values.map { it.coveredLines }.flatten().toSet()
        return this
    }
}
