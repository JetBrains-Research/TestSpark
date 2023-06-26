package org.jetbrains.research.testgenie.tools

import com.intellij.coverage.CoverageSuitesBundle
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import org.evosuite.utils.CompactReport
import com.intellij.openapi.project.Project
import org.jetbrains.research.testgenie.services.SettingsProjectService
import java.io.File

class TestCoverageCollector(
    private val indicator: ProgressIndicator,
    private val project: Project,
) {
    private val sep = File.separatorChar
    private val pathSep = File.pathSeparatorChar

    fun collect(classpath: String): CompactReport? {
        if (!isCompilationCorrect(classpath)) return null
        runTests(classpath)
        val jacocoResult = runJacoco(classpath)
        jacocoResult ?: return null
        return getCompactReport(jacocoResult)
    }

    private fun isCompilationCorrect(classpath: String): Boolean {
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

    private fun runTests(classpath: String) {
        indicator.text = "Running tests"

        // create the path for the command
//        TODO do we need --> val targetProjectCP = testJobInfo.targetClassPath
        val pluginsPath = System.getProperty("idea.plugins.path")
        val junitPath = "$pluginsPath${sep}TestGenie${sep}lib${sep}junit-4.13.jar" // TODO why it is not in lib
        val standaloneRuntimePath = "$pluginsPath${sep}TestGenie${sep}lib${sep}standalone-runtime.jar"
        val hamcrestPath = "$pluginsPath${sep}TestGenie${sep}lib${sep}hamcrest-core-1.3.jar" // TODO why it is not in lib
        val className = "" // TODO
        val path = "${junitPath}$pathSep$standaloneRuntimePath$pathSep$hamcrestPath${pathSep}$classpath"

        // run test
        runCommandLine(
            arrayListOf(
                project.service<SettingsProjectService>().state.javaPath,
                "-cp",
                path,
                "org.junit.runner.JUnitCore",
                className,
            ),
        ) // TODO return what?
    }

    private fun runJacoco(classpath: String): CoverageSuitesBundle? {
        indicator.text = "Running jacoco"

        // create the path for the command
        val className = "" // TODO
        val pluginsPath = System.getProperty("idea.plugins.path")
        val jacocoPath = "$pluginsPath${sep}TestGenie${sep}lib${sep}jacocoagent.jar"
        val jacocoReportPath = "$classpath${sep}jacoco.exec" // TODO is classpath good for jacoco.exec?
        val path = "" // TODO

        // run jacoco
        runCommandLine(
            arrayListOf(
                project.service<SettingsProjectService>().state.javaPath,
                "-javaagent:$jacocoPath=destfile=$jacocoReportPath",
                "-cp",
                path,
                "org.junit.runner.JUnitCore",
                className,
            ),
        ) // TODO return what?
        return null
    }

    private fun getCompactReport(coverageSuitesBundle: CoverageSuitesBundle): CompactReport {
        indicator.text = "Coverage data computing"
        TODO("implement coverageSuitesBundle to CompactReport transforming")
    }

    private fun runCommandLine(cmd: ArrayList<String>) {
        val compilationProcess = GeneralCommandLine(cmd)
        val handler = OSProcessHandler(compilationProcess)
        handler.startNotify()
    }
}
