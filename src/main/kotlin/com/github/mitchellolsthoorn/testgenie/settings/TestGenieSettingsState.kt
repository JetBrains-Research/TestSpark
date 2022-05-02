package com.github.mitchellolsthoorn.testgenie.settings

/**
 * This class is the actual data class that stores the values of the Settings entries.
 */
data class TestGenieSettingsState
    constructor(var globalTimeout: String = "60", var showCoverage: Boolean = false)