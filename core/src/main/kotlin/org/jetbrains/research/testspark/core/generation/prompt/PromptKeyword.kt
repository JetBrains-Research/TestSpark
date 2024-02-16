package org.jetbrains.research.testspark.core.generation.prompt


enum class PromptKeyword(val text: String, val description: String, val mandatory: Boolean) {
    NAME("NAME", "The name of the code under test (Class name, method name, line number)", true),
    CODE("CODE", "The code under test (Class, method, or line)", true),
    LANGUAGE("LANGUAGE", "Programming language of the project under test (only Java supported at this point)", true),
    TESTING_PLATFORM(
        "TESTING_PLATFORM",
        "testing platform used in the project (Only JUnit 4 is supported at this point)",
        true,
    ),
    MOCKING_FRAMEWORK(
        "MOCKING_FRAMEWORK",
        "mock framework that can be used in generated test (Only Mockito is supported at this point)",
        false,
    ),
    METHODS("METHODS", "signature of methods used in the code under tests", false),
    POLYMORPHISM("POLYMORPHISM", "polymorphism relations between classes involved in the code under test.", false),
    ;

    fun getOffsets(prompt: String): Pair<Int, Int>? {
        val textToHighlight = "\$$text"
        if (!prompt.contains(textToHighlight)) {
            return null
        }

        val startOffset = prompt.indexOf(textToHighlight)
        val endOffset = startOffset + textToHighlight.length
        return Pair(startOffset, endOffset)
    }
}