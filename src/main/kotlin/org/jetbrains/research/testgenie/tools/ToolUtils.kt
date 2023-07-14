package org.jetbrains.research.testgenie.tools

import com.intellij.openapi.components.service
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.CompilerModuleExtension
import com.intellij.openapi.roots.ModuleRootManager
import org.jetbrains.research.testgenie.data.Report
import org.jetbrains.research.testgenie.editor.Workspace
import java.io.File

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

fun clearDataBeforeTestGeneration(project: Project, testResultName: String) {
    val workspace = project.service<Workspace>()
    workspace.testGenerationData.clear(project)
    workspace.testGenerationData.pendingTestResults[testResultName] = project.service<Workspace>().key!!
}

fun cancelPendingResult(project: Project, testResultName: String) {
    project.service<Workspace>().testGenerationData.pendingTestResults.remove(testResultName)
}

fun getBuildPath(project: Project): String {
    var buildPath = ""

    for (module in ModuleManager.getInstance(project).modules) {
        val compilerOutputPath = CompilerModuleExtension.getInstance(module)?.compilerOutputPath
        compilerOutputPath?.let { buildPath += compilerOutputPath.path.plus(":") }

        // Include extra libraries in classpath
        val librariesPaths = ModuleRootManager.getInstance(module).orderEntries().librariesOnly().pathsList.pathList
        for (lib in librariesPaths) {
            // exclude the invalid classpaths
            if (buildPath.contains(lib)) {
                continue
            }
            if (lib.endsWith(".zip")) {
                continue
            }

            // remove junit and hamcrest libraries, since we use our own libraries
            val pathArray = lib.split(File.separatorChar)
            val libFileName = pathArray[pathArray.size - 1]
            if (libFileName.startsWith("junit") ||
                libFileName.startsWith("hamcrest")
            ) {
                continue
            }

            buildPath += lib.plus(":")
        }
    }
    return buildPath
}
