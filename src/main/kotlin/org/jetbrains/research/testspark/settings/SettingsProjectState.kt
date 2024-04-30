package org.jetbrains.research.testspark.settings

import org.jetbrains.research.testspark.bundles.plugin.PluginDefaultsBundle

/**
 * This class is the actual data class that stores the values of the Plugin Settings entries.
 */
data class SettingsProjectState(
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
        val showCoverageCheckboxSelected: Boolean = PluginDefaultsBundle.get("showCoverageCheckboxSelected").toBoolean()
        val colorRed: Int = PluginDefaultsBundle.get("colorRed").toInt()
        val colorGreen: Int = PluginDefaultsBundle.get("colorGreen").toInt()
        val colorBlue: Int = PluginDefaultsBundle.get("colorBlue").toInt()
        const val buildPath: String = ""
        const val buildCommand: String = ""
    }
}
