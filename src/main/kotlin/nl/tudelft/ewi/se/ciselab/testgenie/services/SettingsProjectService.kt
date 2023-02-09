package nl.tudelft.ewi.se.ciselab.testgenie.services

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import nl.tudelft.ewi.se.ciselab.testgenie.settings.SettingsBuildSystemApplying
import nl.tudelft.ewi.se.ciselab.testgenie.settings.SettingsProjectState
import java.io.File

/**
 * This class is responsible for storing the project-level settings persistently. It uses SettingsProjectState class for that.
 */
@State(name = "SettingsProjectState", storages = [Storage("TestGeniePluginSettings.xml")])
class SettingsProjectService(_project: Project) : PersistentStateComponent<SettingsProjectState> {

    private var settingsProjectState: SettingsProjectState = SettingsProjectState()
    private var project: Project = _project

    /**
     * Structure for fixing the default buildPath and buildCommand depending on project's build system.
     */
    private var settingsBuildSystemApplying: SettingsBuildSystemApplying = SettingsBuildSystemApplying(project)

    init {
        settingsProjectState.telemetryPath += File.separator.plus(project.name)
        settingsProjectState.buildPath += File.separator.plus(project.name) // "out/production" -> "out/production/projectName"
        settingsBuildSystemApplying.updateBuildPathAndBuildCommand(settingsProjectState)
    }

    /**
     * Gets the currently persisted state of the open project.
     * This method is called every time the values in the Plugin Settings page are saved.
     * If the values from getState are different from the default values obtained by calling
     *  the default constructor, the state is persisted (serialised and stored).
     */
    override fun getState(): SettingsProjectState {
        return settingsProjectState
    }

    /**
     * Loads the state of the settings of the open project.
     * This method is called after the application-level settings component has been created
     *   and if the XML file with the state is changes externally.
     */
    override fun loadState(state: SettingsProjectState) {
        settingsProjectState = state
    }
}
