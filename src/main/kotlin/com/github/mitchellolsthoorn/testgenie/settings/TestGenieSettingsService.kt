package com.github.mitchellolsthoorn.testgenie.settings

import com.intellij.openapi.application.ApplicationManager

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "TestGenieSettingsState", storages = [Storage("testGenieSettings.xml")])
class TestGenieSettingsService : PersistentStateComponent<TestGenieSettingsState> {

    private var testGenieSettingsState: TestGenieSettingsState = TestGenieSettingsState()

    override fun getState(): TestGenieSettingsState {
        return testGenieSettingsState
    }

    override fun loadState(state: TestGenieSettingsState) {
        testGenieSettingsState = state
    }


    companion object {
        val instance: PersistentStateComponent<TestGenieSettingsState>
            get() = ApplicationManager.getApplication().getService(TestGenieSettingsService::class.java)
    }
}