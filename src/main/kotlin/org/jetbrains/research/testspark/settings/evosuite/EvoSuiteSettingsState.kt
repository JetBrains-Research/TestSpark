package org.jetbrains.research.testspark.settings.evosuite

import org.jetbrains.research.testspark.bundles.evosuite.EvoSuiteDefaultsBundle
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
        var javaPath: String = EvoSuiteDefaultsBundle.get("javaPath")
        val sandbox: Boolean = EvoSuiteDefaultsBundle.get("sandbox").toBoolean()
        val assertions: Boolean = EvoSuiteDefaultsBundle.get("assertions").toBoolean()
        val seed: String = EvoSuiteDefaultsBundle.get("seed")
        val junitCheck: Boolean = EvoSuiteDefaultsBundle.get("junitCheck").toBoolean()
        val minimize: Boolean = EvoSuiteDefaultsBundle.get("minimize").toBoolean()
        val algorithm: ContentDigestAlgorithm = ContentDigestAlgorithm.DYNAMOSA
        val configurationId: String = EvoSuiteDefaultsBundle.get("configurationId")
        val clientOnThread: Boolean = EvoSuiteDefaultsBundle.get("clientOnThread").toBoolean()
        val criterionLine: Boolean = EvoSuiteDefaultsBundle.get("criterionLine").toBoolean()
        val criterionBranch: Boolean = EvoSuiteDefaultsBundle.get("criterionBranch").toBoolean()
        val criterionException: Boolean = EvoSuiteDefaultsBundle.get("criterionException").toBoolean()
        val criterionWeakMutation: Boolean = EvoSuiteDefaultsBundle.get("criterionWeakMutation").toBoolean()
        val criterionOutput: Boolean = EvoSuiteDefaultsBundle.get("criterionOutput").toBoolean()
        val criterionMethod: Boolean = EvoSuiteDefaultsBundle.get("criterionMethod").toBoolean()
        val criterionMethodNoException: Boolean = EvoSuiteDefaultsBundle.get("criterionMethodNoException").toBoolean()
        val criterionCBranch: Boolean = EvoSuiteDefaultsBundle.get("criterionCBranch").toBoolean()
        val evosuiteSetupCheckBoxSelected: Boolean = EvoSuiteDefaultsBundle.get("evosuiteSetup").toBoolean()
        val evosuitePort: String = EvoSuiteDefaultsBundle.get("evosuitePort")
    }
}
