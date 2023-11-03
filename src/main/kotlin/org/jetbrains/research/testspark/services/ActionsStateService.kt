package org.jetbrains.research.testspark.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import org.jetbrains.research.testspark.actions.ActionsState

/**
 * This class is responsible for storing the application-level settings persistently. It uses ActionsStateService class for that.
 */
@Service(Service.Level.PROJECT)
@State(name = "ActionsStateService", storages = [Storage("TestSparkActions.xml")])
class ActionsStateService : PersistentStateComponent<ActionsState> {

    private var actionsState: ActionsState = ActionsState()

    /**
     * Returns the current state of the actions.
     *
     * @return the current state of the actions.
     */
    override fun getState(): ActionsState {
        return actionsState
    }

    /**
     * Loads the given ActionsState into the current instance.
     *
     * @param state The ActionsState object to be loaded.
     */
    override fun loadState(state: ActionsState) {
        actionsState = state
    }

    /**
     * Returns the service object with a static call.
     */
    companion object {
        @JvmStatic
        fun getInstance(): PersistentStateComponent<ActionsState> {
            return ApplicationManager.getApplication().getService(ActionsStateService::class.java)
        }
    }
}
