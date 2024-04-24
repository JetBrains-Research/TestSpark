package org.jetbrains.research.testspark.settings.plugin

import org.jetbrains.research.testspark.bundles.plugin.PluginDefaultsBundle

/**
 * This class is the actual data class that stores the values of the Plugin Settings entries.
 */
data class PluginSettingsState(
    var showCoverageCheckboxSelected: Boolean = DefaultPluginSettingsState.showCoverageCheckboxSelected,
    var colorRed: Int = DefaultPluginSettingsState.colorRed,
    var colorGreen: Int = DefaultPluginSettingsState.colorGreen,
    var colorBlue: Int = DefaultPluginSettingsState.colorBlue,
    var buildPath: String = DefaultPluginSettingsState.buildPath,
    var buildCommand: String = DefaultPluginSettingsState.buildCommand,
) {

    /**
     * Default values of SettingsProjectState.
     */
    object DefaultPluginSettingsState {
        val showCoverageCheckboxSelected: Boolean = PluginDefaultsBundle.defaultValue("showCoverageCheckboxSelected").toBoolean()
        val colorRed: Int = PluginDefaultsBundle.defaultValue("colorRed").toInt()
        val colorGreen: Int = PluginDefaultsBundle.defaultValue("colorGreen").toInt()
        val colorBlue: Int = PluginDefaultsBundle.defaultValue("colorBlue").toInt()
        val buildPath: String = PluginDefaultsBundle.defaultValue("buildPath")
        val buildCommand: String = PluginDefaultsBundle.defaultValue("buildCommand")
    }
}
