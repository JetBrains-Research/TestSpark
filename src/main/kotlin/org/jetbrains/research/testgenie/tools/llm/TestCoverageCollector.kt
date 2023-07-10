package org.jetbrains.research.testgenie.tools.llm

import com.gitlab.mvysny.konsumexml.konsumeXml
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ScriptRunnerUtil
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectRootManager
import org.jetbrains.research.testgenie.TestGenieBundle
import org.jetbrains.research.testgenie.data.Report
import org.jetbrains.research.testgenie.data.TestCase
import org.jetbrains.research.testgenie.tools.llm.error.LLMErrorManager
import org.jetbrains.research.testgenie.tools.llm.test.TestCaseGeneratedByLLM
import java.io.File

class TestCoverageCollector(
    private val indicator: ProgressIndicator,
    private val project: Project,
    private val resultPath: String,
    private val generatedTestFile: File,
    private val generatedTestPackage: String,
    private val projectBuildPath: String,
    private val testCases: MutableList<TestCaseGeneratedByLLM>,
    cutModule: Module,
    private val fileNameFQN: String,
) {
    private val sep = File.separatorChar
    private val javaHomeDirectory = ProjectRootManager.getInstance(project).projectSdk!!.homeDirectory!!

    // source path
    private val sourceRoots = ModuleRootManager.getInstance(cutModule).getSourceRoots(false)
    private val report = Report()

    fun collect(): Report {
        // run Jacoco on the compiled test file
        runJacoco()

        // collect the Jacoco results and return the report
        return report.normalized()
    }

    fun compile(): Pair<Boolean, String> {
        indicator.text = TestGenieBundle.message("compilationTestsChecking")

        // find the proper javac
        val javaCompile = File(javaHomeDirectory.path).walk().filter { it.name.equals("javac") && it.isFile }.first()
        // compile file
        val errorMsg = runCommandLine(
            arrayListOf(
                javaCompile.absolutePath,
                "-cp",
                getPath(projectBuildPath),
                generatedTestFile.absolutePath,
            ),
        )

        // create .class file path
        val classFilePath = generatedTestFile.absolutePath.replace(".java", ".class")

        // check is .class file exists
        return Pair(File(classFilePath).exists(), errorMsg)
    }

    private fun runJacoco() {
        indicator.text = TestGenieBundle.message("runningJacoco")

        val className = generatedTestFile.name.split('.')[0]
        // find the proper javac
        val javaRunner = File(javaHomeDirectory.path).walk().filter { it.name.equals("java") && it.isFile }.first()
        // JaCoCo libs
        val jacocoAgentDir = getLibrary("jacocoagent.jar")
        val jacocoCLIDir = getLibrary("jacococli.jar")

        // Execute each test method separately
        for (testCase in testCases) {
            // name of .exec and .xml files
            val dataFileName = "${generatedTestFile.parentFile.absolutePath}/jacoco-${testCase.name}"

            // run the test method with jacoco agent
            runCommandLine(
                arrayListOf(
                    javaRunner.absolutePath,
                    "-javaagent:$jacocoAgentDir=destfile=$dataFileName.exec,append=false",
                    "-cp",
                    "${getPath(projectBuildPath)}${getLibrary("JUnitRunner-1.0.jar")}:$resultPath",
                    "org.jetbrains.research.SingleJUnitTestRunner",
                    "$generatedTestPackage$className#${testCase.name}",
                ),
            )

            // Prepare the command for generating the Jacoco report
            val command = mutableListOf(
                javaRunner.absolutePath,
                "-jar",
                jacocoCLIDir,
                "report",
                "$dataFileName.exec",
            )

            // for each classpath
            projectBuildPath.split(":").forEach { cp ->
                if (cp.trim().isNotEmpty() && cp.trim().isNotBlank()) {
                    command.add("--classfiles")
                    command.add(cp)
                }
            }

            // for each source folder
            sourceRoots.forEach { root ->
                command.add("--sourcefiles")
                command.add(root.path)
            }

            // generate XML report
            command.add("--xml")
            command.add("$dataFileName.xml")

            runCommandLine(command as ArrayList<String>)

            // check if XML report is produced
            if (!File("$dataFileName.xml").exists()) {
                LLMErrorManager().errorProcess("Something went wrong with generating Jacoco report.", project)
            }

            // save data to TestGenerationResult
            saveData(testCase, "$dataFileName.xml")
        }
    }

    private fun saveData(testCase: TestCaseGeneratedByLLM, xmlFileName: String) {
        indicator.text = TestGenieBundle.message("testCasesSaving")
        val setOfLines = mutableSetOf<Int>()
        var isCorrectSourceFile: Boolean
        File(xmlFileName).readText().konsumeXml().apply {
            children("report") {
                children("sessioninfo") {}
                children("package") {
                    children("class") {
                        children("method") {
                            children("counter") {}
                        }
                        children("counter") {}
                    }
                    children("sourcefile") {
                        isCorrectSourceFile = this.attributes.getValue("name") == fileNameFQN
                        children("line") {
                            if (isCorrectSourceFile && this.attributes.getValue("mi") == "0") {
                                setOfLines.add(this.attributes.getValue("nr").toInt())
                            }
                        }
                        children("counter") {}
                    }
                    children("counter") {}
                }
                children("counter") {}
            }
        }
        report.testCaseList[testCase.name] = TestCase(
            testCase.name,
            testCase.toString(),
            setOfLines,
            setOf(),
            setOf(),
        )
    }

    private fun runCommandLine(cmd: ArrayList<String>): String {
        val compilationProcess = GeneralCommandLine(cmd)
        return ScriptRunnerUtil.getProcessOutput(compilationProcess, ScriptRunnerUtil.STDERR_OUTPUT_KEY_FILTER, 30000)
    }

    private fun getPath(buildPath: String): String {
        // create the path for the command
        val pluginsPath = System.getProperty("idea.plugins.path")
        val junitPath = "$pluginsPath${sep}TestGenie${sep}lib${sep}junit-4.13.jar"
        val mockitoPath = "$pluginsPath${sep}TestGenie${sep}lib${sep}mockito-core-5.0.0.jar"
        val hamcrestPath = "$pluginsPath${sep}TestGenie${sep}lib${sep}hamcrest-core-1.3.jar"
        return "$junitPath:$hamcrestPath:$mockitoPath:$buildPath"
    }

    private fun getLibrary(libraryName: String): String {
        val pluginsPath = System.getProperty("idea.plugins.path")
        return "$pluginsPath${sep}TestGenie${sep}lib${sep}$libraryName"
    }
}
