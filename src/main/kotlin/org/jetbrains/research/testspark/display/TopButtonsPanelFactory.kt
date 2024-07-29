package org.jetbrains.research.testspark.display

import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.core.test.SupportedLanguage

class TopButtonsPanelFactory(private val project: Project) {
    fun create(language: SupportedLanguage): TestSuiteView {
        return when (language) {
            SupportedLanguage.Java -> JavaTestSuiteView(project)
            SupportedLanguage.Kotlin -> KotlinTestSuiteView(project)
        }
    }
}
