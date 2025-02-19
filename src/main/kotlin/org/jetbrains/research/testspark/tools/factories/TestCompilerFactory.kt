package org.jetbrains.research.testspark.tools.factories

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import org.jetbrains.kotlin.idea.compiler.configuration.KotlinPluginLayout
import org.jetbrains.research.testspark.bundles.llm.LLMMessagesBundle
import org.jetbrains.research.testspark.core.data.JUnitVersion
import org.jetbrains.research.testspark.core.exception.JavaSDKMissingException
import org.jetbrains.research.testspark.core.test.SupportedLanguage
import org.jetbrains.research.testspark.core.test.TestCompiler
import org.jetbrains.research.testspark.core.test.java.JavaTestCompiler
import org.jetbrains.research.testspark.core.test.kotlin.KotlinTestCompiler
import org.jetbrains.research.testspark.tools.LibraryPathsProvider

object TestCompilerFactory {
    fun create(
        project: Project,
        junitVersion: JUnitVersion,
        language: SupportedLanguage,
        javaSDKHomePath: String = findJavaSDKHomePath(project)
    ): TestCompiler {
        val libraryPaths = LibraryPathsProvider.getTestCompilationLibraryPaths()
        val junitLibraryPaths = LibraryPathsProvider.getJUnitLibraryPaths(junitVersion)

        return when (language) {
            SupportedLanguage.Java -> {
                JavaTestCompiler(libraryPaths, junitLibraryPaths, javaSDKHomePath)
            }
            SupportedLanguage.Kotlin -> {
                // Kotlinc relies on java to compile kotlin files.
                // kotlinc should be under `[kotlinSDKHomeDirectory]/bin/kotlinc`
                val kotlinSDKHomeDirectory = KotlinPluginLayout.kotlinc.absolutePath
                KotlinTestCompiler(libraryPaths, junitLibraryPaths, kotlinSDKHomeDirectory, javaSDKHomePath)
            }
        }
    }

    /**
     * Finds the home path of the Java SDK.
     *
     * @param project The project for which the Java SDK home path is being determined.
     * @return The home path of the Java SDK.
     * @throws JavaSDKMissingException If no Java SDK is configured for the project.
     */
    private fun findJavaSDKHomePath(project: Project): String {
        return ProjectRootManager
            .getInstance(project)
            .projectSdk
            ?.homeDirectory
            ?.path
            ?: (throw JavaSDKMissingException())
    }
}
