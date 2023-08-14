package org.jetbrains.research.testspark

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

const val LABELS_BUNDLE = "defaults.Labels"

/**
 * Loads the label texts from `defaults/Labels.properties` file in the `recourses` directory.
 */
object TestSparkLabelsBundle : DynamicBundle(LABELS_BUNDLE) {

    /**
     * Gets the requested default value.
     */
    @Nls
    fun defaultValue(@PropertyKey(resourceBundle = LABELS_BUNDLE) key: String): String = getMessage(key)
}
