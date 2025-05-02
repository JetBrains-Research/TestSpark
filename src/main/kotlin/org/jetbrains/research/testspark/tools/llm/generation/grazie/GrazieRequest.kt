package org.jetbrains.research.testspark.tools.llm.generation.grazie

import kotlinx.coroutines.flow.Flow

interface GrazieRequest {
    suspend fun request(
        token: String,
        messages: List<Pair<String, String>>,
        profile: String,
    ): Flow<String>
}
