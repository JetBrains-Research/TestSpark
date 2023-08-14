package org.jetbrains.research.testspark

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

const val BUNDLE = "messages.TestSpark"

/**
 * Loads the EvoSuite messages from `messages/TestSpark.properties` file in the `resources` directory.
 */
object TestSparkBundle : DynamicBundle(org.jetbrains.research.testspark.BUNDLE) {

    /**
     * Gets the requested message.
     */
    @Nls
    fun message(@PropertyKey(resourceBundle = org.jetbrains.research.testspark.BUNDLE) key: String, vararg params: Any): String =
        getMessage(key, *params)
}
