package org.jetbrains.research.testgenie.tools.llm.test

data class TestSuiteGeneratedByLLM(
    var imports: Set<String> = emptySet(),
    var packageString: String = "",
    var testCases: MutableList<TestCaseGeneratedByLLM> = mutableListOf(),
) {

    fun isEmpty(): Boolean {
        return testCases.isEmpty()
    }

    override fun toString(): String{
        var testFullText = ""

        // Add package
        if(packageString.isNotBlank()){
            testFullText += "package $packageString;\n"
        }

        // add imports
        imports.forEach{ importedElement ->
            testFullText += "$importedElement\n"
        }

        // open the test class
        testFullText += "public class GeneratedTest{\n\n"

        // Add each test
        testCases.forEach { testCase -> testFullText += "$testCase\n" }

        // close the test class
        testFullText += "}"

        return testFullText
    }

    fun getPrintablePackageString(): String{
        return when{
            packageString.isEmpty() || packageString.isBlank() -> ""
            else -> "$packageString."
        }
    }
}
