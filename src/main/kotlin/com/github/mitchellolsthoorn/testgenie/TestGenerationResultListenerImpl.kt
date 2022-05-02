package com.github.mitchellolsthoorn.testgenie

import com.github.mitchellolsthoorn.testgenie.evo.TestGenerationResultListener
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.evosuite.utils.CompactReport

class TestGenerationResultListenerImpl(val project: Project) : TestGenerationResultListener {
    private val log = Logger.getInstance(this.javaClass)

    override fun testGenerationResult(test: CompactReport) {
        log.info("Received test result for " + test.UUT)
    }
}