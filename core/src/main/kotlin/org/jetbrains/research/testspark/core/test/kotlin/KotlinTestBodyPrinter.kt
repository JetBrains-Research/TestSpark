package org.jetbrains.research.testspark.core.test.kotlin

import org.jetbrains.research.testspark.core.test.TestBodyPrinter
import org.jetbrains.research.testspark.core.test.data.TestLine
import org.jetbrains.research.testspark.core.test.data.TestLineType

class KotlinTestBodyPrinter : TestBodyPrinter {
    override fun printTestBody(
        testInitiatedText: String,
        lines: MutableList<TestLine>,
        throwsException: String,
        name: String,
    ): String {
        var testFullText = testInitiatedText

        // start writing the test signature
        testFullText += "\n\tfun $name() "

        // add throws exception if exists
        if (throwsException.isNotBlank()) {
            testFullText += "throws $throwsException"
        }

        // start writing the test lines
        testFullText += "{\n"

        // write each line
        lines.forEach { line ->
            testFullText += when (line.type) {
                TestLineType.BREAK -> "\t\t\n"
                else -> "\t\t${line.text}\n"
            }
        }

        // close test case
        testFullText += "\t}\n"

        return testFullText
    }
}
