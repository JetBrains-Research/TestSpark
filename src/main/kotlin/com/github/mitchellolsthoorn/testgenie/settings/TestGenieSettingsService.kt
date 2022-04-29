package com.github.mitchellolsthoorn.testgenie.settings

import com.intellij.openapi.application.ApplicationManager

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "TestGenieSettingsState", storages = [Storage("testGenieSettings.xml")])
class TestGenieSettingsService : PersistentStateComponent<TestGenieSettingsState> {

    private var testGenieSettingsState: TestGenieSettingsState = TestGenieSettingsState()

    override fun getState(): TestGenieSettingsState {
        return testGenieSettingsState
    }

    override fun loadState(state: TestGenieSettingsState) {
        //XmlSerializerUtil.copyBean(state, testGenieSettingsState);
        testGenieSettingsState = state
    }


    companion object {
        @JvmStatic
        fun getInstance(): PersistentStateComponent<TestGenieSettingsState> {
            return ServiceManager.getService(TestGenieSettingsService::class.java)
        }
    }
}