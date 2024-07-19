package org.jetbrains.research.testspark.core.test

import org.jetbrains.research.testspark.core.test.data.TestLine

interface TestBodyPrinter {
    /**
     * Generates a test body as a string based on the provided parameters.
     *
     * @param testInitiatedText A string containing the upper part of the test case.
     * @param lines A mutable list of `TestLine` objects representing the lines of the test body.
     * @param throwsException The exception type that the test function throws, if any.
     * @param name The name of the test function.
     * @return A string representing the complete test body.
     */
    fun printTestBody(
        testInitiatedText: String,
        lines: MutableList<TestLine>,
        throwsException: String,
        name: String,
    ): String
}
