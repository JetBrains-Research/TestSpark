package org.jetbrains.research.testspark.tools.llm.generation.grazie

import org.jetbrains.research.testspark.core.test.TestsAssembler

interface GrazieRequest {

    fun request(
        token: String,
        messages: List<Pair<String, String>>,
        profile: String,
        testsAssembler: TestsAssembler,
    ): String
}
