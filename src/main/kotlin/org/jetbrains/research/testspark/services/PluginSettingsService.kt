package org.jetbrains.research.testspark.services

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.settings.common.PluginSettingsState

/**
 * This class is responsible for storing the project-level settings persistently. It uses SettingsProjectState class for that.
 */
@Service(Service.Level.PROJECT)
@State(name = "PluginSettingsState", storages = [Storage("ProjectSettings.xml")])
class PluginSettingsService : PersistentStateComponent<PluginSettingsState> {

    private var pluginSettingsState: PluginSettingsState = PluginSettingsState()

    /**
     * Gets the currently persisted state of the open project.
     * This method is called every time the values in the Plugin Settings page are saved.
     * If the values from getState are different from the default values obtained by calling
     *  the default constructor, the state is persisted (serialised and stored).
     */
    override fun getState(): PluginSettingsState {
        return pluginSettingsState
    }

    /**
     * Loads the state of the settings of the open project.
     * This method is called after the application-level settings component has been created
     *   and if the XML file with the state is changes externally.
     */
    override fun loadState(state: PluginSettingsState) {
        pluginSettingsState = state
    }

    companion object {
        fun service(project: Project) = project.getService(PluginSettingsService::class.java).state
    }
}
