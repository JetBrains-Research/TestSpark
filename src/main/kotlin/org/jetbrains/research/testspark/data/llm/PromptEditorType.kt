package org.jetbrains.research.testspark.data.llm

enum class PromptEditorType(val text: String, val index: Int) {
    CLASS("Class", 0),
    METHOD("Method", 1),
    LINE("Line", 2),
}
