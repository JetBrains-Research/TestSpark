package org.jetbrains.research.testspark.settings

/**
 * This class is the actual data class that stores the values of the Plugin Settings entries.
 */
data class SettingsProjectState(
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
        val javaPath: String = org.jetbrains.research.testspark.TestSparkDefaultsBundle.defaultValue("javaPath")
        val colorRed: Int = org.jetbrains.research.testspark.TestSparkDefaultsBundle.defaultValue("colorRed").toInt()
        val colorGreen: Int = org.jetbrains.research.testspark.TestSparkDefaultsBundle.defaultValue("colorGreen").toInt()
        val colorBlue: Int = org.jetbrains.research.testspark.TestSparkDefaultsBundle.defaultValue("colorBlue").toInt()
        const val buildPath: String = ""
        const val buildCommand: String = ""
        val telemetryEnabled: Boolean = org.jetbrains.research.testspark.TestSparkDefaultsBundle.defaultValue("telemetryEnabled").toBoolean()
        val telemetryPath: String = System.getProperty("user.home")
    }
}
