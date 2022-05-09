package com.github.mitchellolsthoorn.testgenie.evosuite

import com.github.mitchellolsthoorn.testgenie.settings.TestGenieSettingsService
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.io.FileUtilRt
import java.io.File
import java.nio.charset.Charset

/**
 * A utility class that runs evosuite as a separate process in its various
 * modes of operation.
 *
 * @param projectPath The root of the project we're testing, this sets the working dir of the evosuite process
 * @param projectClassPath Path to the class path containing the compiled classes. This will change according to
 * build system (e.g. Maven target/classes or Gradle build/classes)
 * @param classFQN Fully qualified name of the class under test
 */
class Runner(private val projectPath: String, private val projectClassPath: String, private val classFQN: String) {
    private val log = Logger.getInstance(this::class.java)

    private val evoSuiteProcessTimeout: Long = 12000000 // TODO: Source from config
    private val javaPath = "java" // TODO: Source from config
    private val evosuiteVersion = "1.0.2" // TODO: Figure out a better way to source this

    private val pluginsPath = System.getProperty("idea.plugins.path")
    private var evoSuitePath = "$pluginsPath/TestGenie/lib/evosuite-$evosuiteVersion.jar"

    private val ts = System.currentTimeMillis()
    private val sep = File.separatorChar
    private val testResultDirectory = "${FileUtilRt.getTempDirectory()}${sep}testGenieResults$sep"
    private val testResultName = "test_gen_result_$ts"
    private val serializeResultPath = "\"$testResultDirectory$testResultName\""

    private val settingsState = TestGenieSettingsService.getInstance().state

    private var command = mutableListOf<String>()

    /**
     * Sets up evosuite to run for a target class. This is the simplest configuration.
     */
    fun forClass(): Runner {
        command = SettingsArguments(projectClassPath, projectPath, serializeResultPath, classFQN).build()
        return this
    }

    /**
     * Sets up evosuite to run for a target method of the target class. This attaches a method prefix argument
     * to the evosuite process.
     */
    fun forMethod(method: String): Runner {
        command = SettingsArguments(projectClassPath, projectPath, serializeResultPath, classFQN).forMethodPrefix(method)
            .build()
        return this
    }

    /**
     * Performs final argument preparation and launches the evosuite process on a separate thread.
     *
     * @return the path to which results will be (eventually) saved
     */
    fun runEvoSuite(): String {
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

        Thread {
            val evoSuiteProcess = GeneralCommandLine(cmd)
            evoSuiteProcess.charset = Charset.forName("UTF-8")
            evoSuiteProcess.setWorkDirectory(projectPath)

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
        }.start()

        return testResultName
    }
}
