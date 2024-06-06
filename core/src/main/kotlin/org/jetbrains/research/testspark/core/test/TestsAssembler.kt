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
     * Extracts test cases from raw text and generates a TestSuite using the given package name.
     *
     * @param packageName The package name to be set in the generated TestSuite.
     * @return A TestSuiteGeneratedByLLM object containing the extracted test cases and package name.
     */
    abstract fun assembleTestSuite(packageName: String): TestSuiteGeneratedByLLM?
}
