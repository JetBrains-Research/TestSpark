package com.github.mitchellolsthoorn.testgenie.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*

@State(
    name = "EvoSuite Configuration", storages = [Storage("evoSuiteSettings.xml")]
)
class EvoSuiteRuntimeConfiguration : PersistentStateComponent<EvoSuiteRuntimeConfiguration.Config> {

    class Config {
        var javaPath = ""
        var evoSuiteJarPath = ""

        init {
            if (evoSuiteJarPath.isBlank()) {
                val pluginsPath = System.getProperty("idea.plugins.path");
                val localEvoSuiteJarPath = "$pluginsPath/TestGenie/lib/evosuite.jar"
                evoSuiteJarPath = localEvoSuiteJarPath
            }
        }
    }

    private var evoState = Config()

    companion object {
        @JvmStatic
        fun getInstance(): EvoSuiteRuntimeConfiguration =
            ApplicationManager.getApplication().getService(EvoSuiteRuntimeConfiguration::class.java)
    }

    override fun getState(): Config {
        return evoState
    }

    override fun loadState(state: Config) {
        evoState = state
    }

}

