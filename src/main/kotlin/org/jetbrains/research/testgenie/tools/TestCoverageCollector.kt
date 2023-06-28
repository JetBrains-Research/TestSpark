package org.jetbrains.research.testgenie.tools

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.openapi.progress.ProgressIndicator
import org.evosuite.utils.CompactReport
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

class TestCoverageCollector(
    private val indicator: ProgressIndicator,
    private val project: Project,
) {
    private val sep = File.separatorChar
    private val pathSep = File.pathSeparatorChar
    private val junitTimeout: Long = 12000000 // TODO: Source from config
    private val javaHomeDirectory = ProjectRootManager.getInstance(project).projectSdk!!.homeDirectory!!

    fun collect(
        resultPath: String,
        classpath: String,
        buildPath: String,
        testCasesNames: List<String>,
        packageString: String,
        sourceRoots: Array<VirtualFile>
    ): CompactReport? {
        val javaFile = File(classpath)
        if (!javaFile.exists()) return null
        if (!compilation(javaFile, buildPath)) return null
        runJacoco(javaFile, buildPath, resultPath, testCasesNames, packageString, sourceRoots)
        return getCompactReport()
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

    private fun runJacoco(
        javaFile: File,
        buildPath: String,
        resultPath: String,
        testCasesNames: List<String>,
        packageString: String,
        sourceRoots: Array<VirtualFile>
    ) {
        indicator.text = "Running jacoco"

        // find the proper javac
        val javaRunner = File(javaHomeDirectory.path).walk().filter { it.name.equals("java") && it.isFile }.first()

        val className = javaFile.name.split('.')[0]
        val jacocoAgentDir = getLibrary("jacocoagent.jar")
        val jacocoCLIDir = getLibrary("jacococli.jar")

        testCasesNames.forEach {
            //java -javaagent:"$jacocoAgentDir=destfile=$generatedTestDir/jacoco2.exec,append=false" -cp "$baseDirs:$generatedTestDir:$libDir/JUnitRunner.jar" org.example.SingleJUnitTestRunner "org.jetbrains.person.$testFileName#testConstructor"
            // run jacoco
            runCommandLine(
                arrayListOf(
                    javaRunner.absolutePath,
                    "-javaagent:$jacocoAgentDir=destfile=${javaFile.parentFile.absolutePath}/jacoco-$it.exec,append=false",
                    "-cp",
                    "${getPath(buildPath)}${getLibrary("JUnitRunner-1.0.jar")}:$resultPath",
                    "org.jetbrains.research.SingleJUnitTestRunner",
                    "$packageString.$className#$it"
                ),
            )

            // java -jar $jacocoCLIAgent report “$generatedTestDir/jacoco2.exec” --classfiles “$compiledClasses” --sourcefiles “/Users/Pourina.Derakhshanfar/repos/TestAssignment/src/main/java”--xml “$generatedTestDir/jacoco2.xml”
            val command = mutableListOf(
                javaRunner.absolutePath,
                "-jar",
                jacocoCLIDir,
                "report",
                "${javaFile.parentFile.absolutePath}/jacoco-$it.exec",
            )

            buildPath.split(":").forEach { cp ->
                if (cp.trim().isNotEmpty() && cp.trim().isNotBlank()) {
                    command.add("--classfiles")
                    command.add(cp)
                }
            }

            sourceRoots.forEach { root ->
                command.add("--sourcefiles")
                command.add(root.path)
            }
            command.add("--xml")
            command.add("${javaFile.parentFile.absolutePath}/jacoco-$it.xml")

            runCommandLine(command as ArrayList<String>)
        }
    }

    private fun getCompactReport(): CompactReport {
        indicator.text = "Coverage data computing"
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
