package org.jetbrains.research.testspark.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import java.io.BufferedReader
import java.io.InputStreamReader

@Service(Service.Level.PROJECT)
class RunCommandLineService(private val project: Project) {

    /**
     * Executes a command line process and returns the output as a string.
     *
     * @param cmd The command line arguments as an ArrayList of strings.
     * @return The output of the command line process as a string.
     */
    fun runCommandLine(cmd: ArrayList<String>): String {
        var errorMessage = ""

        val process = ProcessBuilder()
            .command("bash", "-c", cmd.joinToString(" "))
            .redirectErrorStream(true)
            .start()

        val reader = BufferedReader(InputStreamReader(process.inputStream))
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            errorMessage += line
        }

        process.waitFor()

        return errorMessage
    }
}
