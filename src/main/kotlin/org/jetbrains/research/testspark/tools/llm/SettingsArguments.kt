package org.jetbrains.research.testspark.tools.llm

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.services.SettingsApplicationService
import org.jetbrains.research.testspark.services.TestGenerationDataService
import org.jetbrains.research.testspark.settings.SettingsApplicationState

/**
 * A class that provides access to various settings arguments.
 */
class SettingsArguments {
    companion object {
        private val settingsState: SettingsApplicationState?
            get() = SettingsApplicationService.getInstance().state

        /**
         * Retrieves the maximum LLM (Longest Lasting Message) request value from the settings state.
         *
         * @return The maximum LLM request value.
         */
        fun maxLLMRequest(): Int = settingsState!!.maxLLMRequest

        /**
         * Returns the maximum depth for input parameters.
         *
         * @param project the project for which to retrieve the maximum input parameters depth value
         * @return The maximum depth for input parameters.
         */
        fun maxInputParamsDepth(project: Project): Int =
            settingsState!!.maxInputParamsDepth - project.service<TestGenerationDataService>().inputParamsDepthReducing

        /**
         * Returns the maximum depth of polymorphism.
         *
         * @return The maximum depth of polymorphism.
         */
        fun maxPolyDepth(project: Project): Int =
            settingsState!!.maxPolyDepth - project.service<TestGenerationDataService>().polyDepthReducing

        /**
         * Checks if the token is set for the user in the settings.
         *
         * @return true if the token is set, false otherwise
         */
        fun isTokenSet(): Boolean = getToken().isNotEmpty()

        /**
         * Return the selected LLm platform
         *
         * @return selected LLM platform
         */
        fun currentLLMPlatformName(): String = settingsState!!.currentLLMPlatformName

        /**
         * Retrieves the token for the current user.
         *
         * @return The token as a string.
         */
        fun getToken(): String = when (currentLLMPlatformName()) {
            settingsState!!.openAIName -> settingsState!!.openAIToken
            settingsState!!.grazieName -> settingsState!!.grazieToken
            else -> ""
        }

        /**
         * Retrieves the token for the current user.
         *
         * @return The token as a string.
         */
        fun getModel(): String = when (currentLLMPlatformName()) {
            settingsState!!.openAIName -> settingsState!!.openAIModel
            settingsState!!.grazieName -> settingsState!!.grazieModel
            else -> ""
        }
    }
}
