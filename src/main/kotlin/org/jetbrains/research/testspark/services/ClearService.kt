package org.jetbrains.research.testspark.services

import com.intellij.openapi.components.Service

@Service(Service.Level.PROJECT)
class ClearService {
    /**
     * Clears the given project's test-related data, including test case display,
     * error service, coverage visualization, and test generation data.
     *
     * @param project the project to clear the test-related data for
//     */
//    fun clear(project: Project) { // should be removed totally!
//        project.service<TestCaseDisplayService>().clear()
//        project.service<ErrorService>().clear()
//        project.service<CoverageVisualisationService>().clear()
//        project.service<TestGenerationData>().clear()
//        project.service<TestsExecutionResultService>().clear()
//        DataFilesUtil.cleanFolder(project.service<ProjectContextService>().resultPath!!)
//    }
}
