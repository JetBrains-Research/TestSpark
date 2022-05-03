package com.github.mitchellolsthoorn.testgenie.toolwindow

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "TestGenieToolWindowState", storages = [Storage("testGenieToolWindowState.xml")])
class TestGenieToolWindowService : PersistentStateComponent<TestGenieToolWindowState> {

    private var toolWindowState: TestGenieToolWindowState = TestGenieToolWindowState()

    override fun getState(): TestGenieToolWindowState? = toolWindowState

    override fun loadState(state: TestGenieToolWindowState) {
        toolWindowState = state
    }

    companion object {
        fun getInstance() : PersistentStateComponent<TestGenieToolWindowState> {
            return ApplicationManager.getApplication().getService(TestGenieToolWindowService::class.java)
        }
    }
}