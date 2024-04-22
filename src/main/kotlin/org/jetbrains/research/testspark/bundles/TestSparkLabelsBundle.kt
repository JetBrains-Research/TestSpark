package org.jetbrains.research.testspark.bundles

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

/**
 * Loads the label texts from `defaults/Labels.properties` file in the `recourses` directory.
 */
object TestSparkLabelsBundle : DynamicBundle(BundlePaths.LABELS) {

    /**
     * Gets the requested default value.
     */
    @Nls
    fun defaultValue(@PropertyKey(resourceBundle = BundlePaths.LABELS) key: String): String = getMessage(key)
}
