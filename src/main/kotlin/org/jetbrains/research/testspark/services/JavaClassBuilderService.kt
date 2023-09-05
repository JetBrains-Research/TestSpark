package org.jetbrains.research.testspark.services

import com.intellij.openapi.project.Project

class JavaClassBuilderService(private val project: Project) {
    /**
     * Generates the code for a test class.
     *
     * @param className the name of the test class
     * @param body the body of the test class
     * @return the generated code as a string
     */
    fun generateCode(
        className: String,
        body: String,
        imports: Set<String>,
        packageString: String,
        runWith: String,
        otherInfo: String,
    ): String {
        var testFullText = printUpperPart(className, imports, packageString, runWith, otherInfo)

        // Add each test (exclude expected exception)
        testFullText += body

        // close the test class
        testFullText += "}"

        return testFullText
    }

    /**
     * Returns the upper part of test suite (package name, imports, and test class name) as a string.
     *
     * @return the upper part of test suite (package name, imports, and test class name) as a string.
     */
    private fun printUpperPart(
        className: String,
        imports: Set<String>,
        packageString: String,
        runWith: String,
        otherInfo: String,
    ): String {
        var testText = ""

        // Add package
        if (packageString.isNotBlank()) {
            testText += "package $packageString;\n"
        }

        // add imports
        imports.forEach { importedElement ->
            testText += "$importedElement\n"
        }

        testText += "\n"

        // add runWith if exists
        if (runWith.isNotBlank()) {
            testText += "@RunWith($runWith)\n"
        }
        // open the test class
        testText += "public class $className{\n\n"

        // Add other presets (annotations, non-test functions)
        if (otherInfo.isNotBlank()) {
            testText += otherInfo
        }

        return testText
    }
}
