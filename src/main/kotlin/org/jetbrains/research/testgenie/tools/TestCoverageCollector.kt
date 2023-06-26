package org.jetbrains.research.testgenie.tools

import com.intellij.coverage.CoverageDataManager
import com.intellij.coverage.CoverageSuitesBundle
import com.intellij.coverage.CoverageRunner
import com.intellij.coverage.DefaultCoverageFileProvider
import com.intellij.coverage.CoverageSuite
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import org.evosuite.utils.CompactReport
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Comparing
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.research.testgenie.services.SettingsProjectService
import java.io.File

class TestCoverageCollector(
        private val indicator: ProgressIndicator,
        private val project: Project,
) {
    private val sep = File.separatorChar
    private val pathSep = File.pathSeparatorChar
    private val junitTimeout: Long = 12000000 // TODO: Source from config

    fun collect(classpath: String): CompactReport? {
        if (!isCompilationCorrect(classpath)) return null
        runTests(classpath)
        return getCompactReport(runJacoco(classpath))
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

    private fun getPath(classpath: String): String {
        // create the path for the command
//        TODO do we need --> val targetProjectCP = testJobInfo.targetClassPath
        val pluginsPath = System.getProperty("idea.plugins.path")
        val junitPath = "$pluginsPath${sep}TestGenie${sep}lib${sep}junit-4.13.jar" // TODO why it is not in lib
        val standaloneRuntimePath = "$pluginsPath${sep}TestGenie${sep}lib${sep}standalone-runtime.jar"
        val hamcrestPath = "$pluginsPath${sep}TestGenie${sep}lib${sep}hamcrest-core-1.3.jar" // TODO why it is not in lib
        return "${junitPath}$pathSep$standaloneRuntimePath$pathSep$hamcrestPath${pathSep}$classpath"
    }

    private fun runTests(classpath: String) {
        indicator.text = "Running tests"

        val className = "" // TODO add class name

        // run test
        runCommandLine(
                arrayListOf(
                        project.service<SettingsProjectService>().state.javaPath,
                        "-cp",
                        getPath(classpath),
                        "org.junit.runner.JUnitCore",
                        className,
                ),
        ) // TODO return what?
    }

    private fun runJacoco(classpath: String): CoverageSuitesBundle {
        indicator.text = "Running jacoco"

        // create the path for the command
        val className = "" // TODO add class name
        val jacocoPath = "${System.getProperty("idea.plugins.path")}${sep}TestGenie${sep}lib${sep}jacocoagent.jar"
        val jacocoReportPath = "$classpath${sep}jacoco.exec" // TODO is classpath good for jacoco.exec?

        // run jacoco
        runCommandLine(
                arrayListOf(
                        project.service<SettingsProjectService>().state.javaPath,
                        "-javaagent:$jacocoPath=destfile=$jacocoReportPath",
                        "-cp",
                        getPath(classpath),
                        "org.junit.runner.JUnitCore",
                        className,
                ),
        )

        val manager = CoverageDataManager.getInstance(project)
        val virtualFile = LocalFileSystem.getInstance().findFileByPath(jacocoReportPath)!!
        val coverageRunner = getCoverageRunner(virtualFile)

        val coverageSuite: CoverageSuite = manager
                .addExternalCoverageSuite(
                        virtualFile.name,
                        virtualFile.timeStamp,
                        coverageRunner,
                        DefaultCoverageFileProvider(virtualFile.path),
                )

        return CoverageSuitesBundle(coverageSuite)
    }

    private fun getCoverageRunner(file: VirtualFile): CoverageRunner? {
        for (runner in CoverageRunner.EP_NAME.extensionList) {
            for (extension in runner.dataFileExtensions) {
                if (Comparing.strEqual(file.extension, extension)) return runner
            }
        }
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
        handler.waitFor(junitTimeout)
    }
}
