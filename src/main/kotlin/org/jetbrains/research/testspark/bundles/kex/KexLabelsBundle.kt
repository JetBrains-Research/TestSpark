package org.jetbrains.research.testspark.bundles.kex

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

/**
 * Loads the `resources` directory.
 */
object KexLabelsBundle : DynamicBundle(KexBundlePaths.LABELS) {
    /**
     * Gets the requested default value.
     */
    @Nls
    fun get(
        @PropertyKey(resourceBundle = KexBundlePaths.LABELS) key: String,
    ): String = getMessage(key)
}
