package org.jetbrains.research.testspark.testmanager.template

import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.core.data.TestGenerationData

/**
 * Interface for generating and formatting test class code.
 */
interface TestGenerator {
    /**
     * Generates the code for a test class.
     *
     * @param project the current project
     * @param className the name of the test class
     * @param body the body of the test class
     * @param imports the set of imports needed in the test class
     * @param packageString the package declaration of the test class
     * @param runWith the runWith annotation for the test class
     * @param otherInfo any other additional information for the test class
     * @param testGenerationData the data used for test generation
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
     * Formats the given Java code using IntelliJ IDEA's code formatting rules.
     *
     * @param project the current project
     * @param code the Java code to be formatted
     * @param generatedTestData the data used for generating the test
     * @return the formatted Java code
     */
    fun formatCode(project: Project, code: String, generatedTestData: TestGenerationData): String
}
