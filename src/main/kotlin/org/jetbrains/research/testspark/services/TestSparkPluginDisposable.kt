package org.jetbrains.research.testspark.services

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

/** A disposable to be used as a parent for the TestSpark plugin, instead of the project or application. */
@Service(Service.Level.APP, Service.Level.PROJECT)
class TestSparkPluginDisposable : Disposable {
    companion object {
        fun getInstance(): Disposable = ApplicationManager.getApplication().getService(TestSparkPluginDisposable::class.java)

        fun getInstance(project: Project): Disposable = project.getService(TestSparkPluginDisposable::class.java)
    }

    override fun dispose() {
        // Intentionally left empty, adopted from
        // https://github.com/JetBrains/intellij-community/blob/idea/243.26053.27/python/openapi/src/com/jetbrains/python/PythonPluginDisposable.java
        // which is mentioned in the documentation
        // (https://plugins.jetbrains.com/docs/intellij/disposers.html?from=IncorrectParentDisposable#choosing-a-disposable-parent)
    }
}
