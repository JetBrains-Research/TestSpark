package org.jetbrains.research.grazie

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.runBlocking
import org.jetbrains.research.testSpark.grazie.TestGeneration
import org.jetbrains.research.testspark.core.test.TestsAssembler
import org.jetbrains.research.testspark.tools.llm.generation.grazie.GrazieRequest

class Request : GrazieRequest {

    override fun request(
        token: String,
        messages: List<Pair<String, String>>,
        profile: String,
        testsAssembler: TestsAssembler,
    ): String {
        val generation = TestGeneration(token)
        var errorMessage = ""
        runBlocking {
            generation.generate(messages, profile).catch {
                errorMessage = it.message.toString()
            }.collect {
                testsAssembler.consume(it)
            }
        }
        return errorMessage
    }
}
