package com.github.mitchellolsthoorn.testgenie.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "EvoSuite Configuration", storages = [Storage("evoSuiteSettings.xml")]
)
class EvoSuiteRuntimeConfiguration : PersistentStateComponent<EvoSuiteRuntimeConfiguration.Config> {

    class Config : BaseState() {
        var javaPath = ""
        var evoSuiteJarPath = "/home/lyubentodorov/Projects/uni/TestGenie/evo/evosuite.jar"
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
        XmlSerializerUtil.copyBean(state, this);
    }

}

