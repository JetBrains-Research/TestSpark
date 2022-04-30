package com.github.mitchellolsthoorn.testgenie.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "EvoSuite Configuration", storages = [Storage("EvoSuiteSettings.xml")]
)
class EvoSuiteRuntimeConfiguration(internal val project: Project) :
    SimplePersistentStateComponent<EvoSuiteRuntimeConfiguration.Config>(Config()) {

    public var javaPath: String = ""
    public var evoSuiteJarPath: String = ""


    class Config : BaseState() {
        var javaPath by string()
        var evoSuiteJarPath by string()
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): EvoSuiteRuntimeConfiguration = project.service()
    }

}

