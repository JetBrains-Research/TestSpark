package org.jetbrains.research.testspark.settings.common

import org.jetbrains.research.testspark.bundles.DefaultsBundle

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
        val showCoverageCheckboxSelected: Boolean = DefaultsBundle.defaultValue("showCoverageCheckboxSelected").toBoolean()
        val colorRed: Int = DefaultsBundle.defaultValue("colorRed").toInt()
        val colorGreen: Int = DefaultsBundle.defaultValue("colorGreen").toInt()
        val colorBlue: Int = DefaultsBundle.defaultValue("colorBlue").toInt()
        val buildPath: String = DefaultsBundle.defaultValue("buildPath")
        val buildCommand: String = DefaultsBundle.defaultValue("buildCommand")
    }
}
