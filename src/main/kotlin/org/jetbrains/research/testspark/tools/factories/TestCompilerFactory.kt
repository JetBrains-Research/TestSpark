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
        javaHomeDirectory: String? = null,
    ): TestCompiler {
        val libraryPaths = LibraryPathsProvider.getTestCompilationLibraryPaths()
        val junitLibraryPaths = LibraryPathsProvider.getJUnitLibraryPaths(junitVersion)

        // TODO add the warning window that for Java we always need the javaHomeDirectoryPath
        return when (language) {
            SupportedLanguage.Java -> {
                val javaSDKHomePath = findJavaSDKHomePath(javaHomeDirectory, project)
                JavaTestCompiler(libraryPaths, junitLibraryPaths, javaSDKHomePath)
            }
            SupportedLanguage.Kotlin -> {
                // Kotlinc relies on java to compile kotlin files.
                val javaSDKHomePath = findJavaSDKHomePath(javaHomeDirectory, project)
                // kotlinc should be under `[kotlinSDKHomeDirectory]/bin/kotlinc`
                val kotlinSDKHomeDirectory = KotlinPluginLayout.kotlinc.absolutePath
                KotlinTestCompiler(libraryPaths, junitLibraryPaths, kotlinSDKHomeDirectory, javaSDKHomePath)
            }
        }
    }

    /**
     * Finds the home path of the Java SDK.
     *
     * @param javaHomeDirectory The directory where Java SDK is installed. If null, the project's configured SDK path is used.
     * @param project The project for which the Java SDK home path is being determined.
     * @return The home path of the Java SDK.
     * @throws JavaSDKMissingException If no Java SDK is configured for the project.
     */
    private fun findJavaSDKHomePath(
        javaHomeDirectory: String?,
        project: Project,
    ): String {
        val javaSDKHomePath =
            javaHomeDirectory
                ?: ProjectRootManager
                    .getInstance(project)
                    .projectSdk
                    ?.homeDirectory
                    ?.path

        if (javaSDKHomePath == null) {
            throw JavaSDKMissingException(LLMMessagesBundle.get("javaSdkNotConfigured"))
        }
        return javaSDKHomePath
    }
}
