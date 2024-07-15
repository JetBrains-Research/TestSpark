package org.jetbrains.research.testspark.core.test.data

interface PrintTestBodyStrategy {
    fun printTestBody(
        testInitiatedText: String,
        lines: MutableList<TestLine>,
        throwsException: String,
        name: String,
    ): String
}
