package org.jetbrains.research.testgenie.tools

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.openapi.progress.ProgressIndicator
import org.evosuite.utils.CompactReport
import com.intellij.openapi.project.Project
import java.io.File

class TestCoverageCollector(private val indicator: ProgressIndicator, private val project: Project) {
    fun main() {
        TODO("change name and implement it")
    }

    fun isCompilationCorrect(classpath: String): Boolean {
        indicator.text = "Compilation tests checking"

        // compile file
        runCommandLine(arrayListOf("javac", classpath))

        // create .class file path
        val classFilePath = classpath.replace(".java", ".class")

        // check is .class file exists
        val result = File(classFilePath).exists()

        // remove .class file
        if (result) runCommandLine(arrayListOf("rm", classFilePath)) // TODO check the classpath structure

        return result
    }

    fun runTests() {
        indicator.text = "Running tests"
    }

    fun runJacoco() {
        indicator.text = "Running jacoco"
    }

    fun getCompactReport(): CompactReport {
        indicator.text = "Coverage data computing"
        TODO("implement jacoco data to CompactReport transforming")
    }

    private fun runCommandLine(cmd: ArrayList<String>) {
        val compilationProcess = GeneralCommandLine(cmd)
        val handler = OSProcessHandler(compilationProcess)
        handler.startNotify()
    }
}
