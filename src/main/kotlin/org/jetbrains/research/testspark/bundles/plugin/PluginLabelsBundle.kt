package org.jetbrains.research.testspark.bundles.plugin

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

/**
 * Loads the `recourses` directory.
 */
object PluginLabelsBundle : DynamicBundle(PluginBundlePaths.labels) {

    /**
     * Gets the requested default value.
     */
    @Nls
    fun defaultValue(@PropertyKey(resourceBundle = PluginBundlePaths.labels) key: String): String = getMessage(key)
}
