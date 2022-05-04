package com.github.mitchellolsthoorn.testgenie.evo

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ScriptRunnerUtil
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.io.FileUtilRt
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Path
import java.nio.file.Paths


class EvoSuiteRunner {

    companion object {
        private val log = Logger.getInstance(Companion::class.java)

        fun runEvoSuite(projectPath: String, projectClassPath: String, classFQN: String): String {

            val pluginsPath = System.getProperty("idea.plugins.path");

            val javaPath = "java";// TODO: Source from config
            val evoSuitePath = "$pluginsPath/TestGenie/lib/evosuite.jar"

            val ts = System.currentTimeMillis()
            val sep = File.separatorChar
            val testResultDirectory = "${FileUtilRt.getTempDirectory()}${sep}testGenieResults${sep}"

            val testResultName = "test_gen_result_$ts"
            val serializeResultPath = "\"$testResultDirectory$testResultName\""

            val command = arrayOf(
                "-generateSuite",
                "-serializeResult",
                "-serializeResultPath", serializeResultPath,
                "-base_dir", projectPath, // Working directory for evosuite
                "-class", classFQN, // class FQN inside the project classpath of the class we're generating tests for
                "-projectCP", projectClassPath, // class path of the project we're generating tests for
                "-Djunit_tests=true", // disable writing to 'evosuite-tests' in working directory
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
                log.info("Results will be saved to $serializeResultPath")

                val generalCommandLine = GeneralCommandLine(cmd)
                generalCommandLine.charset = Charset.forName("UTF-8")
                generalCommandLine.setWorkDirectory(projectPath)

                val processHandler: ProcessHandler = OSProcessHandler(generalCommandLine)

                processHandler.startNotify()

                val stderr = ScriptRunnerUtil.getProcessOutput(
                    generalCommandLine,
                    ScriptRunnerUtil.STDERR_OUTPUT_KEY_FILTER, 12000000
                )

                if (stderr.isNotEmpty()) {
                    log.error("Process output: $stderr")
                }

            }.start()

            return testResultName
        }
    }
}