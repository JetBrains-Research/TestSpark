package org.jetbrains.research.testspark.core

import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.writeText

class ProjectUnderTestFileCreator {
    companion object {
        var projectUnderTestOutputDirectory: String? = null

        fun getOrCreateFileInOutputDirectory(filename: String): Path {
            val filepath = Path.of("${projectUnderTestOutputDirectory!!}/generated-artifacts/$filename")
            // Create the parent directories if they don't exist
            val parentDir = filepath.toFile().parentFile
            parentDir.mkdirs()
            // Create the file
            filepath.toFile().createNewFile()
            return filepath
        }

        fun appendToFile(content: String, filepath: Path) {
            filepath.writeText(content, options = arrayOf(StandardOpenOption.APPEND))
        }

        /**
         * Appends the given content to a log file into the directory of the project under test.
         *
         * @param content the content to be appended to the log file
         * @param alsoPrint if true then the content will be printed in the console as well
         */
        fun log(content: String, alsoPrint: Boolean = true) {
            val logFilepath = getOrCreateFileInOutputDirectory("test-generation.log")
            appendToFile(content + "\n", logFilepath)
            if (alsoPrint) {
                println(content)
            }
        }
    }
}