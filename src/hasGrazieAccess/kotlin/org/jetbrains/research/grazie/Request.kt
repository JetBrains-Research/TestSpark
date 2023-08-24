package org.jetbrains.research.grazie

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.runBlocking
import org.jetbrains.research.testSpark.grazie.TestGeneration
import org.jetbrains.research.testspark.tools.llm.generation.Request
import org.jetbrains.research.testspark.tools.llm.generation.TestsAssembler

class Request : Request {

    override fun request(
        token: String,
        messages: List<Pair<String, String>>,
        testsAssembler: TestsAssembler
    ): Pair<String, TestsAssembler> {

        val generation = TestGeneration(token)
        var errorMessage = ""

        runBlocking {
            generation.generate(messages).catch {
                errorMessage = it.message.toString()
            }.collect {
                testsAssembler.receiveResponse(it)
            }
        }
        return Pair(errorMessage, testsAssembler)
    }
}
