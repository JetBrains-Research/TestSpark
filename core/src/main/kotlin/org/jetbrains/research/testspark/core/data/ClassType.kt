package org.jetbrains.research.testspark.core.data

/**
 * Represents the different types of classes in a codebase.
 */
enum class ClassType(val representation: String) {
    INTERFACE("interface"),
    ABSTRACT_CLASS("abstract class"),
    CLASS("class"),
}
