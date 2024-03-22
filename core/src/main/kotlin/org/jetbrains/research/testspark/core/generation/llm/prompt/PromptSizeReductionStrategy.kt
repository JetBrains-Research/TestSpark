package org.jetbrains.research.testspark.core.generation.llm.prompt

interface PromptSizeReductionStrategy {
    /**
     * Checks if the reduction of the prompt size is possible.
     *
     * @return true if the reduction is possible, false otherwise.
     */
    fun isReductionPossible(): Boolean

    /**
     * Generates a prompt which has the reduced size compared to the previous generated prompt.
     *
     * @return a new prompt which has reduced size as a string.
     */
    fun reduceSizeAndGeneratePrompt(): String
}