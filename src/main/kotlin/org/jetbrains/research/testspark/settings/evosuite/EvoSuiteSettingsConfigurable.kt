package org.jetbrains.research.testspark.settings.evosuite

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import org.jetbrains.research.testspark.bundles.evosuite.EvoSuiteMessagesBundle
import org.jetbrains.research.testspark.services.EvoSuiteSettingsService
import org.jetbrains.research.testspark.settings.template.SettingsConfigurable
import javax.swing.JComponent

/**
 * This class allows to configure some EvoSuite settings via the EvoSuite page in the Settings dialog,
 *   observes the changes and manages the UI and state.
 * It interacts with the SettingsEvoSuiteComponent, TestSparkSettingsService and TestSparkSettingsState.
 * It provides controller functionality for the TestSparkSettingsState.
 */
class EvoSuiteSettingsConfigurable(private val project: Project) : SettingsConfigurable {
    private val evoSuiteSettingsState: EvoSuiteSettingsState
        get() = project.getService(EvoSuiteSettingsService::class.java).state

    var settingsComponent: EvoSuiteSettingsComponent? = null

    /**
     * Creates a settings component that holds the panel with the settings entries, and returns this panel
     *
     * @return the panel used for displaying settings
     */
    override fun createComponent(): JComponent? {
        settingsComponent = EvoSuiteSettingsComponent()
        return settingsComponent!!.panel
    }

    /**
     * Sets the stored state values to the corresponding UI components. This method is called immediately after `createComponent` method.
     */
    override fun reset() {
        settingsComponent!!.javaPath = evoSuiteSettingsState.javaPath
        settingsComponent!!.sandbox = evoSuiteSettingsState.sandbox
        settingsComponent!!.assertions = evoSuiteSettingsState.assertions
        settingsComponent!!.seed = evoSuiteSettingsState.seed
        settingsComponent!!.evosuitePort = evoSuiteSettingsState.evosuitePort
        settingsComponent!!.algorithm = evoSuiteSettingsState.algorithm
        settingsComponent!!.evosuiteSetupCheckBoxSelected = evoSuiteSettingsState.evosuiteSetupCheckBoxSelected
        settingsComponent!!.configurationId = evoSuiteSettingsState.configurationId
        settingsComponent!!.clientOnThread = evoSuiteSettingsState.clientOnThread
        settingsComponent!!.junitCheck = evoSuiteSettingsState.junitCheck
        settingsComponent!!.criterionLine = evoSuiteSettingsState.criterionLine
        settingsComponent!!.criterionBranch = evoSuiteSettingsState.criterionBranch
        settingsComponent!!.criterionException = evoSuiteSettingsState.criterionException
        settingsComponent!!.criterionWeakMutation = evoSuiteSettingsState.criterionWeakMutation
        settingsComponent!!.criterionOutput = evoSuiteSettingsState.criterionOutput
        settingsComponent!!.criterionMethod = evoSuiteSettingsState.criterionMethod
        settingsComponent!!.criterionMethodNoException = evoSuiteSettingsState.criterionMethodNoException
        settingsComponent!!.criterionCBranch = evoSuiteSettingsState.criterionCBranch
        settingsComponent!!.minimize = evoSuiteSettingsState.minimize
    }

