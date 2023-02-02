package nl.tudelft.ewi.se.ciselab.testgenie.settings

import com.intellij.openapi.externalSystem.ExternalSystemModulePropertyManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import nl.tudelft.ewi.se.ciselab.testgenie.TestGenieDefaultsBundle

class SettingsBuildSystemApplying(_project: Project) {
    /**
     * TODO add a comment
     */
    private var buildSystem: String? =
        ExternalSystemModulePropertyManager.getInstance(_project.modules[0]).getExternalSystemId()

    /**
     * TODO add a comment
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
     * TODO add a comment
     */
    fun updateBuildPathAndBuildCommand(settingsProjectState: SettingsProjectState) {
        buildSystem?.let {
            settingsProjectState.buildPath = buildSystemToBuildPath[buildSystem]!!
            settingsProjectState.buildCommand = buildSystemToBuildCommand[buildSystem]!!
        }
    }
}
