package org.jetbrains.research.testspark.settings.evosuite

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.Messages
import org.jetbrains.research.testspark.tools.evosuite.SettingsArguments
import javax.swing.JComponent

/**
 * This class allows to configure some EvoSuite settings via the EvoSuite page in the Settings dialog,
 *   observes the changes and manages the UI and state.
 * It interacts with the SettingsEvoSuiteComponent, TestSparkSettingsService and TestSparkSettingsState.
 * It provides controller functionality for the TestSparkSettingsState.
 */
class SettingsEvoSuiteConfigurable : Configurable {

    var settingsComponent: SettingsEvoSuiteComponent? = null

    /**
     * Creates a settings component that holds the panel with the settings entries, and returns this panel
     *
     * @return the panel used for displaying settings
     */
    override fun createComponent(): JComponent? {
        settingsComponent = SettingsEvoSuiteComponent()
        return settingsComponent!!.panel
    }

    /**
     * Sets the stored state values to the corresponding UI components. This method is called immediately after `createComponent` method.
     */
    override fun reset() {
        settingsComponent!!.javaPath = SettingsArguments.settingsState!!.javaPath
        settingsComponent!!.sandbox = SettingsArguments.settingsState!!.sandbox
        settingsComponent!!.assertions = SettingsArguments.settingsState!!.assertions
        settingsComponent!!.seed = SettingsArguments.settingsState!!.seed
        settingsComponent!!.algorithm = SettingsArguments.settingsState!!.algorithm
        settingsComponent!!.evosuiteSetupCheckBoxSelected = SettingsArguments.settingsState!!.evosuiteSetupCheckBoxSelected
        settingsComponent!!.configurationId = SettingsArguments.settingsState!!.configurationId
        settingsComponent!!.clientOnThread = SettingsArguments.settingsState!!.clientOnThread
        settingsComponent!!.junitCheck = SettingsArguments.settingsState!!.junitCheck
        settingsComponent!!.criterionLine = SettingsArguments.settingsState!!.criterionLine
        settingsComponent!!.criterionBranch = SettingsArguments.settingsState!!.criterionBranch
        settingsComponent!!.criterionException = SettingsArguments.settingsState!!.criterionException
        settingsComponent!!.criterionWeakMutation = SettingsArguments.settingsState!!.criterionWeakMutation
        settingsComponent!!.criterionOutput = SettingsArguments.settingsState!!.criterionOutput
        settingsComponent!!.criterionMethod = SettingsArguments.settingsState!!.criterionMethod
        settingsComponent!!.criterionMethodNoException = SettingsArguments.settingsState!!.criterionMethodNoException
        settingsComponent!!.criterionCBranch = SettingsArguments.settingsState!!.criterionCBranch
        settingsComponent!!.minimize = SettingsArguments.settingsState!!.minimize
    }

    /**
     * Checks if the values of the entries in the settings state are different from the persisted values of these entries.
     *
     * @return whether any setting has been modified
     */
    override fun isModified(): Boolean {
        var modified: Boolean = settingsComponent!!.sandbox != SettingsArguments.settingsState!!.sandbox
        modified = modified or (settingsComponent!!.javaPath != SettingsArguments.settingsState!!.javaPath)
        modified = modified or (settingsComponent!!.assertions != SettingsArguments.settingsState!!.assertions)
        modified = modified or (settingsComponent!!.seed != SettingsArguments.settingsState!!.seed)
        modified = modified or (settingsComponent!!.algorithm != SettingsArguments.settingsState!!.algorithm)
        modified = modified or (settingsComponent!!.evosuiteSetupCheckBoxSelected != SettingsArguments.settingsState!!.evosuiteSetupCheckBoxSelected)
        modified = modified or (settingsComponent!!.configurationId != SettingsArguments.settingsState!!.configurationId)
        modified = modified or (settingsComponent!!.clientOnThread != SettingsArguments.settingsState!!.clientOnThread)
        modified = modified or (settingsComponent!!.junitCheck != SettingsArguments.settingsState!!.junitCheck)
        modified = modified or (settingsComponent!!.criterionLine != SettingsArguments.settingsState!!.criterionLine)
        modified = modified or (settingsComponent!!.criterionBranch != SettingsArguments.settingsState!!.criterionBranch)
        modified = modified or (settingsComponent!!.criterionException != SettingsArguments.settingsState!!.criterionException)
        modified = modified or (settingsComponent!!.criterionWeakMutation != SettingsArguments.settingsState!!.criterionWeakMutation)
        modified = modified or (settingsComponent!!.criterionOutput != SettingsArguments.settingsState!!.criterionOutput)
        modified = modified or (settingsComponent!!.criterionMethod != SettingsArguments.settingsState!!.criterionMethod)
        modified = modified or (settingsComponent!!.criterionMethodNoException != SettingsArguments.settingsState!!.criterionMethodNoException)
        modified = modified or (settingsComponent!!.criterionCBranch != SettingsArguments.settingsState!!.criterionCBranch)
        modified = modified or (settingsComponent!!.minimize != SettingsArguments.settingsState!!.minimize)
        return modified
    }

    /**
     * Persists the modified state after a user hit Apply button.
     */
    override fun apply() {
        SettingsArguments.settingsState!!.javaPath = settingsComponent!!.javaPath
        SettingsArguments.settingsState!!.sandbox = settingsComponent!!.sandbox
        SettingsArguments.settingsState!!.assertions = settingsComponent!!.assertions
        SettingsArguments.settingsState!!.algorithm = settingsComponent!!.algorithm
        SettingsArguments.settingsState!!.evosuiteSetupCheckBoxSelected = settingsComponent!!.evosuiteSetupCheckBoxSelected
        SettingsArguments.settingsState!!.configurationId = settingsComponent!!.configurationId
        SettingsArguments.settingsState!!.clientOnThread = settingsComponent!!.clientOnThread
        SettingsArguments.settingsState!!.junitCheck = settingsComponent!!.junitCheck
        SettingsArguments.settingsState!!.criterionLine = settingsComponent!!.criterionLine
        SettingsArguments.settingsState!!.criterionBranch = settingsComponent!!.criterionBranch
        SettingsArguments.settingsState!!.criterionException = settingsComponent!!.criterionException
        SettingsArguments.settingsState!!.criterionWeakMutation = settingsComponent!!.criterionWeakMutation
        SettingsArguments.settingsState!!.criterionOutput = settingsComponent!!.criterionOutput
        SettingsArguments.settingsState!!.criterionMethod = settingsComponent!!.criterionMethod
        SettingsArguments.settingsState!!.criterionMethodNoException = settingsComponent!!.criterionMethodNoException
        SettingsArguments.settingsState!!.criterionCBranch = settingsComponent!!.criterionCBranch
        SettingsArguments.settingsState!!.minimize = settingsComponent!!.minimize

        val seed = settingsComponent!!.seed.toLongOrNull()
        if (settingsComponent!!.seed != "" && seed == null) {
            Messages.showErrorDialog(
                "Seed parameter is not of numeric type. Therefore, it will not be saved. However, the rest of the parameters have been successfully saved.",
                "Incorrect Numeric Type For Seed",
            )
            return
        }
        SettingsArguments.settingsState!!.seed = settingsComponent!!.seed
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
     * Returns the UI component that should be focused when the TestSpark Settings page is opened.
     *
     *  @return preferred UI component
     */
    override fun getPreferredFocusedComponent(): JComponent {
        return settingsComponent!!.getPreferredFocusedComponent()
    }

    /**
     * Disposes the UI resources. It is called when a user closes the Settings dialog.
     */
    override fun disposeUIResources() {
        settingsComponent = null
    }
}
