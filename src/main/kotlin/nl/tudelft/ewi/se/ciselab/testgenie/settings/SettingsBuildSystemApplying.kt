package nl.tudelft.ewi.se.ciselab.testgenie.settings

import com.intellij.openapi.externalSystem.ExternalSystemModulePropertyManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import nl.tudelft.ewi.se.ciselab.testgenie.TestGenieDefaultsBundle

class SettingsBuildSystemApplying(_project: Project) {
    /**
     * To recognize the build system, the ExternalSystemModulePropertyManager class is used.
     */
    private var buildSystem: String? =
        ExternalSystemModulePropertyManager.getInstance(_project.modules[0]).getExternalSystemId()

    /**
     * List of build systems.
     */
    private val buildSystemToBuildPath = mapOf(
        TestGenieDefaultsBundle.defaultValue("maven") to TestGenieDefaultsBundle.defaultValue("mavenBuildPath"),
        TestGenieDefaultsBundle.defaultValue("gradle") to TestGenieDefaultsBundle.defaultValue("gradleBuildPath")
    )
    private val buildSystemToBuildCommand = mapOf(
        TestGenieDefaultsBundle.defaultValue("maven") to TestGenieDefaultsBundle.defaultValue("mavenBuildCommand"),
        TestGenieDefaultsBundle.defaultValue("gradle") to TestGenieDefaultsBundle.defaultValue("gradleBuildCommand")
    )

    /**
     * Called from SettingsProjectService.
     */
    fun updateBuildPathAndBuildCommand(settingsProjectState: SettingsProjectState) {
        buildSystem?.let {
            // TODO add an exception if (buildSystemToBuildPath[buildSystem] == null)
            settingsProjectState.buildPath = buildSystemToBuildPath[buildSystem]!!
            settingsProjectState.buildCommand = buildSystemToBuildCommand[buildSystem]!!
        }
    }
}
