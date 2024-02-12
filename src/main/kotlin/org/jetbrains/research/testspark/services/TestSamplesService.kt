package org.jetbrains.research.testspark.services

import com.intellij.openapi.components.Service

@Service(Service.Level.PROJECT)
class TestSamplesService {
    val testSamples = mutableListOf<String>()
    val selectedTestSamples = mutableListOf<String>()

    fun clear() {
        testSamples.clear()
        selectedTestSamples.clear()
    }
}