    /**
     * Checks if the values of the entries in the settings state are different from the persisted values of these entries.
     *
     * @return whether any setting has been modified
     */
    override fun isModified(): Boolean {
        var modified: Boolean = settingsComponent!!.sandbox != evoSuiteSettingsState.sandbox
        modified = modified or (settingsComponent!!.javaPath != evoSuiteSettingsState.javaPath)
        modified = modified or (settingsComponent!!.assertions != evoSuiteSettingsState.assertions)
        modified = modified or (settingsComponent!!.seed != evoSuiteSettingsState.seed)
        modified = modified or (settingsComponent!!.evosuitePort != evoSuiteSettingsState.evosuitePort)
        modified = modified or (settingsComponent!!.algorithm != evoSuiteSettingsState.algorithm)
        modified = modified or (settingsComponent!!.evosuiteSetupCheckBoxSelected != evoSuiteSettingsState.evosuiteSetupCheckBoxSelected)
        modified = modified or (settingsComponent!!.configurationId != evoSuiteSettingsState.configurationId)
        modified = modified or (settingsComponent!!.clientOnThread != evoSuiteSettingsState.clientOnThread)
        modified = modified or (settingsComponent!!.junitCheck != evoSuiteSettingsState.junitCheck)
        modified = modified or (settingsComponent!!.criterionLine != evoSuiteSettingsState.criterionLine)
        modified = modified or (settingsComponent!!.criterionBranch != evoSuiteSettingsState.criterionBranch)
        modified = modified or (settingsComponent!!.criterionException != evoSuiteSettingsState.criterionException)
        modified = modified or (settingsComponent!!.criterionWeakMutation != evoSuiteSettingsState.criterionWeakMutation)
        modified = modified or (settingsComponent!!.criterionOutput != evoSuiteSettingsState.criterionOutput)
        modified = modified or (settingsComponent!!.criterionMethod != evoSuiteSettingsState.criterionMethod)
        modified = modified or (settingsComponent!!.criterionMethodNoException != evoSuiteSettingsState.criterionMethodNoException)
        modified = modified or (settingsComponent!!.criterionCBranch != evoSuiteSettingsState.criterionCBranch)
        modified = modified or (settingsComponent!!.minimize != evoSuiteSettingsState.minimize)
        return modified
    }

    /**
     * Persists the modified state after a user hit Apply button.
     */
    override fun apply() {
        evoSuiteSettingsState.javaPath = settingsComponent!!.javaPath
        evoSuiteSettingsState.sandbox = settingsComponent!!.sandbox
        evoSuiteSettingsState.assertions = settingsComponent!!.assertions
        evoSuiteSettingsState.algorithm = settingsComponent!!.algorithm
        evoSuiteSettingsState.evosuiteSetupCheckBoxSelected = settingsComponent!!.evosuiteSetupCheckBoxSelected
        evoSuiteSettingsState.configurationId = settingsComponent!!.configurationId
        evoSuiteSettingsState.clientOnThread = settingsComponent!!.clientOnThread
        evoSuiteSettingsState.junitCheck = settingsComponent!!.junitCheck
        evoSuiteSettingsState.criterionLine = settingsComponent!!.criterionLine
        evoSuiteSettingsState.criterionBranch = settingsComponent!!.criterionBranch
        evoSuiteSettingsState.criterionException = settingsComponent!!.criterionException
        evoSuiteSettingsState.criterionWeakMutation = settingsComponent!!.criterionWeakMutation
        evoSuiteSettingsState.criterionOutput = settingsComponent!!.criterionOutput
        evoSuiteSettingsState.criterionMethod = settingsComponent!!.criterionMethod
        evoSuiteSettingsState.criterionMethodNoException = settingsComponent!!.criterionMethodNoException
        evoSuiteSettingsState.criterionCBranch = settingsComponent!!.criterionCBranch
        evoSuiteSettingsState.minimize = settingsComponent!!.minimize

        val seed = settingsComponent!!.seed.toLongOrNull()
        if (settingsComponent!!.seed != "" && seed == null) {
            Messages.showErrorDialog(
                EvoSuiteMessagesBundle.message("seedParameterMessage"),
                EvoSuiteMessagesBundle.message("seedParameterTitle"),
            )
        } else {
            evoSuiteSettingsState.seed = settingsComponent!!.seed
        }

        val evosuitePort = settingsComponent!!.evosuitePort.toIntOrNull()
        if (evosuitePort != null && (evosuitePort < 1024 || evosuitePort > 65535)) {
            Messages.showErrorDialog(
                EvoSuiteMessagesBundle.message("evosuitePortMessage"),
                EvoSuiteMessagesBundle.message("evosuitePortTitle"),
            )
        } else {
            evoSuiteSettingsState.evosuitePort = settingsComponent!!.evosuitePort
        }
    }

    /**
     * Returns the displayed name of the Settings tab.
     *
     * @return the name displayed in the menu (settings)
     */
    override fun getDisplayName(): String {
        return "TestSpark"
    }

    /**
     * Disposes the UI resources. It is called when a user closes the Settings dialog.
     */
    override fun disposeUIResources() {
        settingsComponent = null
    }
}
