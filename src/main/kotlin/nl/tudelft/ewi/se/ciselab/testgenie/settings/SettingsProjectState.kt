package nl.tudelft.ewi.se.ciselab.testgenie.settings

data class SettingsProjectState(
    var javaPath: String = SettingsApplicationState.DefaultSettingsState.javaPath,
    var colorRed: Int = SettingsApplicationState.DefaultSettingsState.colorRed,
    var colorGreen: Int = SettingsApplicationState.DefaultSettingsState.colorGreen,
    var colorBlue: Int = SettingsApplicationState.DefaultSettingsState.colorBlue,
    var buildPath: String = SettingsApplicationState.DefaultSettingsState.buildPath,
    var buildCommand: String = SettingsApplicationState.DefaultSettingsState.buildCommand,
    var telemetryEnabled: Boolean = SettingsApplicationState.DefaultSettingsState.telemetryEnabled,
    var telemetryPath: String = SettingsApplicationState.DefaultSettingsState.telemetryPath
)
