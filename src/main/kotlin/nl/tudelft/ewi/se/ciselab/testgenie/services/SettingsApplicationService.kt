package nl.tudelft.ewi.se.ciselab.testgenie.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import nl.tudelft.ewi.se.ciselab.testgenie.settings.SettingsApplicationState

/**
 * This class is responsible for storing the application-level settings persistently. It uses SettingsApplicationState class for that.
 */
@State(name = "TestGenieSettingsApplicationState", storages = [Storage("TestGenieSettingsApplication")])
class SettingsApplicationService : PersistentStateComponent<SettingsApplicationState> {

    private var settingsApplicationState: SettingsApplicationState = SettingsApplicationState()

    /**
     * Gets the currently persisted state of the application.
     * This method is called every time the values in the EvoSuite Settings page are saved.
     * If the values from getState are different from the default values obtained by calling
     *  the default constructor, the state is persisted (serialised and stored).
     */
    override fun getState(): SettingsApplicationState {
        return settingsApplicationState
    }

    /**
     * Loads the state of the application-level settings.
     * This method is called after the application-level settings component has been created
     *   and if the XML file with the state is changes externally.
     */
    override fun loadState(state: SettingsApplicationState) {
        settingsApplicationState = state
    }

    /**
     * Returns the service object with a static call.
     */
    companion object {
        @JvmStatic
        fun getInstance(): PersistentStateComponent<SettingsApplicationState> {
            return ApplicationManager.getApplication().getService(SettingsApplicationService::class.java)
        }
    }
}
