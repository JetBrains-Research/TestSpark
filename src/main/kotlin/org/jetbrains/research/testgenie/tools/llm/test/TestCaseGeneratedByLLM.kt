package org.jetbrains.research.testgenie.tools.llm.test

data class TestCaseGeneratedByLLM(
    var name: String = "",
    var expectedException: String = "",
    var throwsException: String = "",
    var lines: MutableList<TestLine> = mutableListOf(),
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TestCaseGeneratedByLLM

        if (name != other.name) return false
        if (expectedException != other.expectedException) return false
        return lines == other.lines
    }

    fun isEmpty(): Boolean {
        return (lines.size == 0)
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + expectedException.hashCode()
        result = 31 * result + arrayListOf(lines).hashCode()
        return result
    }

    override fun toString(): String {
        var testFullText = ""

        // Add test annotation
        testFullText += "\t@Test"

        // add expectedException if it exists
        if (expectedException.isNotBlank()) {
            testFullText += "${expectedException.replace("@Test", "")})"
        }

        // start writing the test signature
        testFullText += "\n\tpublic void $name() "

        // add throws exception if exists
        if (throwsException.isNotBlank()) {
            testFullText += "throws $throwsException"
        }

        // start writing the test lines
        testFullText += "{\n"

        // write each line
        lines.forEach { line ->
            testFullText += when (line.type) {
                TestLineType.BREAK -> "\t\t\n"
                else -> "\t\t${line.text}\n"
            }
        }

        // close test case
        testFullText += "\t}\n"

        return testFullText
    }

    fun reformat() {
        for (index in lines.indices.reversed()) {
            if (lines[index].type == TestLineType.BREAK) {
                lines.removeAt(index)
            } else {
                break
            }
        }
    }
}
