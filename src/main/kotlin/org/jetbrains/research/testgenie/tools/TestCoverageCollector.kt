package org.jetbrains.research.testgenie.tools

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.openapi.progress.ProgressIndicator
import org.evosuite.utils.CompactReport
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import java.io.File
import java.nio.file.Files

class TestCoverageCollector(
    private val indicator: ProgressIndicator,
    private val project: Project,
) {
    private val sep = File.separatorChar
    private val pathSep = File.pathSeparatorChar
    private val junitTimeout: Long = 12000000 // TODO: Source from config

    fun collect(classpath: String, buildPath: String): CompactReport? {
        val javaFile = File(classpath)
        if (!javaFile.exists()) return null
        if (!compilation(javaFile, buildPath)) return null
        runJacoco(javaFile)
        return getCompactReport()
    }

    private fun compilation(javaFile: File, buildPath: String): Boolean {
        indicator.text = "Compilation tests checking"

        // find the proper javac
        val javaHomeDirectory = ProjectRootManager.getInstance(project).projectSdk!!.homeDirectory!!
        val javaCompile = File(javaHomeDirectory.path).walk().filter {it.name.equals("javac") && it.isFile}.first()
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

    private fun getPath(buildPath: String): String {
        // create the path for the command
        val pluginsPath = System.getProperty("idea.plugins.path")
        val junitPath = "$pluginsPath${sep}TestGenie${sep}lib${sep}junit-4.13.jar"
        val mockitoPath = "$pluginsPath${sep}TestGenie${sep}lib${sep}mockito-core-5.0.0.jar"
        val hamcrestPath = "$pluginsPath${sep}TestGenie${sep}lib${sep}hamcrest-core-1.3.jar"
        return "$junitPath:$hamcrestPath:$mockitoPath:$buildPath"
    }
}
