package org.jetbrains.research.testspark.tools

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import org.jetbrains.research.testspark.core.data.JUnitVersion
import org.jetbrains.research.testspark.core.test.Language
import org.jetbrains.research.testspark.core.test.TestCompiler

class TestCompilerFactory {
    companion object {
        fun createTestCompiler(
            project: Project,
            junitVersion: JUnitVersion,
            javaHomeDirectory: String? = null,
            language: Language,
        ): TestCompiler {
            val javaSDKHomePath =
                javaHomeDirectory ?: ProjectRootManager.getInstance(project).projectSdk?.homeDirectory?.path
                    ?: throw RuntimeException("Java SDK not configured for the project.")

            val libraryPaths = LibraryPathsProvider.getTestCompilationLibraryPaths()
            val junitLibraryPaths = LibraryPathsProvider.getJUnitLibraryPaths(junitVersion)

            return TestCompiler(javaSDKHomePath, libraryPaths, junitLibraryPaths, language)
        }
    }
}
