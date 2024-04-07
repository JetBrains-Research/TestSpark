package org.jetbrains.research.testspark.tools

import com.intellij.openapi.application.PathManager
import org.jetbrains.research.testspark.core.data.JUnitVersion
import java.io.File


/**
 * The class encapsulates logic for creation of library paths:
 * 1. Paths related to JUnit
 * 2. Paths related to test compilation
 * 3. Paths related to coverage
 */
class LibraryPathsProvider {
    companion object {
        private val sep = File.separatorChar
        private val libPrefix = "${PathManager.getPluginsPath()}${sep}TestSpark${sep}lib${sep}"

        fun getTestCompilationLibraryPaths() = listOf(
            "$libPrefix${sep}mockito-core-5.0.0.jar",
            "$libPrefix${sep}hamcrest-core-1.3.jar",
            "$libPrefix${sep}byte-buddy-1.14.6.jar",
            "$libPrefix${sep}byte-buddy-agent-1.14.6.jar",
        )

        fun getJUnitLibraryPaths(junitVersion: JUnitVersion): List<String> = junitVersion.libJar.map { "$libPrefix${sep}$it" }

        fun getJacocoCliLibraryPath() = "$libPrefix${sep}jacococli.jar"
        fun getJacocoAgentLibraryPath() = "$libPrefix${sep}jacocoagent.jar"
        fun getJUnitRunnerLibraryPath() = "$libPrefix${sep}JUnitRunner.jar"
    }
}