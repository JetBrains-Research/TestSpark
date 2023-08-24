package org.jetbrains.research.testspark.tools.llm.generation

interface Request {

    fun request(
        token: String,
        messages: List<Pair<String, String>>,
        testsAssembler: TestsAssembler
    ): Pair<String, TestsAssembler>
}
