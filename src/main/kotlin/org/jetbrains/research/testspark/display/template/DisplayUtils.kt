package org.jetbrains.research.testspark.display.template

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.jetbrains.research.testspark.data.UIContext
import org.jetbrains.research.testspark.langwrappers.PsiClassWrapper

interface DisplayUtils {
    fun applyTests(project: Project, uiContext: UIContext?, testCaseComponents: List<String>)

    fun appendTestsToClass(
        project: Project,
        uiContext: UIContext?,
        testCaseComponents: List<String>,
        selectedClass: PsiClassWrapper,
        outputFile: PsiFile,
    )
}
