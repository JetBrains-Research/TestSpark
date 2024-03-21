package org.jetbrains.research.testspark.data

import com.intellij.openapi.module.Module
import com.intellij.psi.PsiClass

data class ProjectContext(
    // The class path of the project.
    var projectClassPath: String? = null,

    // The URL of the file being tested.
    var fileUrlAsString: String? = null,

    // The PsiClass of the class under test
    var cutPsiClass: PsiClass? = null,

    // The full qualified name of the class under test
    var classFQN: String? = null,

    // The module to cut.
    var cutModule: Module? = null,
)
