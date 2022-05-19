package nl.tudelft.ewi.se.ciselab.testgenie

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

const val DEFAULTS_BUNDLE = "defaults.TestGenie"

/**
 * Loads the default values from `defaults.TestGenie` file in the `recourses` directory.
 */
object TestGenieDefaultsBundle : DynamicBundle(DEFAULTS_BUNDLE) {
    @Nls
    fun defaultValue(@PropertyKey(resourceBundle = DEFAULTS_BUNDLE) key: String): String = getMessage(key)
}
