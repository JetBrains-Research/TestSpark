package nl.tudelft.ewi.se.ciselab.testgenie.toolwindow

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

/**
 * This class is responsible for helping to implement persistence for Quick Access Tool Menu on a sidebar.
 * It provides ability to load state and get state.
 */
@State(name = "TestGenieToolWindowState", storages = [Storage("testGenieToolWindowState.xml")])
class TestGenieToolWindowService : PersistentStateComponent<TestGenieToolWindowState> {

    private var toolWindowState: TestGenieToolWindowState = TestGenieToolWindowState()

    /**
     * Gets the currently persisted state. This method is called every time the save button is pressed in the menu.
     *
     * @return the current tool window state
     */
    override fun getState(): TestGenieToolWindowState = toolWindowState

    /**
     * Loads the state. Everytime we need to update the parameters in the GUI according to a new state we call this
     * method.
     *
     * @param state the new state
     */
    override fun loadState(state: TestGenieToolWindowState) {
        toolWindowState = state
    }

    companion object {

        /**
         * Returns the service object with a static call.
         *
         * @return the service that manages the state
         */
        fun getInstance() : PersistentStateComponent<TestGenieToolWindowState> {
            return ApplicationManager.getApplication().getService(TestGenieToolWindowService::class.java)
        }
    }
}