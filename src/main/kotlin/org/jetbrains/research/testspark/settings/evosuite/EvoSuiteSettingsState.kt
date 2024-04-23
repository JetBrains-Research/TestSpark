package org.jetbrains.research.testspark.settings.evosuite

import org.jetbrains.research.testspark.bundles.DefaultsBundle
import org.jetbrains.research.testspark.data.evosuite.ContentDigestAlgorithm

/**
 * This class is the actual data class that stores the values of the EvoSuite Settings entries.
 */
data class EvoSuiteSettingsState(
    var javaPath: String = DefaultSettingsEvoSuiteState.javaPath,
    var sandbox: Boolean = DefaultSettingsEvoSuiteState.sandbox,
    var assertions: Boolean = DefaultSettingsEvoSuiteState.assertions,
    var seed: String = DefaultSettingsEvoSuiteState.seed,
    var algorithm: ContentDigestAlgorithm = DefaultSettingsEvoSuiteState.algorithm,
    var configurationId: String = DefaultSettingsEvoSuiteState.configurationId,
    var clientOnThread: Boolean = DefaultSettingsEvoSuiteState.clientOnThread,
    var junitCheck: Boolean = DefaultSettingsEvoSuiteState.junitCheck,
    var criterionLine: Boolean = DefaultSettingsEvoSuiteState.criterionLine,
    var criterionBranch: Boolean = DefaultSettingsEvoSuiteState.criterionBranch,
    var criterionException: Boolean = DefaultSettingsEvoSuiteState.criterionException,
    var criterionWeakMutation: Boolean = DefaultSettingsEvoSuiteState.criterionWeakMutation,
    var criterionOutput: Boolean = DefaultSettingsEvoSuiteState.criterionOutput,
    var criterionMethod: Boolean = DefaultSettingsEvoSuiteState.criterionMethod,
    var criterionMethodNoException: Boolean = DefaultSettingsEvoSuiteState.criterionMethodNoException,
    var criterionCBranch: Boolean = DefaultSettingsEvoSuiteState.criterionCBranch,
    var minimize: Boolean = DefaultSettingsEvoSuiteState.minimize,
    var evosuiteSetupCheckBoxSelected: Boolean = DefaultSettingsEvoSuiteState.evosuiteSetupCheckBoxSelected,
    var evosuitePort: String = DefaultSettingsEvoSuiteState.evosuitePort,
) {

    /**
     * Default values of SettingsEvoSuiteState.
     */
    object DefaultSettingsEvoSuiteState {
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
