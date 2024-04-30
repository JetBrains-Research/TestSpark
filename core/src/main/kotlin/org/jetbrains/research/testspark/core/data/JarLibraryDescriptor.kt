package org.jetbrains.research.testspark.core.data

/**
 * Represents a descriptor for a JAR library.
 *
 * @property name The name of the library, it must contain the .jar extension as well.
 * @property downloadUrl The URL to download the library.
 */
data class JarLibraryDescriptor(
    val name: String,
    val downloadUrl: String,
)
