package org.jetbrains.research.testspark.settings.evosuite

import org.jetbrains.research.testspark.bundles.DefaultsBundle
import org.jetbrains.research.testspark.data.evosuite.ContentDigestAlgorithm

/**
 * This class is the actual data class that stores the values of the EvoSuite Settings entries.
 */
data class EvoSuiteSettingsState(
    var javaPath: String = DefaultEvoSuiteSettingsState.javaPath,
    var sandbox: Boolean = DefaultEvoSuiteSettingsState.sandbox,
    var assertions: Boolean = DefaultEvoSuiteSettingsState.assertions,
    var seed: String = DefaultEvoSuiteSettingsState.seed,
    var algorithm: ContentDigestAlgorithm = DefaultEvoSuiteSettingsState.algorithm,
    var configurationId: String = DefaultEvoSuiteSettingsState.configurationId,
    var clientOnThread: Boolean = DefaultEvoSuiteSettingsState.clientOnThread,
    var junitCheck: Boolean = DefaultEvoSuiteSettingsState.junitCheck,
    var criterionLine: Boolean = DefaultEvoSuiteSettingsState.criterionLine,
    var criterionBranch: Boolean = DefaultEvoSuiteSettingsState.criterionBranch,
    var criterionException: Boolean = DefaultEvoSuiteSettingsState.criterionException,
    var criterionWeakMutation: Boolean = DefaultEvoSuiteSettingsState.criterionWeakMutation,
    var criterionOutput: Boolean = DefaultEvoSuiteSettingsState.criterionOutput,
    var criterionMethod: Boolean = DefaultEvoSuiteSettingsState.criterionMethod,
    var criterionMethodNoException: Boolean = DefaultEvoSuiteSettingsState.criterionMethodNoException,
    var criterionCBranch: Boolean = DefaultEvoSuiteSettingsState.criterionCBranch,
    var minimize: Boolean = DefaultEvoSuiteSettingsState.minimize,
    var evosuiteSetupCheckBoxSelected: Boolean = DefaultEvoSuiteSettingsState.evosuiteSetupCheckBoxSelected,
    var evosuitePort: String = DefaultEvoSuiteSettingsState.evosuitePort,
) {

    /**
     * Default values of SettingsEvoSuiteState.
     */
    object DefaultEvoSuiteSettingsState {
        var javaPath: String = DefaultsBundle.defaultValue("javaPath")
        val sandbox: Boolean = DefaultsBundle.defaultValue("sandbox").toBoolean()
        val assertions: Boolean = DefaultsBundle.defaultValue("assertions").toBoolean()
        val seed: String = DefaultsBundle.defaultValue("seed")
        val junitCheck: Boolean = DefaultsBundle.defaultValue("junitCheck").toBoolean()
        val minimize: Boolean = DefaultsBundle.defaultValue("minimize").toBoolean()
        val algorithm: ContentDigestAlgorithm = ContentDigestAlgorithm.DYNAMOSA
        val configurationId: String = DefaultsBundle.defaultValue("configurationId")
        val clientOnThread: Boolean = DefaultsBundle.defaultValue("clientOnThread").toBoolean()
        val criterionLine: Boolean = DefaultsBundle.defaultValue("criterionLine").toBoolean()
        val criterionBranch: Boolean = DefaultsBundle.defaultValue("criterionBranch").toBoolean()
        val criterionException: Boolean = DefaultsBundle.defaultValue("criterionException").toBoolean()
        val criterionWeakMutation: Boolean = DefaultsBundle.defaultValue("criterionWeakMutation").toBoolean()
        val criterionOutput: Boolean = DefaultsBundle.defaultValue("criterionOutput").toBoolean()
        val criterionMethod: Boolean = DefaultsBundle.defaultValue("criterionMethod").toBoolean()
        val criterionMethodNoException: Boolean = DefaultsBundle.defaultValue("criterionMethodNoException").toBoolean()
        val criterionCBranch: Boolean = DefaultsBundle.defaultValue("criterionCBranch").toBoolean()
        val openAIName: String = DefaultsBundle.defaultValue("openAIName")
        val evosuiteSetupCheckBoxSelected: Boolean = DefaultsBundle.defaultValue("evosuiteSetup").toBoolean()
        val evosuitePort: String = DefaultsBundle.defaultValue("evosuitePort")
    }
}
