package org.jetbrains.research.testgenie.tools.llm

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.jetbrains.research.testgenie.editor.Workspace
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
         * @param project the project for which to retrieve the maximum input parameters depth value
         * @return The maximum depth for input parameters.
         */
        fun maxInputParamsDepth(project: Project): Int = settingsState!!.maxInputParamsDepth - project.service<Workspace>().testGenerationData.inputParamsDepthReducing

        /**
         * Returns the maximum depth of polygons.
         *
         * @return The maximum depth of polygons.
         */
        fun maxPolyDepth(project: Project): Int = settingsState!!.maxPolyDepth - project.service<Workspace>().testGenerationData.polyDepthReducing

        /**
         * Checks if the token is set for the user in the settings.
         *
         * @return true if the token is set, false otherwise
         */
        fun isTokenSet(): Boolean = settingsState!!.grazieUserToken.isNotEmpty()
    }
}
