package org.jetbrains.research.testspark.core

import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.writeText

class ProjectUnderTestFileCreator {
    companion object {
        var projectUnderTestOutputDirectory: String? = null
        val llmOutputFile get() = getOrCreateFileInOutputDirectory("llm.txt")

        private const val FILE_CONTENT_SEPARATOR = "\n===========================================================================\n"
        private const val STATUS_PREFIX = ">>>>>"
        var tries: Int = 0

        fun getOrCreateFileInOutputDirectory(filename: String): Path {
            val root =
                if (projectUnderTestOutputDirectory == null) {
                    System.getProperty("user.dir") + "/output"
                } else {
                    projectUnderTestOutputDirectory
                }

            val filepath = Path.of("$root/generated-artifacts/$filename")
            // Create the parent directories if they don't exist
            val parentDir = filepath.toFile().parentFile
            parentDir.mkdirs()
            // Create the file
            filepath.toFile().createNewFile()
            return filepath
        }

        private fun appendToFile(
            content: String,
            filepath: Path,
        ) {
            filepath.writeText(content, options = arrayOf(StandardOpenOption.APPEND))
        }

        private fun addSeparator(filepath: Path) {
            appendToFile(FILE_CONTENT_SEPARATOR, filepath)
        }

        private fun addMessage(
            content: String,
            type: String,
        ) {
            appendToFile("$STATUS_PREFIX $type $tries\n", llmOutputFile)
            appendToFile(content, llmOutputFile)
            addSeparator(llmOutputFile)
        }

        fun addPrompt(content: String) {
            addMessage(content, "PROMPT")
        }

        fun addError(content: String) {
            addMessage(content, "ERROR")
            tries++
        }

        fun addResponse(content: String) {
            addMessage(content, "RESPONSE")
            tries++
        }

        /**
         * Appends the given content to a log file into the directory of the project under test.
         *
         * @param content the content to be appended to the log file
         * @param alsoPrint if true then the content will be printed in the console as well
         */
        fun log(
            content: String,
            alsoPrint: Boolean = true,
        ) {
            val logFilepath = getOrCreateFileInOutputDirectory("test-generation.log")
            appendToFile(content + "\n", logFilepath)
            if (alsoPrint) {
                println(content)
            }
        }
    }
}
