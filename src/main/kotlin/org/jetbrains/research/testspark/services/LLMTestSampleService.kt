package org.jetbrains.research.testspark.services

import com.intellij.openapi.components.Service

@Service(Service.Level.PROJECT)
class LLMTestSampleService {
    private var testSample: String? = null

    fun setTestSample(testSample: String?) {
        this.testSample = testSample
    }

    fun getTestSample(): String = testSample ?: ""
}
