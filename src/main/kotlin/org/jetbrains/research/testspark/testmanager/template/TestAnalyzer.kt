package org.jetbrains.research.testspark.testmanager.template

/**
 * Interface for retrieving information from test class code.
 */
interface TestAnalyzer {
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
     * @param oldTestCaseName The old name of a test case
     * @param classCode The source code of the class containing test methods.
     * @return The name of the first test method. If no test method is found, an empty string is returned.
     */
    fun extractFirstTestMethodName(oldTestCaseName: String, classCode: String): String

    /**
     * Retrieves the class name from the given test case code.
     *
     * @param code the test case code to extract the class name from
     * @return the class name extracted from the test case code
     */
    fun getClassFromTestCaseCode(code: String): String

    /**
     * Return the right file name from the given test case code.
     *
     * @param code the test case code to extract the class name from
     * @return the class name extracted from the test case code
     */
    fun getFileNameFromTestCaseCode(code: String): String
}
