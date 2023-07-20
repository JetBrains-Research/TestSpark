package org.jetbrains.research.testgenie.tools.llm

import org.jetbrains.research.testgenie.services.SettingsApplicationService

/**
 * A class that provides access to various settings arguments.
 */
class SettingsArguments {
    companion object {
        val settingsState = SettingsApplicationService.getInstance().state

        /**
         * Retrieves the Grazie user token from the application settings.
         *
         * @return The Grazie user token.
         */
        fun grazieUserToken(): String = settingsState!!.grazieUserToken

        /**
         * Retrieves the maximum LLM (Longest Lasting Message) request value from the settings state.
         *
         * @return The maximum LLM request value.
         */
        fun maxLLMRequest(): Int = settingsState!!.maxLLMRequest

        /**
         * Returns the maximum depth for input parameters.
         *
         * @return The maximum depth for input parameters.
         */
        fun maxInputParamsDepth(): Int = settingsState!!.maxInputParamsDepth

        /**
         * Returns the maximum depth of polygons.
         *
         * @return The maximum depth of polygons.
         */
        fun maxPolyDepth(): Int = settingsState!!.maxPolyDepth

        /**
         * Checks if the token is set for the user in the settings.
         *
         * @return true if the token is set, false otherwise
         */
        fun isTokenSet(): Boolean = settingsState!!.grazieUserToken.isNotEmpty()
    }
}
