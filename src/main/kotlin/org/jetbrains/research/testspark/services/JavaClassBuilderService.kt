package org.jetbrains.research.testspark.services

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import com.intellij.openapi.project.Project
import java.util.Locale

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

        testFullText.replace("\r\n", "\n")

        return Regex("\n\n\n(\n)*").replace(testFullText, "\n\n")
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

    /**
     * Returns the generated class name for a given test case.
     *
     * @param testCaseName The test case name.
     * @return The generated class name as a string.
     */
    fun getClassWithTestCaseName(testCaseName: String): String {
        val className = testCaseName.replaceFirstChar {
            if (it.isLowerCase()) {
                it.titlecase(Locale.getDefault())
            } else {
                it.toString()
            }
        }
        return "Generated$className"
    }

    /**
     * Finds the test method from a given class with the specified test case name.
     *
     * @param code The code of the class containing test methods.
     * @return The test method as a string, including the "@Test" annotation.
     */
    fun getTestMethodCodeFromClassWithTestCase(code: String): String {
        var result = ""
        val componentUnit: CompilationUnit = StaticJavaParser.parse(code)

        object : VoidVisitorAdapter<Any?>() {
            override fun visit(method: MethodDeclaration, arg: Any?) {
                super.visit(method, arg)
                if (method.getAnnotationByName("Test").isPresent) {
                    result += "\t" + method.toString().replace("\n", "\n\t") + "\n\n"
                }
            }
        }.visit(componentUnit, null)

        return result
    }

    /**
     * Retrieves the name of the test method from a given Java class with test cases.
     *
     * @param code The source code of the Java class with test cases.
     * @return The name of the test method. If no test method is found, an empty string is returned.
     */
    fun getTestMethodNameFromClassWithTestCase(code: String): String {
        var result = ""
        val componentUnit: CompilationUnit = StaticJavaParser.parse(code)

        object : VoidVisitorAdapter<Any?>() {
            override fun visit(method: MethodDeclaration, arg: Any?) {
                super.visit(method, arg)
                if (method.getAnnotationByName("Test").isPresent) {
                    result = method.nameAsString
                }
            }
        }.visit(componentUnit, null)

        return result
    }
}
