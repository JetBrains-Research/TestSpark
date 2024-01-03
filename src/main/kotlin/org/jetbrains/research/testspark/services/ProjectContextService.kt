package org.jetbrains.research.testspark.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.module.Module
import com.intellij.psi.PsiClass

@Service(Service.Level.PROJECT)
class ProjectContextService {
    var editor: Editor? = null

    // The class path of the project.
    var projectClassPath: String? = null

    // The path to save the generated test results.
    var resultPath: String? = null

    // The base directory of the project.
    var baseDir: String? = null

    // The URL of the file being tested.
    var fileUrl: String? = null

    var cutPsiClass: PsiClass? = null

    // The module to cut.
    var cutModule: Module? = null

    // The fully qualified name of the class being tested.
    var classFQN: String? = null
}
