package nl.tudelft.ewi.se.ciselab.testgenie.services

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.externalSystem.ExternalSystemModulePropertyManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import nl.tudelft.ewi.se.ciselab.testgenie.TestGenieDefaultsBundle
import nl.tudelft.ewi.se.ciselab.testgenie.settings.SettingsProjectState
import java.io.File

/**
 * This class is responsible for storing the project-level settings persistently. It uses SettingsProjectState class for that.
 */
@State(name = "SettingsProjectState", storages = [Storage("TestGeniePluginSettings.xml")])
class SettingsProjectService(_project: Project) : PersistentStateComponent<SettingsProjectState> {

    private var settingsProjectState: SettingsProjectState = SettingsProjectState()
    private var project: Project = _project

    init {
        settingsProjectState.telemetryPath.plus(File.separator).plus(project.name)
        updateBuildPathAndBuildCommand()
    }

    /**
     * TODO add a comment
     * TODO create class "buildManager"
     */
    private fun updateBuildPathAndBuildCommand() {
        val buildSystemToBuildPath = mapOf(
            TestGenieDefaultsBundle.defaultValue("maven") to TestGenieDefaultsBundle.defaultValue("mavenBuildPath"),
            TestGenieDefaultsBundle.defaultValue("gradle") to TestGenieDefaultsBundle.defaultValue("gradleBuildPath")
        )
        val buildSystemToBuildCommand = mapOf(
            TestGenieDefaultsBundle.defaultValue("maven") to TestGenieDefaultsBundle.defaultValue("mavenBuildCommand"),
            TestGenieDefaultsBundle.defaultValue("gradle") to TestGenieDefaultsBundle.defaultValue("gradleBuildCommand")
        )
        val buildSystem: String? =
            ExternalSystemModulePropertyManager.getInstance(project.modules[0]).getExternalSystemId()
        buildSystem?.let {
            settingsProjectState.buildPath = buildSystemToBuildPath[buildSystem]!!
            settingsProjectState.buildCommand = buildSystemToBuildCommand[buildSystem]!!
        }
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
