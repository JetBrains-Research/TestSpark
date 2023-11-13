package org.jetbrains.research.testspark.settings

import org.jetbrains.research.testspark.TestSparkDefaultsBundle

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
    var telemetryEnabled: Boolean = DefaultSettingsPluginState.telemetryEnabled,
    var telemetryPath: String = DefaultSettingsPluginState.telemetryPath,
) {
    /**
     * Default values of SettingsProjectState.
     */
    object DefaultSettingsPluginState {
        val showCoverageCheckboxSelected: Boolean = TestSparkDefaultsBundle.defaultValue("showCoverageCheckboxSelected").toBoolean()
        val colorRed: Int = TestSparkDefaultsBundle.defaultValue("colorRed").toInt()
        val colorGreen: Int = TestSparkDefaultsBundle.defaultValue("colorGreen").toInt()
        val colorBlue: Int = TestSparkDefaultsBundle.defaultValue("colorBlue").toInt()
        const val buildPath: String = ""
        const val buildCommand: String = ""
        val telemetryEnabled: Boolean = TestSparkDefaultsBundle.defaultValue("telemetryEnabled").toBoolean()
        val telemetryPath: String = System.getProperty("user.home")
    }
}
