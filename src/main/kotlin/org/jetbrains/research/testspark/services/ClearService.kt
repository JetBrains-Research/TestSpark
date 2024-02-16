package org.jetbrains.research.testspark.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.data.DataFilesUtil

@Service(Service.Level.PROJECT)
class ClearService {
    /**
     * Clears the given project's test-related data, including test case display,
     * error service, coverage visualization, and test generation data.
     *
     * @param project the project to clear the test-related data for
     */
    fun clear(project: Project) {
        project.service<TestCaseDisplayService>().clear()
        project.service<ErrorService>().clear()
        project.service<CoverageVisualisationService>().clear()
        project.service<TestGenerationDataService>().clear()
        project.service<TestsExecutionResultService>().clear()
        DataFilesUtil.cleanFolder(project.service<ProjectContextService>().resultPath!!)
    }
}
