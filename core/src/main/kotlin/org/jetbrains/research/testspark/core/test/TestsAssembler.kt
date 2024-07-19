package org.jetbrains.research.testspark.core.test

import org.jetbrains.research.testspark.core.test.data.TestSuiteGeneratedByLLM

abstract class TestsAssembler {
    private var rawText = ""

    /**
     * Receives a text chunk of the response of an LLM.
     * Derived classes must call this method in order to store a piece of content and further get if from the getContent method.
     *
     * @param text part of the LLM response
     */
    open fun consume(text: String) {
        rawText = rawText.plus(text)
    }

    /**
     * Returns the content of the LLM response collected so far.
     *
     * @return The content of the LLM response.
     */
    fun getContent(): String {
        return rawText
    }

    /**
     * Clears the raw text content stored in the TestsAssembler object.
     */
    fun clear() {
        rawText = ""
    }

    /**
     * Extracts test cases from raw text and generates a TestSuite.
     *
     * @return A TestSuiteGeneratedByLLM object containing information about the extracted test cases.
     */
    abstract fun assembleTestSuite(): TestSuiteGeneratedByLLM?
}
