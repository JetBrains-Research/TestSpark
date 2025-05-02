package org.jetbrains.research.grazie

import kotlinx.coroutines.flow.Flow
import org.jetbrains.research.testSpark.grazie.TestGeneration
import org.jetbrains.research.testspark.tools.llm.generation.grazie.GrazieRequest

class Request : GrazieRequest {
    override suspend fun request(
        token: String,
        messages: List<Pair<String, String>>,
        profile: String,
    ): Flow<String> {
        val generation = TestGeneration(token)
        return generation.generate(messages, profile)
    }
}
