package com.github.mitchellolsthoorn.testgenie.evo

import com.github.mitchellolsthoorn.testgenie.settings.EvoSuiteRuntimeConfiguration
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ScriptRunnerUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import java.nio.charset.Charset


class EvoSuiteRunner {
    companion object {
        fun runEvoSuite(project: Project, projectPath: String, projectClassPath: String, classFQN: String) {
            val evosuiteSettings = EvoSuiteRuntimeConfiguration.getInstance(project);

            val javaPath = "java";// TODO: Source from config
            val evoSuitePath = evosuiteSettings.evoSuiteJarPath;

            val command = arrayOf(
                "-generateSuite",
                "-base_dir",
                projectPath,
                "-class",
                classFQN,
                "-projectCP",
                projectClassPath,
                "-Dserialize_ga",
                "true"
            )

            Thread {
                val cmd = ArrayList<String>()
                cmd.add(javaPath)
                cmd.add("-jar")
                cmd.add(evoSuitePath)
                cmd.addAll(command)

                val generalCommandLine = GeneralCommandLine(cmd)
                generalCommandLine.charset = Charset.forName("UTF-8")
                generalCommandLine.setWorkDirectory(projectPath)

                val processHandler: ProcessHandler = OSProcessHandler(generalCommandLine)
                processHandler.startNotify()

                val output = ScriptRunnerUtil.getProcessOutput(generalCommandLine)
                println(output)

            }.start()
        }
    }
}