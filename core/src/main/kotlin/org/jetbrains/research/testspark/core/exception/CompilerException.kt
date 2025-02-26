package org.jetbrains.research.testspark.core.exception

import org.jetbrains.research.testspark.core.data.TestSparkModule

sealed class CompilerException(cause: Throwable? = null) : TestSparkException(
    module = TestSparkModule.Compiler,
    cause = cause,
)

class KotlinCompilerNotFoundException(val kotlinSdkHomeDirectory: String) : CompilerException()

/**
 * Custom exception to indicate that the Java compiler was not found.
 *
 * @param message A descriptive message explaining the error.
 */
class JavaCompilerNotFoundException(val javaHomeDirectoryPath: String) : CompilerException()

/**
 * Represents an exception thrown when a required Java SDK is missing in the system.
 *
 * @param message A descriptive message explaining the specific error that led to this exception.
 */
class JavaSDKMissingException() : CompilerException()

/**
 * Represents an exception thrown when a class file could not be found in the same path after the code compilation.
 *
 * @param message A descriptive message explaining the error
 */
class ClassFileNotFoundException(
    val classFilePath: String,
    val filePath: String,
) : CompilerException()
