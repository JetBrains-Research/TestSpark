package org.jetbrains.research.testgenie.tools.llm.test

data class TestCaseGeneratedByLLM(
    val name: String = "",
    val expectedException: String = "",
    val lines: Array<TestLine> = emptyArray()
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TestCaseGeneratedByLLM

        if (name != other.name) return false
        if (expectedException != other.expectedException) return false
        return lines.contentEquals(other.lines)
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + expectedException.hashCode()
        result = 31 * result + lines.contentHashCode()
        return result
    }
}
