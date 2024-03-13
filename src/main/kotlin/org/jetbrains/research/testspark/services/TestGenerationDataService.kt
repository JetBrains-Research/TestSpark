package org.jetbrains.research.testspark.services

import com.intellij.openapi.components.Service
import org.jetbrains.research.testspark.data.Report
import org.jetbrains.research.testspark.core.test.TestCaseGeneratedByLLM

@Service(Service.Level.PROJECT)
class TestGenerationDataService {
    // Result processing
    var testGenerationResultList: MutableList<Report?> = mutableListOf()
    var resultName: String = ""
    var fileUrl: String = ""

    // Code required of imports and package for generated tests
    var importsCode: MutableSet<String> = mutableSetOf()
    var packageLine: String = ""
    var runWith: String = ""
    var otherInfo: String = ""

    // changing parameters with a large prompt
    var polyDepthReducing: Int = 0
    var inputParamsDepthReducing: Int = 0

    // list of correct test cases during the incorrect compilation
    val compilableTestCases = mutableSetOf<TestCaseGeneratedByLLM>()

    /**
     * Cleaning all old data before new test generation.
     */
    fun clear() {
        testGenerationResultList.clear()
        resultName = ""
        fileUrl = ""
        importsCode = mutableSetOf()
        packageLine = ""
        runWith = ""
        otherInfo = ""
        polyDepthReducing = 0
        inputParamsDepthReducing = 0
        compilableTestCases.clear()
    }
}
