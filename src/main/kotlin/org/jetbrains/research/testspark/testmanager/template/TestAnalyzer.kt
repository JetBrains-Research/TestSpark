package org.jetbrains.research.testspark.testmanager.template

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.research.testspark.core.test.data.TestSample

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
    fun extractFirstTestMethodName(
        oldTestCaseName: String,
        classCode: String,
    ): String

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

    /**
     * Extracts a list of test samples from the given project and file.
     * This method analyzes the provided file in the context of the project to identify relevant test samples.
     *
     * @param project The current project where the file is located.
     * @param file The virtual file to be analyzed for extracting test samples.
     */
    fun getTestSamplesList(
        project: Project,
        file: VirtualFile,
    ): List<TestSample>

    /**
     * Creates a visual representation of a method reference in the format "ClassName#MethodName".
     *
     * @param className The name of the class containing the method.
     * @param methodName The name of the method.
     * @return A string representation of the method reference enclosed in HTML tags.
     */
    fun createHtmlMethodName(
        className: String?,
        methodName: String?,
    ): String {
        var htmlMethodName = "<html>"
        htmlMethodName += className ?: "unnamed"
        htmlMethodName += "#"
        htmlMethodName += methodName ?: "unnamed"
        htmlMethodName += "</html>"
        return htmlMethodName
    }

    /**
     * Checks if the given file is of a supported type.
     *
     * @param file The virtual file to be checked.
     */
    fun isSupportedFileType(file: VirtualFile): Boolean
}
