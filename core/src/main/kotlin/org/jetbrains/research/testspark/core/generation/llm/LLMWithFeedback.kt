package org.jetbrains.research.testspark.core.generation.llm

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.research.testspark.core.data.Report
import org.jetbrains.research.testspark.core.generation.llm.network.RequestManager
import org.jetbrains.research.testspark.core.progress.CustomProgressIndicator
import org.jetbrains.research.testspark.core.test.data.TestSuiteGeneratedByLLM

class LLMWithFeedback(
    val report: Report,
    val messageToPrompt: String,
    val requestManager: RequestManager,
    val indicator: CustomProgressIndicator,
) {

    private val log = KotlinLogging.logger { this::class.java }

    fun run(): Unit? {
        var requestsCount = 0
        var generatedTestsArePassing = false

        var generatedTestSuite: TestSuiteGeneratedByLLM? = null

        while (!generatedTestsArePassing) {
            requestsCount++
            log.info { "New iterations of requests" }

            // Process stopped checking
            if (indicator.isCanceled()) {
                return null
            }

            break
        }

        return null
    }
}
