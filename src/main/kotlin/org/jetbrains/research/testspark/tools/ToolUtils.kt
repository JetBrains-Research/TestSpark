package org.jetbrains.research.testspark.tools

import com.intellij.openapi.components.service
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.CompilerModuleExtension
import com.intellij.openapi.roots.ModuleRootManager
import org.jetbrains.research.testspark.TestSparkBundle
import org.jetbrains.research.testspark.data.Report
import org.jetbrains.research.testspark.editor.Workspace
import org.jetbrains.research.testspark.services.ErrorService
import org.jetbrains.research.testspark.services.TestCoverageCollectorService
import java.io.File

/**
 * Retrieves the imports code from a given test suite code.
 *
 * @param testSuiteCode The test suite code from which to extract the imports code. If null, an empty string is returned.
 * @param classFQN The fully qualified name of the class to be excluded from the imports code. It will not be included in the result.
 * @return The imports code extracted from the test suite code. If no imports are found or the result is empty after filtering, an empty string is returned.
 */
fun getImportsCodeFromTestSuiteCode(testSuiteCode: String?, classFQN: String): MutableSet<String> {
    testSuiteCode ?: return mutableSetOf()
    return testSuiteCode.replace("\r\n", "\n").split("\n").asSequence()
        .filter { it.contains("^import".toRegex()) }
        .filterNot { it.contains("evosuite".toRegex()) }
        .filterNot { it.contains("RunWith".toRegex()) }
        .filterNot { it.contains(classFQN.toRegex()) }.toMutableSet()
}

/**
 * Retrieves the package declaration from the given test suite code.
 *
 * @param testSuiteCode The generated code of the test suite.
 * @return The package declaration extracted from the test suite code, or an empty string if no package declaration was found.
 */
// get package from a generated code
fun getPackageFromTestSuiteCode(testSuiteCode: String?): String {
    testSuiteCode ?: return ""
    if (!testSuiteCode.contains("package")) return ""
    val result = testSuiteCode.replace("\r\n", "\n").split("\n")
        .filter { it.contains("^package".toRegex()) }.joinToString("").split("package ")[1].split(";")[0]
    if (result.isBlank()) return ""
    return result
}

/**
 * Saves the data related to test generation in the specified project's workspace.
 *
 * @param project The project in which the test generation data will be saved.
 * @param report The report object to be added to the test generation result list.\
 * @param packageLine The package declaration line of the test generation data.
 * @param importsCode The import statements code of the test generation data.
 */
fun saveData(
    project: Project,
    report: Report,
    packageLine: String,
    importsCode: MutableSet<String>,
    indicator: ProgressIndicator,
) {
    val workspace = project.service<Workspace>()
    workspace.testGenerationData.resultName = project.service<Workspace>().testResultName!!
    workspace.testGenerationData.fileUrl = project.service<Workspace>().fileUrl!!
    workspace.testGenerationData.packageLine = packageLine
    workspace.testGenerationData.importsCode.addAll(importsCode)

    indicator.text = TestSparkBundle.message("testExecutionMessage")

    for (testCase in report.testCaseList.values) {
        indicator.text = "Executing ${testCase.testName}"
        project.service<TestCoverageCollectorService>().updateDataWithTestCase(testCase.testCode, testCase.testName)
    }

    workspace.testGenerationData.testGenerationResultList.add(report)
}

/**
 * Retrieves the key for a test job in the workspace.
 *
 * @param fileUrl The URL of the file associated with the test job.
 * @param classFQN The fully qualified name of the class associated with the test job.
 * @param modTs The modification timestamp of the file associated with the test job.
 * @param testResultName The name of the test result associated with the test job.
 * @param projectClassPath The classpath of the project associated with the test job.
 * @return The test job information containing the provided parameters.
 */
fun getKey(project: Project, classFQN: String): Workspace.TestJobInfo =
    Workspace.TestJobInfo(
        project.service<Workspace>().fileUrl!!,
        classFQN,
        project.service<Workspace>().modificationStamp!!,
        project.service<Workspace>().testResultName!!,
        project.service<Workspace>().projectClassPath!!,
    )

/**
 * Clears the data before test generation for a specific test result.
 *
 * @param project The project for which the test generation data needs to be cleared.
 * @param testResultName The name of the test result for which the data needs to be cleared.
 */
fun clearDataBeforeTestGeneration(project: Project, testResultName: String) {
    val workspace = project.service<Workspace>()
    workspace.clear(project)
    workspace.testGenerationData.pendingTestResults[testResultName] = project.service<Workspace>().key!!
}

/**
 * Retrieves the build path for the given project.
 *
 * @param project the project for which to retrieve the build path
 * @return the build path as a string
 */
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

/**
 * Checks if the process has been stopped.
 *
 * @param project the project in which the process is running
 * @param indicator the progress indicator for tracking the progress of the process
 *
 * @return true if the process has been stopped, false otherwise
 */
fun processStopped(project: Project, indicator: ProgressIndicator): Boolean {
    if (project.service<ErrorService>().isErrorOccurred()) return true
    if (indicator.isCanceled) {
        project.service<ErrorService>().errorOccurred()
        indicator.stop()
        return true
    }
    return false
}

/**
 * Checks if the length of the given text is within the specified limit.
 *
 * @param text The text to check.
 * @param limit The maximum length limit in bytes. Defaults to 16384 bytes (4096 * 4).
 * @return `true` if the length of the text is within the limit, `false` otherwise.
 */
fun isPromptLengthWithinLimit(text: String, limit: Int = 4096 * 4): Boolean { // Average of 4 bytes per token
    return text.toByteArray(Charsets.UTF_8).size <= limit
}
