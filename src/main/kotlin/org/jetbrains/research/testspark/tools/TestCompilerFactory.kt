package org.jetbrains.research.testspark.tools

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import org.jetbrains.research.testspark.core.data.JUnitVersion
import org.jetbrains.research.testspark.core.test.SupportedLanguage
import org.jetbrains.research.testspark.core.test.TestCompiler
import org.jetbrains.research.testspark.core.test.java.JavaTestCompiler
import org.jetbrains.research.testspark.core.test.kotlin.KotlinTestCompiler

class TestCompilerFactory {
    companion object {
        fun createTestCompiler(
            project: Project,
            junitVersion: JUnitVersion,
            language: SupportedLanguage,
            javaHomeDirectory: String? = null,
        ): TestCompiler {
            val javaSDKHomePath =
                javaHomeDirectory ?: ProjectRootManager.getInstance(project).projectSdk?.homeDirectory?.path
                    ?: throw RuntimeException("Java SDK not configured for the project.")

            val libraryPaths = LibraryPathsProvider.getTestCompilationLibraryPaths()
            val junitLibraryPaths = LibraryPathsProvider.getJUnitLibraryPaths(junitVersion)

            // TODO add the warning window that for Java we always need the javaHomeDirectoryPath
            return when (language) {
                SupportedLanguage.Java -> JavaTestCompiler(libraryPaths, junitLibraryPaths, javaSDKHomePath)
                SupportedLanguage.Kotlin -> KotlinTestCompiler(libraryPaths, junitLibraryPaths)
            }
        }
    }
}
