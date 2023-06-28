package org.jetbrains.research.testgenie.tools

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.openapi.progress.ProgressIndicator
import org.evosuite.utils.CompactReport
import com.intellij.openapi.project.Project
import java.io.File

class TestCoverageCollector(
    private val indicator: ProgressIndicator,
    private val project: Project,
) {
    private val sep = File.separatorChar
    private val pathSep = File.pathSeparatorChar
    private val junitTimeout: Long = 12000000 // TODO: Source from config

    fun collect(classpath: String): CompactReport? {
        val javaFile = File(classpath)
        if (!javaFile.exists()) return null
        if (!compilation(javaFile)) return null
        runJacoco(javaFile)
        return getCompactReport()
    }

    private fun compilation(javaFile: File): Boolean {
        indicator.text = "Compilation tests checking"

        // compile file
        runCommandLine(
            arrayListOf(
                "javac",
                javaFile.absolutePath
            )
        ) // TODO change "javac" to ProjectRootManager.getInstance(project).projectSdk.homeDirectory

        // create .class file path
        val classFilePath = javaFile.absolutePath.replace(".java", ".class")

        // check is .class file exists
        val result = File(classFilePath).exists()

        // remove .class file
        if (result) runCommandLine(arrayListOf("rm", classFilePath))

        return result
    }

    private fun runJacoco(javaFile: File) {
        indicator.text = "Running jacoco"

        val className = javaFile.name.split('.')[0]

        // run jacoco
        runCommandLine(
            arrayListOf(
            ),
        )
        TODO("implement it")
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

    private fun getPath(): String {
        // create the path for the command
        val pluginsPath = System.getProperty("idea.plugins.path")
        val junitPath = "$pluginsPath${sep}TestGenie${sep}lib${sep}junit-4.13.jar"
        val standaloneRuntimePath = "$pluginsPath${sep}TestGenie${sep}lib${sep}standalone-runtime.jar"
        val hamcrestPath = "$pluginsPath${sep}TestGenie${sep}lib${sep}hamcrest-core-1.3.jar"
        return ""
    }
}
