package org.jetbrains.research.testspark.settings.common

import org.jetbrains.research.testspark.bundles.DefaultsBundle

/**
 * This class is the actual data class that stores the values of the Plugin Settings entries.
 */
data class PluginSettingsState(
    var showCoverageCheckboxSelected: Boolean = DefaultSettingsPluginState.showCoverageCheckboxSelected,
    var colorRed: Int = DefaultSettingsPluginState.colorRed,
    var colorGreen: Int = DefaultSettingsPluginState.colorGreen,
    var colorBlue: Int = DefaultSettingsPluginState.colorBlue,
    var buildPath: String = DefaultSettingsPluginState.buildPath,
    var buildCommand: String = DefaultSettingsPluginState.buildCommand,
) {

    /**
     * Default values of SettingsProjectState.
     */
    object DefaultSettingsPluginState {
        val showCoverageCheckboxSelected: Boolean = DefaultsBundle.defaultValue("showCoverageCheckboxSelected").toBoolean()
        val colorRed: Int = DefaultsBundle.defaultValue("colorRed").toInt()
        val colorGreen: Int = DefaultsBundle.defaultValue("colorGreen").toInt()
        val colorBlue: Int = DefaultsBundle.defaultValue("colorBlue").toInt()
        const val buildPath: String = ""
        const val buildCommand: String = ""
    }
}
