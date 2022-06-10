package nl.tudelft.ewi.se.ciselab.testgenie

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

const val LABELS_BUNDLE = "defaults.Labels"

/**
 * Loads the default values from `defaults.TestGenie` file in the `recourses` directory.
 */
object TestGenieLabelsBundle : DynamicBundle(LABELS_BUNDLE) {

    /**
     * Gets the requested default value.
     */
    @Nls
    fun defaultValue(@PropertyKey(resourceBundle = LABELS_BUNDLE) key: String): String = getMessage(key)
}
