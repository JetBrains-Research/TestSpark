package org.jetbrains.research.testspark.services

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ScriptRunnerUtil
import com.intellij.openapi.project.Project
import java.io.File

class CommandLineService(private val project: Project) {
    private val sep = File.separatorChar

    /**
     * Executes a command line process and returns the output as a string.
     *
     * @param cmd The command line arguments as an ArrayList of strings.
     * @return The output of the command line process as a string.
     */
    fun runCommandLine(cmd: ArrayList<String>): String {
        val compilationProcess = GeneralCommandLine(cmd)
        return ScriptRunnerUtil.getProcessOutput(compilationProcess, ScriptRunnerUtil.STDERR_OUTPUT_KEY_FILTER, 30000)
    }

    /**
     * Generates the path for the command by concatenating the necessary paths.
     *
     * @param buildPath The path of the build file.
     * @return The generated path as a string.
     */
    fun getPath(buildPath: String): String {
        // create the path for the command
        val pluginsPath = System.getProperty("idea.plugins.path")
        val junitPath = "$pluginsPath${sep}TestSpark${sep}lib${sep}junit-4.13.jar"
        val mockitoPath = "$pluginsPath${sep}TestSpark${sep}lib${sep}mockito-core-5.0.0.jar"
        val hamcrestPath = "$pluginsPath${sep}TestSpark${sep}lib${sep}hamcrest-core-1.3.jar"
        return "$junitPath:$hamcrestPath:$mockitoPath:$buildPath"
    }

    /**
     * Retrieves the absolute path of the specified library.
     *
     * @param libraryName the name of the library
     * @return the absolute path of the library
     */
    fun getLibrary(libraryName: String): String {
        val pluginsPath = System.getProperty("idea.plugins.path")
        return "$pluginsPath${sep}TestSpark${sep}lib${sep}$libraryName"
    }
}
