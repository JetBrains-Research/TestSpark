package org.jetbrains.research.testgenie.data

import org.jetbrains.research.testgenie.editor.Workspace

class TestGenerationData {
    // Result processing
    var testGenerationResultList: MutableList<Report?> = mutableListOf()
    var resultName: String = ""
    var fileUrl: String = ""

    // Code required of imports and package for generated tests
    var importsCode: String = ""
    var packageLine: String = ""

    // Maps a workspace file to the test generation jobs that were triggered on it.
    // Currently, the file key is represented by its presentableUrl
    var testGenerationResults: HashMap<String, ArrayList<Workspace.TestJob>> = HashMap()

    // Maps a test generation job id to its corresponding test job information
    var pendingTestResults: HashMap<String, Workspace.TestJobInfo> = HashMap()

    fun clear() {
        testGenerationResultList.clear()
        resultName = ""
        fileUrl = ""
        importsCode = ""
        packageLine = ""
        testGenerationResults.clear()
        pendingTestResults.clear()
    }
}
