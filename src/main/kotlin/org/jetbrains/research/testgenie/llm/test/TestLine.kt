package org.jetbrains.research.testgenie.llm.test

data class TestLine(
    val type: TestLineType,
    val text: String
) {
}