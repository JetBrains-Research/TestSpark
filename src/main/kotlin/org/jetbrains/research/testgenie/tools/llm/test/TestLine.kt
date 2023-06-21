package org.jetbrains.research.testgenie.tools.llm.test

data class TestLine(
    val type: TestLineType,
    val text: String,
)
