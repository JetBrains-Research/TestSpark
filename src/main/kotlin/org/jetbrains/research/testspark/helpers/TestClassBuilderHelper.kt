package org.jetbrains.research.testspark.helpers

import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.core.data.TestGenerationData

interface TestClassBuilderHelper {
    /**
     * Generates the code for a test class.
     *
     * @param className the name of the test class
     * @param body the body of the test class
     * @return the generated code as a string
     */
    fun generateCode(
        project: Project,
        className: String,
        body: String,
        imports: Set<String>,
        packageString: String,
        runWith: String,
        otherInfo: String,
        testGenerationData: TestGenerationData,
    ): String

    /**
     * Finds the test method from a given class with the specified test case name.
     *
     * @param code The code of the class containing test methods.
     * @return The test method as a string, including the "@Test" annotation.
     */
    fun getTestMethodCodeFromClassWithTestCase(code: String): String

    /**
     * Retrieves the name of the test method from a given Java class with test cases.
     *
     * @param oldTestCaseName The old name of test case
     * @param code The source code of the Java class with test cases.
     * @return The name of the test method. If no test method is found, an empty string is returned.
     */
    fun getTestMethodNameFromClassWithTestCase(oldTestCaseName: String, code: String): String

    /**
     * Retrieves the class name from the given test case code.
     *
     * @param code The test case code to extract the class name from.
     * @return The class name extracted from the test case code.
     */
    fun getClassFromTestCaseCode(code: String): String

    /**
     * Formats the given Java code using IntelliJ IDEA's code formatting rules.
     *
     * @param code The Java code to be formatted.
     * @return The formatted Java code.
     */
    fun formatCode(project: Project, code: String, generatedTestData: TestGenerationData): String
}
