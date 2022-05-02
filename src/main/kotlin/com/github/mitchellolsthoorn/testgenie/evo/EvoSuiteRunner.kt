package com.github.mitchellolsthoorn.testgenie.evo

import com.github.mitchellolsthoorn.testgenie.settings.EvoSuiteRuntimeConfiguration
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ScriptRunnerUtil
import com.intellij.openapi.diagnostic.Logger
import java.io.FileInputStream
import java.io.ObjectInputStream
import java.nio.charset.Charset


class EvoSuiteRunner {

    companion object {
        private val log = Logger.getInstance(Companion::class.java)

        fun runEvoSuite(projectPath: String, projectClassPath: String, classFQN: String) {

            val evosuiteSettings = EvoSuiteRuntimeConfiguration.getInstance();

            val javaPath = "java";// TODO: Source from config
            val evoSuitePath = evosuiteSettings.state.evoSuiteJarPath;

            val command = arrayOf(
                "-generateSuite",
                "-serializeResult",
                "-serializeResultPath", "/tmp/mincho",
                "-base_dir", projectPath, // Working directory for evosuite
                "-class", classFQN, // class FQN inside the project classpath of the class we're generating tests for
                "-projectCP", projectClassPath, // class path of the project we're generating tests for
                "-Djunit_tests=false", // disable writing to 'evosuite-tests' in working directory
                "-Dnew_statistics=false" //disable writing to 'evosuite-report' in working directory
            )

            Thread {
                val cmd = ArrayList<String>()
                cmd.add(javaPath)
                cmd.add("-jar")
                cmd.add(evoSuitePath)
                cmd.addAll(command)

                val cmdString = cmd.fold(String()) { acc, e -> acc.plus(e).plus(" ") }

                log.info("Starting EvoSuite with arguments: $cmdString")

                val generalCommandLine = GeneralCommandLine(cmd)
                generalCommandLine.charset = Charset.forName("UTF-8")
                generalCommandLine.setWorkDirectory(projectPath)

                val processHandler: ProcessHandler = OSProcessHandler(generalCommandLine)

                processHandler.startNotify()

                val output = ScriptRunnerUtil.getProcessOutput(
                    generalCommandLine,
                    ScriptRunnerUtil.STDOUT_OR_STDERR_OUTPUT_KEY_FILTER, 12000000
                )
                println("Process output: $output")

            }.start()
        }
    }
}