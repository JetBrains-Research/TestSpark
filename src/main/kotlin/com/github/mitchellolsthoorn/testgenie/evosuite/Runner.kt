package com.github.mitchellolsthoorn.testgenie.evosuite

import com.github.mitchellolsthoorn.testgenie.settings.TestGenieSettingsService
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.*
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.io.FileUtilRt
import java.io.File
import java.nio.charset.Charset


class Runner {

    companion object {
        private val log = Logger.getInstance(Companion::class.java)

        fun runEvoSuiteForClass(projectPath: String, projectClassPath: String, classFQN: String): String {

            val javaPath = "java"// TODO: Source from config

            val pluginsPath = System.getProperty("idea.plugins.path")
            val evoSuitePath = "$pluginsPath/TestGenie/lib/evosuite.jar"

            val ts = System.currentTimeMillis()
            val sep = File.separatorChar
            val testResultDirectory = "${FileUtilRt.getTempDirectory()}${sep}testGenieResults${sep}"

            val testResultName = "test_gen_result_$ts"
            val serializeResultPath = "\"$testResultDirectory$testResultName\""

            val settingsState = TestGenieSettingsService.getInstance().state

            var command = SettingsArguments(projectClassPath, projectPath, serializeResultPath, classFQN).build()

            if (!settingsState?.seed.isNullOrBlank()) command.add("-seed=${settingsState?.seed}")
            if (!settingsState?.configurationId.isNullOrBlank()) command.add("-Dconfiguration_id=${settingsState?.configurationId}")

            val cmd = ArrayList<String>()
            cmd.add(javaPath)
            cmd.add("-jar")
            cmd.add(evoSuitePath)
            cmd.addAll(command)

            val cmdString = cmd.fold(String()) { acc, e -> acc.plus(e).plus(" ") }

            log.info("Starting EvoSuite with arguments: $cmdString")
            log.info("Results will be saved to $serializeResultPath")

            runEvoSuite(cmd, projectPath)

            return testResultName

        }

        fun runEvoSuiteForMethod(
            projectPath: String,
            projectClassPath: String,
            classFQN: String,
            method: String
        ): String {

            val javaPath = "java"// TODO: Source from config

            val pluginsPath = System.getProperty("idea.plugins.path")
            val evoSuitePath = "$pluginsPath/TestGenie/lib/evosuite.jar"

            val ts = System.currentTimeMillis()
            val sep = File.separatorChar
            val testResultDirectory = "${FileUtilRt.getTempDirectory()}${sep}testGenieResults${sep}"

            val testResultName = "test_gen_result_$ts"
            val serializeResultPath = "\"$testResultDirectory$testResultName\""

            val settingsState = TestGenieSettingsService.getInstance().state

            var command =
                SettingsArguments(projectClassPath, projectPath, serializeResultPath, classFQN).forMethodPrefix(method)
                    .build()

            if (!settingsState?.seed.isNullOrBlank()) command.add("-seed=${settingsState?.seed}")
            if (!settingsState?.configurationId.isNullOrBlank()) command.add("-Dconfiguration_id=${settingsState?.configurationId}")

            val cmd = ArrayList<String>()
            cmd.add(javaPath)
            cmd.add("-jar")
            cmd.add(evoSuitePath)
            cmd.addAll(command)

            val cmdString = cmd.fold(String()) { acc, e -> acc.plus(e).plus(" ") }

            log.info("Starting EvoSuite with arguments: $cmdString")
            log.info("Results will be saved to $serializeResultPath")

            runEvoSuite(cmd, projectPath)

            return testResultName
        }


        private fun runEvoSuite(cmd: ArrayList<String>, workDir: String) {
            val evoSuiteProcessTimeout: Long = 12000000 // TODO: Source from config

            Thread {
                val evoSuiteProcess = GeneralCommandLine(cmd)
                evoSuiteProcess.charset = Charset.forName("UTF-8")
                evoSuiteProcess.setWorkDirectory(workDir)

                val handler: ProcessHandler = OSProcessHandler(evoSuiteProcess)

                val outputBuilder = StringBuilder()

                // attach process listener for output
                handler.addProcessListener(object : ProcessAdapter() {
                    override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                        val text = event.text
                        outputBuilder.append(text)
                        log.info(text)
                    }
                })

                handler.startNotify()

                if (!handler.waitFor(evoSuiteProcessTimeout)) {
                    log.error("EvoSuite process exceeded timeout - ${evoSuiteProcessTimeout}ms")
                }

                // TODO: handle stderr separately
//                if (stderr.isNotEmpty()) {
//                    log.error("EvoSuite process exited with error - $stderr")
//                }

            }.start()
        }
    }
}