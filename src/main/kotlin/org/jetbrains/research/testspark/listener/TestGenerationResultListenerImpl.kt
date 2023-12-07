package org.jetbrains.research.testspark.listener

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.data.Report
import org.jetbrains.research.testspark.editor.Workspace
import org.jetbrains.research.testspark.tools.TestGenerationResultListener

class TestGenerationResultListenerImpl(private val project: Project) : TestGenerationResultListener {
    private val log = Logger.getInstance(this.javaClass)

    override fun testGenerationResult(testReport: Report, resultName: String, fileUrl: String) {
        log.info("Received test result for $resultName")
        val workspace = project.service<Workspace>()

        ApplicationManager.getApplication().invokeLater {
            workspace.receiveGenerationResult(resultName, testReport)
        }
    }
}
