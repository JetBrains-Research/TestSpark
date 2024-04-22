package org.jetbrains.research.testspark.bundles

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

/**
 * Loads the tooltip texts from `defaults/Tooltips.properties` file in the `resources` directory.
 */
object TestSparkToolTipsBundle : DynamicBundle(BundlePaths.TOOLTIPS) {

    /**
     * Gets the requested default value.
     */
    @Nls
    fun defaultValue(@PropertyKey(resourceBundle = BundlePaths.TOOLTIPS) key: String): String = getMessage(key)
}
