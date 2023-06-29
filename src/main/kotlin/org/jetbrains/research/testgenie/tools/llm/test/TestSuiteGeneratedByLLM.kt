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
        testCases.forEach { testCase ->
            // Add test annotation
            testFullText += "\t@Test"

            // add expectedException if it exists
            if (testCase.expectedException.isNotBlank()) {
                testFullText += "${testCase.expectedException.replace("@Test", "")})"
            }

            // start writing the test signature
            testFullText += "\n\tpublic void ${testCase.name}() "

            // add throws exception if exists
            if (testCase.throwsException.isNotBlank()) {
                testFullText += "throws ${testCase.throwsException}"
            }

            // start writing the test lines
            testFullText += "{\n"

            // write each line
            testCase.lines.forEach { line ->
                testFullText += when (line.type) {
                    TestLineType.BREAK -> "\t\t\n"
                    else -> "\t\t${line.text}\n"
                }
            }

            // close test case
            testFullText += "\t}\n"
        }

        // close the test class
        testFullText += "}"


        return testFullText
    }
}
