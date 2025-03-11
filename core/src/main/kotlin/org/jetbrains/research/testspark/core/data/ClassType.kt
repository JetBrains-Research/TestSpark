package org.jetbrains.research.testspark.core.data

/**
 * Enumeration representing different types of classes.
 *
 * @param representation The string representation of the class type.
 */
enum class ClassType(
    val representation: String,
) {
    INTERFACE("interface"),
    ABSTRACT_CLASS("abstract class"),
    CLASS("class"),
    DATA_CLASS("data class"),
    INLINE_VALUE_CLASS("inline value class"),
    OBJECT("object"),
}
