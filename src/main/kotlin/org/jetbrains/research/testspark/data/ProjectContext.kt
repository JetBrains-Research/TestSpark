package org.jetbrains.research.testspark.data

import com.intellij.openapi.module.Module
import com.intellij.psi.PsiClass

data class ProjectContext(
    // The class path of the project.
    var projectClassPath: String? = null,

    // The URL of the file being tested.
    var fileUrl: String? = null,

    var cutPsiClass: PsiClass? = null,

    var classFQN: String? = null,

    // The module to cut.
    var cutModule: Module? = null,
)
