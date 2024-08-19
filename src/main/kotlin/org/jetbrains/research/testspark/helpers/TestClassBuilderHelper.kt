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
     * Extracts the code of the first test method found in the given class code.
     *
     * @param classCode The code of the class containing test methods.
     * @return The code of the first test method as a string, including the "@Test" annotation.
     */
    fun extractFirstTestMethodCode(classCode: String): String

    /**
     * Retrieves the name of the first test method found in the given class code.
     *
     * @param oldTestCaseName The old name of test case
     * @param classCode The source code of the class containing test methods.
     * @return The name of the first test method. If no test method is found, an empty string is returned.
     */
    fun extractFirstTestMethodName(oldTestCaseName: String, classCode: String): String

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
