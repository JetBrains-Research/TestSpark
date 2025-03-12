package org.jetbrains.research.testspark.services

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.settings.llm.LLMSettingsState

/**
 * This class is responsible for storing the application-level settings persistently. It uses SettingsApplicationState class for that.
 */
@Service(Service.Level.PROJECT)
@State(name = "LLMSettingsState", storages = [Storage("LLMSettings.xml")])
class LLMSettingsService : PersistentStateComponent<LLMSettingsState> {
    private var llmSettingsState: LLMSettingsState = LLMSettingsState()

    /**
     * Gets the currently persisted state of the application.
     * This method is called every time the values in the EvoSuite Settings page are saved.
     * If the values from getState are different from the default values obtained by calling
     *  the default constructor, the state is persisted (serialised and stored).
     */
    override fun getState(): LLMSettingsState = llmSettingsState

    /**
     * Loads the state of the application-level settings.
     * This method is called after the application-level settings component has been created
     *   and if the XML file with the state is changes externally.
     */
    override fun loadState(state: LLMSettingsState) {
        llmSettingsState = state
    }

    /**
     * Returns the service object with a static call.
     */

    companion object {
        fun service(project: Project) = project.getService(LLMSettingsService::class.java).state
    }
}
