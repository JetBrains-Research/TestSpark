package org.jetbrains.research.testspark.core.exception

sealed class GeneralException() : TestSparkException()

/**
 * Custom exception to indicate that the Kotlin compiler was not found.
 *
 * @param message A descriptive message explaining the error.
 */

class KotlinCompilerNotFoundException(val kotlinSdkHomeDirectory: String) : GeneralException()

/**
 * Custom exception to indicate that the Java compiler was not found.
 *
 * @param message A descriptive message explaining the error.
 */
class JavaCompilerNotFoundException() : GeneralException()

/**
 * Represents an exception thrown when a required Java SDK is missing in the system.
 *
 * @param message A descriptive message explaining the specific error that led to this exception.
 */
class JavaSDKMissingException() : GeneralException()

/**
 * Represents an exception thrown when a class file could not be found in the same path after the code compilation.
 *
 * @param message A descriptive message explaining the error
 */
class ClassFileNotFoundException() : GeneralException()
