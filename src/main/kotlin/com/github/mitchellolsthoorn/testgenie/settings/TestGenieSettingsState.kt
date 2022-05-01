package com.github.mitchellolsthoorn.testgenie.settings

/**
 * This class is the actual data class that stores the values of the Settings entries.
 */
data class TestGenieSettingsState
    constructor(var userId: String = "Amogus", var ideaStatus: Boolean = false)