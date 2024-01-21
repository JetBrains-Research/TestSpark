package org.jetbrains.research.testspark.tools.llm

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.services.SettingsApplicationService
import org.jetbrains.research.testspark.services.TestGenerationDataService

/**
 * A class that provides access to various settings arguments.
 */
class SettingsArguments {
    companion object {
        val settingsState = SettingsApplicationService.getInstance().state

        /**
         * Retrieves the OpenAI token from the application's settings.
         *
         * @return The OpenAI token as a string.
         */
        fun openAIToken(): String = settingsState!!.openAIToken

        /**
         * Retrieves the Grazie token from the settings state.
         *
         * @return The Grazie token as a string.
         */
        fun grazieToken(): String = settingsState!!.grazieToken

        /**
         * Opens the AI model associated with the given settings state.
         *
         * @return The AI model as a string.
         */
        fun openAIModel(): String = settingsState!!.openAIModel

        /**
         * Retrieves the Grazie model from the application's settings state.
         *
         * @return The Grazie model as a String.
         */
        fun grazieModel(): String = settingsState!!.grazieModel

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
        fun llmPlatform(): String = settingsState!!.llmPlatform

        /**
         * Retrieves the token for the current user.
         *
         * @return The token as a string.
         */
        fun getToken(): String {
            return if (llmPlatform() == "Grazie")
                grazieToken()
            else
                openAIToken()
        }
    }
}
