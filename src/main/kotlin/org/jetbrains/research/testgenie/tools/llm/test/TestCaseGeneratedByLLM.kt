package org.jetbrains.research.testgenie.tools.llm.test

data class TestCaseGeneratedByLLM(
    var name: String = "",
    var expectedException: String = "",
    var throwsException: String = "",
    var lines: MutableList<TestLine> = mutableListOf()
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TestCaseGeneratedByLLM

        if (name != other.name) return false
        if (expectedException != other.expectedException) return false
        return lines == other.lines
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + expectedException.hashCode()
        result = 31 * result + arrayListOf(lines).hashCode()
        return result
    }
}
