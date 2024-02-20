package org.jetbrains.research.testspark.appstarter

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationStarter
import kotlin.system.exitProcess

class TestSparkStarter: ApplicationStarter {
    @Deprecated("Specify it as `id` for extension definition in a plugin descriptor")
    override val commandName: String ="testspark"

    override fun main(args: List<String>) {
        val projectPath = args[1]
//
        println("Test generation requested for $projectPath")
        val project = ProjectUtil.openOrImport(projectPath, null, true) ?: run {
            println("couldn't find project in $projectPath")
            exitProcess(1)
        }

        println("Detected project: ${project}")
    }
}