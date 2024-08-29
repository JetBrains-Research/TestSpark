package org.jetbrains.research.testspark.tools

import com.intellij.openapi.application.PathManager
import org.jetbrains.research.testspark.core.data.JUnitVersion
import org.jetbrains.research.testspark.core.test.data.dependencies.TestCompilationDependencies
import java.io.File

/**
 * The class encapsulates logic for creation of library paths:
 * 1. Paths related to JUnit
 * 2. Paths related to test compilation
 * 3. Paths related to coverage
 */
object LibraryPathsProvider {
    private val sep = File.separatorChar
    private val libPrefix = "${PathManager.getPluginsPath()}${sep}TestSpark${sep}lib$sep"

    fun getTestCompilationLibraryPaths() = TestCompilationDependencies.getJarDescriptors().map { descriptor ->
        "$libPrefix${descriptor.name}"
    }

    fun getJUnitLibraryPaths(junitVersion: JUnitVersion): List<String> = junitVersion.libJar.map { descriptor ->
        "$libPrefix${descriptor.name}"
    }

    fun getJacocoCliLibraryPath() = "${libPrefix}jacococli.jar"
    fun getJacocoAgentLibraryPath() = "${libPrefix}jacocoagent.jar"
    fun getJUnitRunnerLibraryPath() = "${libPrefix}JUnitRunner.jar"
}
