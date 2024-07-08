package org.jetbrains.research.testspark.core.generation.llm.prompt

enum class PromptKeyword(val text: String, val description: String, val mandatory: Boolean) {
    NAME("NAME", "The name of the code under test (Class name, method name, line number)", true),
    CODE("CODE", "The code under test (Class, method, or line)", true),
    LANGUAGE("LANGUAGE", "Programming language of the project under test (only Java supported at this point)", true),
    TESTING_PLATFORM(
        "TESTING_PLATFORM",
        "Testing platform used in the project (Only JUnit 4 is supported at this point)",
        true,
    ),
    MOCKING_FRAMEWORK(
        "MOCKING_FRAMEWORK",
        "Mock framework that can be used in generated test (Only Mockito is supported at this point)",
        false,
    ),
    METHODS("METHODS", "Signature of methods used in the code under tests", false),
    POLYMORPHISM("POLYMORPHISM", "Polymorphism relations between classes involved in the code under test", false),
    TEST_SAMPLE("TEST_SAMPLE", "Test samples for LLM for test generation", false),
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

    // TODO: replace all "\$$" with use of this `PromptKeyword.variable`
    /**
     * Provides variables from the underlying keyword.
     */
    val variable: String
        get() = "\$${this.text}"
}
