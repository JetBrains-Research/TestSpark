package org.jetbrains.research.testgenie.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import org.jetbrains.research.testgenie.toolwindow.QuickAccessParametersState

/**
 * This class is responsible for helping to implement persistence for Quick Access Tool Menu on a sidebar.
 * It provides ability to load state and get state.
 */
@State(name = "QuickAccessParametersState", storages = [Storage("testGenieQuickAccessParametersState.xml")])
class QuickAccessParametersService : PersistentStateComponent<QuickAccessParametersState> {

    private var quickAccessParameters: QuickAccessParametersState = QuickAccessParametersState()

    /**
     * Gets the currently persisted state. This method is called every time the save button is pressed in the menu.
     *
     * @return the current tool window state
     */
    override fun getState(): QuickAccessParametersState = quickAccessParameters

    /**
     * Loads the state. Everytime we need to update the parameters in the GUI according to a new state we call this
     * method.
     *
     * @param state the new state
     */
    override fun loadState(state: QuickAccessParametersState) {
        quickAccessParameters = state
    }

    companion object {

        /**
         * Returns the service object with a static call.
         *
         * @return the service that manages the state
         */
        fun getInstance(): PersistentStateComponent<QuickAccessParametersState> {
            return ApplicationManager.getApplication().getService(QuickAccessParametersService::class.java)
        }
    }
}
