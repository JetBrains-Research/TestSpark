package org.jetbrains.research.testspark.tools.error.message

import org.jetbrains.research.testspark.bundles.llm.LLMMessagesBundle
import org.jetbrains.research.testspark.bundles.plugin.PluginMessagesBundle
import org.jetbrains.research.testspark.core.exception.ClassFileNotFoundException
import org.jetbrains.research.testspark.core.exception.CompilerException
import org.jetbrains.research.testspark.core.exception.JavaCompilerNotFoundException
import org.jetbrains.research.testspark.core.exception.JavaSDKMissingException
import org.jetbrains.research.testspark.core.exception.KotlinCompilerNotFoundException

val CompilerException.compilerExceptionMessage: String?
    get() = when(this) {
        is ClassFileNotFoundException ->
            PluginMessagesBundle.get("classFileNotFoundErrorMessage").format(classFilePath, filePath)
        is JavaSDKMissingException -> LLMMessagesBundle.get("javaSdkNotConfigured")
        is JavaCompilerNotFoundException ->
            PluginMessagesBundle.get("compilerNotFoundErrorMessage").format("Java", javaHomeDirectoryPath)
        is KotlinCompilerNotFoundException ->
            PluginMessagesBundle.get("compilerNotFoundErrorMessage").format("Kotlin", kotlinSdkHomeDirectory)
    }

