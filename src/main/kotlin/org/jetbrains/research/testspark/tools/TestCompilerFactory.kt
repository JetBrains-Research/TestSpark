package org.jetbrains.research.testspark.tools

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import org.jetbrains.research.testspark.core.data.JUnitVersion
import org.jetbrains.research.testspark.core.test.TestCompiler
import org.jetbrains.research.testspark.core.utils.Language

class TestCompilerFactory {
    companion object {
        fun createJavacTestCompiler(
            project: Project,
            junitVersion: JUnitVersion,
            javaHomeDirectory: String? = null,
        ): TestCompiler {
            val javaHomePath =
                javaHomeDirectory ?: ProjectRootManager.getInstance(project).projectSdk!!.homeDirectory!!.path
            val libraryPaths = LibraryPathsProvider.getTestCompilationLibraryPaths()
            val junitLibraryPaths = LibraryPathsProvider.getJUnitLibraryPaths(junitVersion)

            return TestCompiler(javaHomePath, libraryPaths, junitLibraryPaths)
        }
    }
}
