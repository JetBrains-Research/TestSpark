package org.jetbrains.research.testspark.tools

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.SdkTypeId
import com.intellij.openapi.roots.ProjectRootManager
import org.jetbrains.research.testspark.core.data.JUnitVersion
import org.jetbrains.research.testspark.core.test.KotlinTestCompiler
import org.jetbrains.research.testspark.core.test.TestCompiler
import org.jetbrains.research.testspark.core.test.java.JavaTestCompiler
import org.jetbrains.research.testspark.core.utils.Language
import java.io.File

class TestCompilerFactory {
    companion object {
        fun createTestCompiler(
            project: Project,
            junitVersion: JUnitVersion,
            javaHomeDirectory: String? = null,
            language: Language
        ): TestCompiler {
            val javaHomePath = javaHomeDirectory ?: findJavaHomeDirectory(project)
//            val kotlinHomePath = findKotlinHomeDirectory(project)
            val libraryPaths = LibraryPathsProvider.getTestCompilationLibraryPaths()
            val junitLibraryPaths = LibraryPathsProvider.getJUnitLibraryPaths(junitVersion)

            return when (language) {
                Language.Java -> JavaTestCompiler(javaHomePath, libraryPaths, junitLibraryPaths)
                Language.Kotlin -> JavaTestCompiler(javaHomePath, libraryPaths, junitLibraryPaths)
            }
        }

        // Utility function to find Kotlin SDK
        private fun findKotlinHomeDirectory(project: Project): String {
            val projectSdk = ProjectRootManager.getInstance(project).projectSdk
            if (projectSdk != null && projectSdk.sdkType.name.contains("Kotlin", ignoreCase = true)) {
                return projectSdk.homePath ?: throw RuntimeException("Kotlin SDK home directory not found.")
            }
            // If the project's SDK is not Kotlin, we look through all defined SDKs
            val allSdks = ProjectJdkTable.getInstance().allJdks
            val kotlinSdk = allSdks.firstOrNull { it.sdkType.name.contains("Kotlin", ignoreCase = true) }
                ?: throw RuntimeException("Kotlin SDK not configured for the project.")

            val kotlinHomeDirectoryPath =
                kotlinSdk.homePath ?: throw RuntimeException("Kotlin SDK home directory not found.")
            return kotlinHomeDirectoryPath

        }

        private fun findJavaHomeDirectory(project: Project): String {
            return ProjectRootManager.getInstance(project).projectSdk?.homeDirectory?.path
                ?: throw RuntimeException("Java SDK not configured for the project.")
        }
    }
}

