package org.jetbrains.research.testgenie.tools.llm

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiClass
import org.jetbrains.research.testgenie.data.Report
import org.jetbrains.research.testgenie.data.TestCase
import org.jetbrains.research.testgenie.tools.llm.test.TestCaseGeneratedByLLM
import java.io.File

class TestCoverageCollector(
    private val indicator: ProgressIndicator,
    project: Project,
    private val resultPath: String,
    private val generatedTestFile: File,
    private val generatedTestPackage: String,
    private val projectBuildPath: String,
    private val testCases: MutableList<TestCaseGeneratedByLLM>,
    cut: PsiClass
) {
    private val sep = File.separatorChar
    private val junitTimeout: Long = 12000000 // TODO: Source from config
    private val javaHomeDirectory = ProjectRootManager.getInstance(project).projectSdk!!.homeDirectory!!

    // source path
    private val cutModule: Module = ProjectFileIndex.getInstance(project).getModuleForFile(
        cut.containingFile.virtualFile
    )!!
    private val sourceRoots = ModuleRootManager.getInstance(cutModule).getSourceRoots(false)
    private val report = Report()

    fun collect(): Report? {
        // the test file cannot be null
        if (!generatedTestFile.exists()) return null
        // compile the test file
        if (!compilation(generatedTestFile, projectBuildPath)) return null
        // run Jacoco on the compiled test file
        runJacoco()

        // TODO remove it :)
        report.testCaseList["mySuperFunction"] = TestCase(
            "mySuperFunction", "" +
                    "@Test(timeout = 4000)\n" +
                    "public void mySuperFunction() throws Throwable  {\n" +
                    "   System.out.println(\"Here is my super mega function!!!\");\n" +
                    "}", setOf(3, 4, 5), setOf(), setOf()
        )

        // collect the Jacoco results and return the report
        return report
    }

    private fun compilation(javaFile: File, buildPath: String): Boolean {
        indicator.text = "Compilation tests checking"

        // find the proper javac
        val javaCompile = File(javaHomeDirectory.path).walk().filter { it.name.equals("javac") && it.isFile }.first()
        // compile file
        runCommandLine(
            arrayListOf(
                javaCompile.absolutePath,
                "-cp",
                getPath(buildPath),
                javaFile.absolutePath
            )
        )

        // create .class file path
        val classFilePath = javaFile.absolutePath.replace(".java", ".class")

        // check is .class file exists
        return File(classFilePath).exists()
    }

    private fun runJacoco() {
        indicator.text = "Running jacoco"

        val className = generatedTestFile.name.split('.')[0]
        // find the proper javac
        val javaRunner = File(javaHomeDirectory.path).walk().filter { it.name.equals("java") && it.isFile }.first()
        // JaCoCo libs
        val jacocoAgentDir = getLibrary("jacocoagent.jar")
        val jacocoCLIDir = getLibrary("jacococli.jar")

        // Execute each test method separately
        testCases.map { it.name }.forEach {
            // name of .exec and .xml files
            val dataFileName = "${generatedTestFile.parentFile.absolutePath}/jacoco-$it"

            // run the test method with jacoco agent
            runCommandLine(
                arrayListOf(
                    javaRunner.absolutePath,
                    "-javaagent:$jacocoAgentDir=destfile=$dataFileName.exec,append=false",
                    "-cp",
                    "${getPath(projectBuildPath)}${getLibrary("JUnitRunner-1.0.jar")}:$resultPath",
                    "org.jetbrains.research.SingleJUnitTestRunner",
                    "$generatedTestPackage.$className#$it"
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

            // save data to TestGenerationResult
//            saveData(it, "$dataFileName.xml")

            runCommandLine(command as ArrayList<String>)
        }
    }

    private fun saveData(testName: String, xmlFileName: String) {
        TODO("implement it")
    }

    private fun runCommandLine(cmd: ArrayList<String>) {
        val compilationProcess = GeneralCommandLine(cmd)
        val handler = OSProcessHandler(compilationProcess)
        handler.startNotify()
        handler.waitFor(junitTimeout)
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
