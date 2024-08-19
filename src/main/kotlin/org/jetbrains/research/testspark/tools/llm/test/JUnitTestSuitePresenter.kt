package org.jetbrains.research.testspark.tools.llm.test

import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.core.data.TestGenerationData
import org.jetbrains.research.testspark.core.generation.llm.getClassWithTestCaseName
import org.jetbrains.research.testspark.core.test.SupportedLanguage
import org.jetbrains.research.testspark.core.test.data.TestSuiteGeneratedByLLM
import org.jetbrains.research.testspark.helpers.java.JavaClassBuilderHelper
import org.jetbrains.research.testspark.helpers.kotlin.KotlinClassBuilderHelper

class JUnitTestSuitePresenter(
    private val project: Project,
    private val generatedTestsData: TestGenerationData,
    private val language: SupportedLanguage,
) {
    /**
     * Returns a string representation of this object.
     *
     * The returned string includes the package name, imports, test class declaration, and test cases.
     *
     * If the package name is not blank, it is added to the string representation with the prefix "package ".
     *
     * Each import in the imports list is added to the string representation followed by a new line.
     *
     * The test class declaration "public class GeneratedTest{" is included in the string representation.
     *
     * Each test case in the testCases list is appended to the string representation.
     *
     * The test class closing bracket "}" is included in the string representation.
     *
     * @return A string representing the test file.
     */
    fun toString(testSuite: TestSuiteGeneratedByLLM): String {
        var testBody = ""

        return testSuite.run {
            // Add each test
            testCases.forEach { testCase -> testBody += "$testCase\n" }

            when (language) {
                SupportedLanguage.Java -> JavaClassBuilderHelper.generateCode(
                    project,
                    testFileName,
                    testBody,
                    imports,
                    packageName,
                    runWith,
                    otherInfo,
                    generatedTestsData,
                )

                SupportedLanguage.Kotlin -> KotlinClassBuilderHelper.generateCode(
                    project,
                    testFileName,
                    testBody,
                    imports,
                    packageName,
                    runWith,
                    otherInfo,
                    generatedTestsData,
                )
            }
        }
    }

    /**
     * Returns the full text of the test suite (excluding the expected exception).
     *
     * @return the full text of the test suite (excluding the expected exception) as a string.
     */
    fun toStringSingleTestCaseWithoutExpectedException(
        testSuite: TestSuiteGeneratedByLLM,
        testCaseIndex: Int,
    ): String =
        testSuite.run {
            when (language) {
                SupportedLanguage.Java -> JavaClassBuilderHelper.generateCode(
                    project,
                    getClassWithTestCaseName(testCases[testCaseIndex].name),
                    testCases[testCaseIndex].toStringWithoutExpectedException() + "\n",
                    imports,
                    packageName,
                    runWith,
                    otherInfo,
                    generatedTestsData,
                )

                SupportedLanguage.Kotlin -> KotlinClassBuilderHelper.generateCode(
                    project,
                    getClassWithTestCaseName(testCases[testCaseIndex].name),
                    testCases[testCaseIndex].toStringWithoutExpectedException() + "\n",
                    imports,
                    packageName,
                    runWith,
                    otherInfo,
                    generatedTestsData,
                )
            }
        }

    /**
     * Returns the full text of the test suite (excluding the expected exception).
     *
     * @return the full text of the test suite (excluding the expected exception) as a string.
     */
    fun toStringWithoutExpectedException(testSuite: TestSuiteGeneratedByLLM): String {
        var testBody = ""

        return testSuite.run {
            // Add each test (exclude expected exception)
            testCases.forEach { testCase -> testBody += "${testCase.toStringWithoutExpectedException()}\n" }

            when (language) {
                SupportedLanguage.Java ->
                    JavaClassBuilderHelper.generateCode(
                        project,
                        testFileName,
                        testBody,
                        imports,
                        packageName,
                        runWith,
                        otherInfo,
                        generatedTestsData,
                    )

                SupportedLanguage.Kotlin -> KotlinClassBuilderHelper.generateCode(
                    project,
                    testFileName,
                    testBody,
                    imports,
                    packageName,
                    runWith,
                    otherInfo,
                    generatedTestsData,
                )
            }
        }
    }

    /**
     * Returns a printable package string.
     *
     * If the package string is empty or consists of only whitespace characters, an empty string is returned.
     * Otherwise, the package string followed by a period is returned.
     *
     * @return The printable package string.
     */
    fun getPrintablePackageString(testSuite: TestSuiteGeneratedByLLM): String {
        return testSuite.run {
            when {
                packageName.isEmpty() || packageName.isBlank() -> ""
                else -> packageName
            }
        }
    }
}
