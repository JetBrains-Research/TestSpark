package org.jetbrains.research.testgenie.tools

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.jetbrains.research.testgenie.data.Report
import org.jetbrains.research.testgenie.editor.Workspace
import org.jetbrains.research.testgenie.tools.evosuite.Pipeline

// get junit imports from a generated code
fun getImportsCodeFromTestSuiteCode(testSuiteCode: String?, classFQN: String): String {
    testSuiteCode ?: return ""
    val result = testSuiteCode.replace("\r\n", "\n").split("\n").asSequence()
        .filter { it.contains("^import".toRegex()) }
        .filterNot { it.contains("evosuite".toRegex()) }
        .filterNot { it.contains("RunWith".toRegex()) }
        .filterNot { it.contains(classFQN.toRegex()) }
        .joinToString("\n").plus("\n")
    if (result.isBlank()) return ""
    return result
}

// get package from a generated code
fun getPackageFromTestSuiteCode(testSuiteCode: String?): String {
    testSuiteCode ?: return ""
    val result = testSuiteCode.replace("\r\n", "\n").split("\n")
        .filter { it.contains("^package".toRegex()) }
        .joinToString("\n").plus("\n")
    if (result.isBlank()) return ""
    return result
}

fun saveData(project: Project, report: Report, resultName: String, fileUrl: String, packageLine: String, importsCode: String) {
    val workspace = project.service<Workspace>()
    workspace.testGenerationData.testGenerationResultList.add(report)
    workspace.testGenerationData.resultName = resultName
    workspace.testGenerationData.fileUrl = fileUrl
    workspace.testGenerationData.packageLine = packageLine
    workspace.testGenerationData.importsCode = importsCode
}

fun getKey(fileUrl: String, classFQN: String, modTs: Long, testResultName: String, projectClassPath: String): Workspace.TestJobInfo =
    Workspace.TestJobInfo(fileUrl, classFQN, modTs, testResultName, projectClassPath)

fun clearDataBeforeTestGeneration(project: Project, key: Workspace.TestJobInfo, testResultName: String) {
    val workspace = project.service<Workspace>()
    workspace.testGenerationData.clear()
    workspace.testGenerationData.pendingTestResults[testResultName] = key
}

fun receiveGenerationResult(
    project: Project,
    testResultName: String,
    report: Report,
    cacheLazyPipeline: Pipeline? = null,
    cachedJobKey: Workspace.TestJobInfo? = null,
) {
    project.service<Workspace>().receiveGenerationResult(testResultName, report, cacheLazyPipeline, cachedJobKey)
}

fun cancelPendingResult(project: Project, testResultName: String) {
    project.service<Workspace>().testGenerationData.pendingTestResults.remove(testResultName)
}
