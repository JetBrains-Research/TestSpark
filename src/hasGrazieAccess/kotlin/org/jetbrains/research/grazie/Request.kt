package org.jetbrains.research.grazie

import kotlinx.coroutines.runBlocking
import org.jetbrains.research.testSpark.grazie.TestGeneration
import org.jetbrains.research.testspark.tools.llm.generation.Request
import org.jetbrains.research.testspark.tools.llm.generation.TestsAssembler

class Request : Request {

    override fun request(
        token: String,
        messages: List<Pair<String, String>>,
        testsAssembler: TestsAssembler
    ): TestsAssembler {

        val generation = TestGeneration(token)

        runBlocking {
            generation.generate(messages).collect {
                testsAssembler.receiveResponse(it)
            }
        }

        return testsAssembler
    }
}
