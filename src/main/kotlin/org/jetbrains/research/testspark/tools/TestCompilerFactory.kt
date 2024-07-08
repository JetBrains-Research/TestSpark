package org.jetbrains.research.testspark.tools

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import org.jetbrains.research.testspark.core.data.JUnitVersion
import org.jetbrains.research.testspark.core.test.kotlin.KotlinTestCompiler
import org.jetbrains.research.testspark.core.test.TestCompiler
import org.jetbrains.research.testspark.core.test.java.JavaTestCompiler
import org.jetbrains.research.testspark.core.utils.Language

class TestCompilerFactory {
    companion object {
        fun createTestCompiler(
            project: Project,
            junitVersion: JUnitVersion,
            javaHomeDirectory: String? = null,
            language: Language
        ): TestCompiler {
            val javaSDKHomePath =
                javaHomeDirectory ?: ProjectRootManager.getInstance(project).projectSdk?.homeDirectory?.path
                ?: throw RuntimeException("Java SDK not configured for the project.")

            val libraryPaths = LibraryPathsProvider.getTestCompilationLibraryPaths()
            val junitLibraryPaths = LibraryPathsProvider.getJUnitLibraryPaths(junitVersion)

            return when (language) {
                Language.Java -> JavaTestCompiler(javaSDKHomePath, libraryPaths, junitLibraryPaths)
                Language.Kotlin -> KotlinTestCompiler(libraryPaths, junitLibraryPaths)
            }
        }
    }
}

