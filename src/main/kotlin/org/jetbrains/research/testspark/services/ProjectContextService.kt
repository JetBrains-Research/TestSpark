package org.jetbrains.research.testspark.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.module.Module
import com.intellij.psi.PsiClass

@Service(Service.Level.PROJECT)
class ProjectContextService {
    // The class path of the project.
    var projectClassPath: String? = null

    // The path to save the generated test results.
    var resultPath: String? = null

    // The base directory of the project.
    var baseDir: String? = null

    // The URL of the file being tested.
    var fileUrl: String? = null

    var cutPsiClass: PsiClass? = null

    var classFQN: String? = null

    // The module to cut.
    var cutModule: Module? = null
}
