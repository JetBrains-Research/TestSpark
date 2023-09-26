package org.jetbrains.research.testspark.services

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.settings.SettingsProjectState
import java.io.File

/**
 * This class is responsible for storing the project-level settings persistently. It uses SettingsProjectState class for that.
 */
@Service(Service.Level.PROJECT)
@State(name = "SettingsProjectState", storages = [Storage("TestSparkPluginSettings.xml")])
class SettingsProjectService(project: Project) : PersistentStateComponent<SettingsProjectState> {

    private var settingsProjectState: SettingsProjectState = SettingsProjectState()

    init {
        settingsProjectState.telemetryPath += File.separator.plus(project.name)
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
