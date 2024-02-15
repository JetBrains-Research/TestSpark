package org.jetbrains.research.testspark.appstarter

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationStarter
import kotlin.system.exitProcess

class TestSparkStarter: ApplicationStarter {
    @Deprecated("Specify it as `id` for extension definition in a plugin descriptor")
    override val commandName: String ="testspark"

    override fun main(args: List<String>) {
        println("Hello from TestSpark CLI")
//        println("Hello from TestSpark CLI");
//        val projectPath = args[1]
//
//
//        val project = ProjectUtil.openOrImport(projectPath, null, true) ?: run {
//            println("couldn't find project in $projectPath")
//            exitProcess(1)
//        }
    }
}