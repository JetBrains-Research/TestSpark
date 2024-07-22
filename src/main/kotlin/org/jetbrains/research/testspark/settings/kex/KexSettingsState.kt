package org.jetbrains.research.testspark.settings.kex

import org.jetbrains.research.testspark.bundles.kex.KexDefaultsBundle

/**
 * This class is the actual data class that stores the values of the Kex Settings entries.
 */
data class KexSettingsState(var kexVersion: String = DefaultKexSettingsState.kexVersion) {

    /**
     * Default values of SettingsEvoSuiteState.
     */
    object DefaultKexSettingsState {
        val kexVersion: String = KexDefaultsBundle.get("kexVersion")
    }
}