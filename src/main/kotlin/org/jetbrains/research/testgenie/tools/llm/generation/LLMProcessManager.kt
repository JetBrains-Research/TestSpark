package org.jetbrains.research.testgenie.tools.llm.generation

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.CompilerModuleExtension
import org.jetbrains.research.testgenie.TestGenieBundle
import org.jetbrains.research.testgenie.services.SettingsProjectService
import org.jetbrains.research.testgenie.services.TestCaseDisplayService
import org.jetbrains.research.testgenie.tools.TestCoverageCollector
import org.jetbrains.research.testgenie.tools.llm.error.LLMErrorManager
import org.jetbrains.research.testgenie.tools.llm.test.TestLineType
import org.jetbrains.research.testgenie.tools.llm.test.TestSuiteGeneratedByLLM
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.createDirectories

class LLMProcessManager(
    private val project: Project,
    private val projectPath: String,
    private val projectClassPath: String,
    private val fileUrl: String,
) {
    private val settingsProjectState = project.service<SettingsProjectService>().state
    private var finalPathAddress: String = ""
    private var testFileName: String = "GeneratedTest.java"

    fun runLLMTestGenerator(
        indicator: ProgressIndicator,
        prompt: String,
        log: Logger,
        resultPath: String,
        packageName: String,
    ) {
        // update build path
        var buildPath = projectClassPath
        if (settingsProjectState.buildPath.isEmpty()) {
            // User did not set own path
            buildPath = ""
            for (module in ModuleManager.getInstance(project).modules) {
                val compilerOutputPath = CompilerModuleExtension.getInstance(module)?.compilerOutputPath
                compilerOutputPath?.let { buildPath += compilerOutputPath.path.plus(":") }
            }
        }
        indicator.text = TestGenieBundle.message("searchMessage")

        // Send request to LLM
        val generatedTestSuite: TestSuiteGeneratedByLLM = LLMRequest().request(prompt, indicator, packageName)

        // Check if response is not empty
        if (generatedTestSuite.isEmpty()) {
            LLMErrorManager.displayEmptyTests(project)
            return
        }

        // Save the generated TestSuite into a temp file
        finalPathAddress = "$resultPath${File.separatorChar}"
        generatedTestSuite.packageString.split(".").forEach { directory ->
            if (directory.isNotBlank()) finalPathAddress += "$directory${File.separatorChar}"
        }
        saveGeneratedTests(generatedTestSuite)

        // TODO move this operation to Manager
        // TODO work with null value
        project.service<TestCaseDisplayService>().testGenerationResultList.add(
            TestCoverageCollector(
                indicator,
                project
            ).collect("$finalPathAddress$testFileName")
        )
    }

    private fun saveGeneratedTests(generatedTestSuite: TestSuiteGeneratedByLLM) {
        Path(finalPathAddress).createDirectories()

        val testFile = File("$finalPathAddress${File.separatorChar}$testFileName")
        testFile.createNewFile()
        println("Save test in file " + testFile.absolutePath)

        // Add package
        if (generatedTestSuite.packageString.isNotBlank()) {
            testFile.appendText("package ${generatedTestSuite.packageString};\n")
        }

        // add imports
        generatedTestSuite.imports.forEach { importedElement ->
            testFile.appendText("import $importedElement\n")
        }

        // open the test class
        testFile.appendText("public class generatedTest{\n\n")

        // print each test
        generatedTestSuite.testCases.forEach { testCase ->
            // Add test annotation
            testFile.appendText("\t@Test")

            // add expectedException if it exists
            if (testCase.expectedException.isNotBlank()) {
                testFile.appendText("(expected = ${testCase.expectedException})")
            }

            // start writing the test signature
            testFile.appendText("\n\tpublic void ${testCase.name}() ")

            // add throws exception if exists
            if (testCase.throwsException.isNotBlank()) {
                testFile.appendText("throws ${testCase.throwsException}")
            }

            // start writing the test lines
            testFile.appendText("{\n")

            // write each line
            testCase.lines.forEach { line ->
                when (line.type) {
                    TestLineType.BREAK -> testFile.appendText("\t\t\n")
                    else -> testFile.appendText("\t\t${line.text}\n")
                }
            }
            // close test case
            testFile.appendText("\t}\n")
        }

        // close the test class
        testFile.appendText("}")
    }
}
