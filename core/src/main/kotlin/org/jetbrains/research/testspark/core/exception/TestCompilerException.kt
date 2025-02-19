package org.jetbrains.research.testspark.core.exception

sealed class TestCompilerException() : TestSparkException()

/**
 * Custom exception to indicate that the Kotlin compiler was not found.
 *
 * @param message A descriptive message explaining the error.
 */

class KotlinCompilerNotFoundException(val kotlinSdkHomeDirectory: String) : TestCompilerException()

/**
 * Custom exception to indicate that the Java compiler was not found.
 *
 * @param message A descriptive message explaining the error.
 */
class JavaCompilerNotFoundException() : TestCompilerException()

/**
 * Represents an exception thrown when a required Java SDK is missing in the system.
 *
 * @param message A descriptive message explaining the specific error that led to this exception.
 */
class JavaSDKMissingException() : TestCompilerException()

/**
 * Represents an exception thrown when a class file could not be found in the same path after the code compilation.
 *
 * @param message A descriptive message explaining the error
 */
class ClassFileNotFoundException() : TestCompilerException()
