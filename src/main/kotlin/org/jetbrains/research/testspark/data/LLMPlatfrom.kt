package org.jetbrains.research.testspark.data

/**
 * Class representing a LLM (Low-Level Middleware) platform.
 *
 * @property name The name of the platform.
 * @property token The token used for authentication.
 * @property model The model of the platform.
 */
data class LLMPlatform(val name: String, var token: String, var model: String)
