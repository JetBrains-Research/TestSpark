package org.jetbrains.research.testspark.tools.llm.generation.grazie

import kotlinx.coroutines.flow.Flow
import org.jetbrains.research.testspark.core.error.Result
import org.jetbrains.research.testspark.core.error.TestSparkError
import org.jetbrains.research.testspark.core.test.TestsAssembler

interface GrazieRequest {
    suspend fun request(
        token: String,
        messages: List<Pair<String, String>>,
        profile: String,
    ): Flow<String>
}
