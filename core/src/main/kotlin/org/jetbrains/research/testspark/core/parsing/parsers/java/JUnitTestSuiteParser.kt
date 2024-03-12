package org.jetbrains.research.testspark.core.parsing.parsers.java

import org.jetbrains.research.testspark.core.data.JUnitVersion
import org.jetbrains.research.testspark.core.generation.importPattern
import org.jetbrains.research.testspark.core.parsing.parsers.TestSuiteParser
import org.jetbrains.research.testspark.core.parsing.test.ParsedTestSuite
import org.jetbrains.research.testspark.core.parsing.test.TestCaseGeneratedByLLM
import org.jetbrains.research.testspark.core.parsing.test.TestLine
import org.jetbrains.research.testspark.core.parsing.test.TestLineType


class JUnitTestSuiteParser(
    private val packageName: String,
    private val junitVersion: JUnitVersion
) : TestSuiteParser {
    override fun parseTestSuite(rawText: String): ParsedTestSuite? {
        if (rawText.isBlank()) {
            return null
        }

        try {
            var rawCode = rawText

            if (rawText.contains("```")) {
                rawCode = rawText.split("```")[1]
            }

            // save imports
            val imports = importPattern.findAll(rawCode, 0)
                .map { it.groupValues[0] }
                .toSet()

            // save RunWith
            val runWith: String = junitVersion.runWithAnnotationMeta.extract(rawCode) ?: ""

            val testSet: MutableList<String> = rawCode.split("@Test").toMutableList()

            // save annotations and pre-set methods
            val otherInfo: String = run {
                val otherInfoList = testSet.removeAt(0).split("public class")[1].split("{").toMutableList()
                otherInfoList.removeFirst()
                val otherInfo = otherInfoList.joinToString("{").trimEnd() + "\n\n"
                otherInfo.ifBlank { "" }
            }

            // Save the main test cases
            val testCases: MutableList<TestCaseGeneratedByLLM> = mutableListOf()
            val testCaseParser = JUnitTestCaseParser()

            testSet.forEach ca@{
                val rawTest = "@Test$it"

                val isLastTestCaseInTestSuite = (testCases.size == testSet.size - 1)
                val currentTest = testCaseParser.parse(rawTest, isLastTestCaseInTestSuite) ?: return@ca

                // TODO: make logging work
                // log.info("New test case: $currentTest")
                println("New test case: $currentTest")

                testCases.add(currentTest)
            }

            val testSuite = ParsedTestSuite(
                imports = imports,
                packageString = packageName,
                runWith = runWith,
                otherInfo = otherInfo,
                testCases = testCases,
            )

            return testSuite
        } catch (e: Exception) {
            return null
        }
    }
}

private class JUnitTestCaseParser {
    fun parse(rawTest: String, isLastTestCaseInTestSuite: Boolean): TestCaseGeneratedByLLM? {
        var expectedException = ""
        var throwsException = ""
        val testLines: MutableList<TestLine> = mutableListOf()

        // Get expected Exception
        if (rawTest.startsWith("@Test(expected =")) {
            expectedException = rawTest.split(")")[0].trim()
        }

        // Get unexpected exceptions
        if (!rawTest.contains("public void")) {
            println("WARNING: The raw Test does not contain public void:\n $rawTest")
            return null
        }
        val interestingPartOfSignature = rawTest.split("public void")[1]
            .split("{")[0]
            .split("()")[1]
            .trim()

        if (interestingPartOfSignature.contains("throws")) {
            throwsException = interestingPartOfSignature.split("throws")[1].trim()
        }

        // Get test name
        val testName: String = rawTest.split("public void ")[1]
            .split("()")[0]
            .trim()

        // Get test body and remove opening bracket
        var testBody = rawTest.split("{").toMutableList().apply {removeFirst() }
            .joinToString("{").trim()

        // remove closing bracket
        val tempList = testBody.split("}").toMutableList()
        tempList.removeLast()

        if (isLastTestCaseInTestSuite) {
            // it is the last test, thus we should remove another closing bracket
            if (tempList.isNotEmpty()) {
                tempList.removeLast()
            } else {
                println("WARNING: the final test does not have to brackets:\n $testBody")
            }
        }

        testBody = tempList.joinToString("}")

        // Save each line
        val rawLines = testBody.split("\n").toMutableList()
        rawLines.forEach { rawLine ->
            val line = rawLine.trim()

            val type: TestLineType = when {
                line.startsWith("//") -> TestLineType.COMMENT
                line.isBlank() -> TestLineType.BREAK
                line.lowercase().startsWith("assert") -> TestLineType.ASSERTION
                else -> TestLineType.CODE
            }

            testLines.add(TestLine(type, line))
        }

        val currentTest = TestCaseGeneratedByLLM(
            name = testName,
            expectedException = expectedException,
            throwsException = throwsException,
            lines = testLines,
        )

        return currentTest
    }
}