package org.jetbrains.research.testspark.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import org.jetbrains.research.testspark.core.utils.CommandLineRunner

@Service(Service.Level.PROJECT)
class RunCommandLineService(private val project: Project) {
    fun runCommandLine(cmd: ArrayList<String>): String {
        return CommandLineRunner.run(cmd)
    }
}
