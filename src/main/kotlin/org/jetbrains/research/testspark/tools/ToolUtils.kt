package org.jetbrains.research.testspark.tools

import com.intellij.openapi.components.service
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.CompilerModuleExtension
import com.intellij.openapi.roots.ModuleRootManager
import org.jetbrains.research.testspark.data.DataFilesUtil
import org.jetbrains.research.testspark.data.Report
import org.jetbrains.research.testspark.services.ErrorService
import org.jetbrains.research.testspark.services.JavaClassBuilderService
import org.jetbrains.research.testspark.services.ProjectContextService
import org.jetbrains.research.testspark.services.TestGenerationDataService
import org.jetbrains.research.testspark.services.TestStorageProcessingService
import org.jetbrains.research.testspark.services.TestsExecutionResultService
import org.jetbrains.research.testspark.tools.llm.generation.RequestManager
import java.io.File
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.writeText


class ProjectUnderTestFileCreator {
    companion object {
        var projectUnderTestOutputDirectory: String? = null

        fun getOrCreateFileInOutputDirectory(filename: String): Path {
            val filepath = Path.of("${projectUnderTestOutputDirectory!!}/generated-artifacts/$filename")
            // Create the parent directories if they don't exist
            val parentDir = filepath.toFile().parentFile
            parentDir.mkdirs()
            // Create the file
            filepath.toFile().createNewFile()
            return filepath
        }

        fun appendToFile(content: String, filepath: Path) {
            filepath.writeText(content, options = arrayOf(StandardOpenOption.APPEND))
        }
    }
}



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
 * @param report The report object to be added to the test generation result list.
 * @param packageLine The package declaration line of the test generation data.
 * @param importsCode The import statements code of the test generation data.
 */
fun saveData(
    project: Project,
    report: Report,
    packageLine: String,
    importsCode: MutableSet<String>,
) {
    project.service<TestGenerationDataService>().resultName = project.service<TestStorageProcessingService>().testResultName
    project.service<TestGenerationDataService>().fileUrl = project.service<ProjectContextService>().fileUrl!!
    project.service<TestGenerationDataService>().packageLine = packageLine
    project.service<TestGenerationDataService>().importsCode.addAll(importsCode)

    project.service<TestsExecutionResultService>().initExecutionResult(report.testCaseList.values.map { it.id })

    for (testCase in report.testCaseList.values) {
        val code = testCase.testCode
        testCase.testCode = project.service<JavaClassBuilderService>().generateCode(
            project.service<JavaClassBuilderService>().getClassWithTestCaseName(testCase.testName),
            code,
            project.service<TestGenerationDataService>().importsCode,
            project.service<TestGenerationDataService>().packageLine,
            project.service<TestGenerationDataService>().runWith,
            project.service<TestGenerationDataService>().otherInfo,
        )
    }

    project.service<TestGenerationDataService>().testGenerationResultList.add(report)
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

        compilerOutputPath?.let { buildPath += compilerOutputPath.path.plus(DataFilesUtil.classpathSeparator.toString()) }
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

            buildPath += lib.plus(DataFilesUtil.classpathSeparator.toString())
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
fun processStopped(project: Project, indicator: ProgressIndicator?): Boolean {
    if (project.service<ErrorService>().isErrorOccurred()) return true
    if (indicator != null && indicator.isCanceled) {
        project.service<ErrorService>().errorOccurred()
        indicator.stop()
        return true
    }
    return false
}
